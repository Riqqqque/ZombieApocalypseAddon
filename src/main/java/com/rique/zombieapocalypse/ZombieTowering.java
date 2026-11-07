package com.rique.zombieapocalypse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ZombieTowering {

    private record Settings(
            boolean debugLogging,
            double chance,
            int maxTargetDistance,
            int minNearbyZombies,
            double crowdRadius,
            int maxStackSize,
            int maxTowersPerPlayer,
            boolean dynamicHeightEnabled,
            int targetHeightOffset,
            boolean smartDismountEnabled,
            boolean jumpingEnabled,
            int jumpCooldownTicks,
            double dismountDistance,
            double verticalBoost,
            double forwardBoost,
            int maxHeightAboveTarget,
            boolean requireObstacle) {

        static Settings capture() {
            return new Settings(
                    Config.COMMON.enableDebugLogging.get(),
                    ConfigValidator.probability(Config.COMMON.zombieToweringChance.get()),
                    Math.max(1, Config.COMMON.zombieToweringMaxTargetDistance.get()),
                    Math.max(1, Config.COMMON.zombieToweringMinNearbyZombies.get()),
                    Math.max(0.5, Config.COMMON.zombieToweringCrowdRadius.get()),
                    Math.max(0, Config.COMMON.zombieToweringMaxStackSize.get()),
                    Math.max(0, Config.COMMON.zombieToweringMaxTowersPerPlayer.get()),
                    Config.COMMON.zombieToweringDynamicHeightEnabled.get(),
                    Math.max(0, Config.COMMON.zombieToweringTargetHeightOffset.get()),
                    Config.COMMON.zombieToweringSmartDismountEnabled.get(),
                    Config.COMMON.zombieToweringJumpingEnabled.get(),
                    Math.max(1, Config.COMMON.zombieToweringJumpCooldownTicks.get()),
                    Math.max(1.0, Config.COMMON.zombieToweringDismountDistance.get()),
                    Math.max(0.1, Config.COMMON.zombieToweringVerticalBoost.get()),
                    Math.max(0.0, Config.COMMON.zombieToweringForwardBoost.get()),
                    Math.max(0, Config.COMMON.zombieToweringMaxHeightAboveTarget.get()),
                    Config.COMMON.zombieToweringRequireObstacle.get());
        }
    }

    private record CrowdState(int nearbyZombies, Zombie support) {
    }

    private record TowerMemory(
            UUID targetId,
            long lastValidTargetTick,
            long lastActionTick,
            long reachableGroundSinceTick) {
    }

    private record TargetState(LivingEntity target, boolean graceExpired) {
    }

    private static final String STACK_RIDER_TAG = "zombieapocalypse.tower_rider";
    private static final long TARGET_LOSS_GRACE_TICKS = 100L;
    private static final long REACHABLE_GROUND_STABILITY_TICKS = 10L;
    private static final int GROUND_REACHABILITY_CHECK_INTERVAL_TICKS = 5;
    private static final int TOWER_LIMIT_CHECK_INTERVAL_TICKS = 20;
    private static final long MEMORY_PRUNE_INTERVAL_TICKS = 1200L;
    private static final double MIN_TOWER_VERTICAL_GAP = 1.5;
    private static final double COLLISION_EPSILON = 1.0E-4;
    private static final int SAFE_DISMOUNT_RADIUS = 3;
    // Standing zombies hover this far above the head below them. A zombie falls about 0.078 blocks in
    // its own tick before the end-of-tick pass puts it back, so the gap keeps the two hitboxes from
    // overlapping, which would make the game push the base sideways every tick.
    static final double STANDING_GAP = 0.1;
    // Guard for walking a tower: no real tower is anywhere near this tall.
    private static final int MAX_CHAIN_STEPS = 1024;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, TowerMemory> TOWER_MEMORIES = new HashMap<>();
    private static long lastMemoryPruneTick = Long.MIN_VALUE;

    private ZombieTowering() {
    }

    public static boolean tick(Zombie zombie) {
        if (!(zombie.level() instanceof ServerLevel level) || !zombie.isAlive()) {
            return false;
        }
        pruneTowerMemories(level.getServer(), level.getGameTime());

        if (isAddonStackRider(zombie)) {
            return maintainStack(level, zombie);
        }

        if (zombie.isNoAi()
                || zombie.isPassenger()
                || hasAnythingOnTop(zombie)
                || TowerLinks.isLinked(zombie)
                || zombie.isInWaterOrBubble()
                || zombie.isInLava()) {
            return false;
        }

        if (!isToweringActive(level)) {
            return false;
        }

        long gameTime = level.getGameTime();
        int scheduleSalt = zombie.getId() * 37 + 23;
        if (!ZombieBlockBreaker.isScheduledTick(
                gameTime,
                scheduleSalt,
                Config.COMMON.zombieToweringInterval.get())) {
            return false;
        }

        LivingEntity target = getValidTarget(zombie);
        if (target == null) {
            return false;
        }

        Settings settings = Settings.capture();
        if (!isTargetDistanceAllowed(zombie.distanceToSqr(target), settings.maxTargetDistance())
                || !isHeightAllowed(
                        zombie.getY(),
                        target.getY(),
                        settings.dynamicHeightEnabled(),
                        settings.targetHeightOffset(),
                        settings.maxHeightAboveTarget())
                || settings.chance() <= 0.0
                || zombie.getRandom().nextDouble() >= settings.chance()) {
            return false;
        }

        Vec3 direction = horizontalDirection(zombie, target);
        boolean hasLineOfSight = zombie.hasLineOfSight(target);
        boolean hasForwardBarrier = hasForwardBarrier(level, zombie, direction);
        boolean normalPathReachable = hasNormalGroundRoute(
                zombie,
                target,
                hasForwardBarrier,
                hasLineOfSight);
        if (!shouldAttemptTower(
                settings.requireObstacle(),
                target.onGround(),
                zombie.getY(),
                target.getY(),
                hasForwardBarrier,
                hasLineOfSight,
                normalPathReachable)) {
            return false;
        }

        CrowdState crowd = inspectCrowd(level, zombie, target, settings);
        if (!hasRequiredCrowd(crowd.nearbyZombies(), settings.minNearbyZombies())
                || crowd.support() == null) {
            return false;
        }

        Zombie support = crowd.support();
        Zombie root = towerRoot(support);
        TowerMemory memory = TOWER_MEMORIES.get(root.getUUID());
        if (memory != null
                && !canPerformTowerAction(gameTime, memory.lastActionTick(), settings.jumpCooldownTicks())) {
            return false;
        }
        boolean createsNewTower = towerSize(root) == 1;
        if (createsNewTower
                && target instanceof ServerPlayer player
                && !hasTowerSlot(
                        countLoadedTowersFor(level, player, settings.maxTargetDistance()),
                        settings.maxTowersPerPlayer())) {
            return false;
        }
        if (!joinTower(level, zombie, support)) {
            return false;
        }
        rememberAction(root, target, gameTime);

        if (settings.debugLogging()) {
            LOGGER.info(
                    "[ZombieApocalypse] {} joined a {}-zombie tower toward {}",
                    zombie.getType().getDescriptionId(),
                    stackSizeBelow(zombie),
                    target.getType().getDescriptionId());
        }
        return true;
    }

    public static int releaseAll(MinecraftServer server) {
        int released = 0;
        for (ServerLevel level : server.getAllLevels()) {
            for (Zombie root : loadedTowerRoots(level)) {
                released += releaseTower(root);
            }
            List<Zombie> malformedRiders = new ArrayList<>();
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Zombie zombie && isAddonStackRider(zombie)) {
                    malformedRiders.add(zombie);
                }
            }
            for (Zombie zombie : malformedRiders) {
                if (releaseStackRider(zombie)) {
                    released++;
                }
            }
        }
        TOWER_MEMORIES.clear();
        TowerLinks.clear();
        lastMemoryPruneTick = Long.MIN_VALUE;
        return released;
    }

    public static void clearRuntimeState() {
        TOWER_MEMORIES.clear();
        TowerLinks.clear();
        lastMemoryPruneTick = Long.MIN_VALUE;
    }

    /**
     * End-of-tick pass for standing towers: puts every standing zombie back on the head below it and
     * marks the whole tower for a position update every tick, so players see the stack move as one.
     * Runs after all entities ticked; the server sends positions at the start of the next tick, so
     * players only ever see these snapped positions.
     */
    public static void positionStandingTowers(ServerLevel level) {
        if (TowerLinks.isEmpty()) {
            return;
        }
        List<UUID> uppers = TowerLinks.uppersIn(level.dimension());
        if (uppers.isEmpty()) {
            return;
        }

        Set<Zombie> roots = new LinkedHashSet<>();
        for (UUID upperId : uppers) {
            Entity entity = level.getEntity(upperId);
            if (!(entity instanceof Zombie upper) || !upper.isAlive() || upper.isRemoved()) {
                // Dead, unloaded, or gone. If it is ever loaded again, maintainStack drops its tag.
                TowerLinks.unlink(upperId);
                continue;
            }
            if (upper.isPassenger() || !isAddonStackRider(upper)) {
                dropStandingLink(level, upper, false);
                continue;
            }
            Zombie support = TowerLinks.supportOf(upper);
            if (support == null) {
                // The zombie below died or is gone. Put this one down on safe ground, the way the
                // game dismounts a rider whose vehicle disappears, instead of letting it fall.
                dropStandingLink(level, upper, true);
                continue;
            }
            if (!TowerLinks.isLinked(support)) {
                roots.add(support);
            }
        }

        for (Zombie root : roots) {
            root.hasImpulse = true;
            Zombie current = root;
            Zombie upper;
            int steps = 0;
            while ((upper = TowerLinks.upperOf(current)) != null
                    && isAddonStackRider(upper)
                    && steps++ < MAX_CHAIN_STEPS) {
                placeStanding(upper, current);
                current = upper;
            }
        }
    }

    public static int trimLoadedTowers(MinecraftServer server, int maximumStackSize) {
        if (maximumStackSize <= 0) {
            return 0;
        }
        int released = 0;
        int safeMaximum = Math.min(ConfigLimits.MAX_TOWER_STACK_SIZE, Math.max(1, maximumStackSize));
        for (ServerLevel level : server.getAllLevels()) {
            for (Zombie root : loadedTowerRoots(level)) {
                released += trimTower(root, safeMaximum);
            }
        }
        return released;
    }

    public static int enforcePerPlayerLimit(MinecraftServer server, int maximumTowersPerPlayer) {
        if (maximumTowersPerPlayer <= 0) {
            return 0;
        }

        int released = 0;
        for (ServerLevel level : server.getAllLevels()) {
            Map<UUID, List<Zombie>> towersByPlayer = new HashMap<>();
            for (Zombie root : loadedTowerRoots(level)) {
                LivingEntity target = findLiveChainTarget(root);
                ServerPlayer player;
                if (target instanceof ServerPlayer currentPlayer) {
                    player = currentPlayer;
                } else {
                    TowerMemory memory = TOWER_MEMORIES.get(root.getUUID());
                    Entity remembered = memory == null || memory.targetId() == null
                            ? null
                            : level.getEntity(memory.targetId());
                    if (remembered instanceof ServerPlayer rememberedPlayer && isValidTarget(rememberedPlayer)) {
                        player = rememberedPlayer;
                    } else {
                        continue;
                    }
                }
                towersByPlayer.computeIfAbsent(player.getUUID(), ignored -> new ArrayList<>()).add(root);
            }

            for (List<Zombie> roots : towersByPlayer.values()) {
                roots.sort(Comparator.comparingInt(Entity::getId));
                for (int i = maximumTowersPerPlayer; i < roots.size(); i++) {
                    released += releaseTower(roots.get(i));
                }
            }
        }
        return released;
    }

    public static int countLoadedTowers(MinecraftServer server) {
        int count = 0;
        for (ServerLevel level : server.getAllLevels()) {
            count += loadedTowerRoots(level).size();
        }
        return count;
    }

    public static boolean needsMaintenance(Zombie zombie) {
        return isAddonStackRider(zombie);
    }

    static boolean isToweringActive(ServerLevel level) {
        return HordeManager.isAbilityActive(level,
                Config.COMMON.enableZombieTowering.get(),
                Config.COMMON.zombieToweringStartDay.get(),
                Config.COMMON.zombieToweringDuringEvents.get());
    }

    static boolean isToweringActive(boolean enabled, long currentDay, int startDay) {
        return HordeManager.isAbilityActive(enabled, currentDay, startDay, false, false);
    }

    static boolean isTargetDistanceAllowed(double distanceSquared, int maxDistance) {
        long safeDistance = Math.max(1, maxDistance);
        return distanceSquared <= safeDistance * safeDistance;
    }

    static boolean isHeightAllowed(
            double zombieY,
            double targetY,
            boolean dynamicHeightEnabled,
            int targetHeightOffset,
            int maxHeightAboveTarget) {
        if (dynamicHeightEnabled) {
            double maximumBlockLevel = Math.floor(targetY) + Math.max(0, targetHeightOffset);
            return Math.floor(zombieY) <= maximumBlockLevel;
        }
        return maxHeightAboveTarget <= 0 || zombieY <= targetY + maxHeightAboveTarget;
    }

    static boolean shouldAttemptTower(
            boolean requireObstacle,
            boolean targetOnGround,
            double zombieY,
            double targetY,
            boolean hasForwardBarrier,
            boolean hasLineOfSight,
            boolean normalPathReachable) {
        if (!requireObstacle) {
            return true;
        }

        boolean ordinaryGroundCombat = targetOnGround
                && targetY <= zombieY + 1.0
                && normalPathReachable;
        if (ordinaryGroundCombat) {
            return false;
        }

        boolean meaningfullyRaised = targetY > zombieY + MIN_TOWER_VERTICAL_GAP;
        return meaningfullyRaised
                || hasForwardBarrier
                || (targetOnGround && !hasLineOfSight && !normalPathReachable);
    }

    static boolean hasRequiredCrowd(int nearbyZombies, int minimumNearbyZombies) {
        return Math.max(0, nearbyZombies) >= Math.max(1, minimumNearbyZombies);
    }

    static boolean isSupportPosition(double horizontalDistanceSquared, double verticalDifference, double radius) {
        double safeRadius = Math.max(0.5, radius);
        return horizontalDistanceSquared <= safeRadius * safeRadius
                && verticalDifference >= -2.5
                && verticalDifference <= 1.0;
    }

    static boolean canGrowStack(int currentStackSize, int maximumStackSize) {
        return maximumStackSize <= 0 || Math.max(1, currentStackSize) < maximumStackSize;
    }

    static boolean hasTowerSlot(int activeTowers, int maximumTowersPerPlayer) {
        return maximumTowersPerPlayer <= 0 || Math.max(0, activeTowers) < maximumTowersPerPlayer;
    }

    static boolean isTargetLossGraceExpired(long lastValidTargetTick, long currentGameTime) {
        return currentGameTime - lastValidTargetTick > TARGET_LOSS_GRACE_TICKS;
    }

    static boolean canPerformTowerAction(long currentGameTime, long lastActionTick, int cooldownTicks) {
        return lastActionTick < 0L || currentGameTime - lastActionTick >= Math.max(1, cooldownTicks);
    }

    static boolean shouldDismount(
            double horizontalDistanceSquared,
            double zombieY,
            double targetY,
            double dismountDistance,
            boolean hasLineOfSight) {
        double safeDistance = Math.max(1.0, dismountDistance);
        return horizontalDistanceSquared >= 1.0
                && horizontalDistanceSquared <= safeDistance * safeDistance
                && zombieY >= targetY - 0.75
                && hasLineOfSight;
    }

    static boolean shouldSmartDismount(
            boolean enabled,
            boolean targetOnGround,
            double rootY,
            double targetY,
            boolean normalPathReachable,
            boolean hasForwardBarrier,
            boolean hasLineOfSight) {
        return enabled
                && targetOnGround
                && targetY <= rootY + 1.0
                && (normalPathReachable || (!hasForwardBarrier && hasLineOfSight));
    }

    static boolean isReachableGroundStable(long reachableSinceTick, long currentGameTime) {
        return reachableSinceTick >= 0L
                && currentGameTime - reachableSinceTick >= REACHABLE_GROUND_STABILITY_TICKS;
    }

    static Vec3 computeBoostedMovement(
            Vec3 horizontalDirection,
            double verticalBoost,
            double forwardBoost) {
        double safeVerticalBoost = Math.max(0.1, verticalBoost);
        double safeForwardBoost = Math.max(0.0, forwardBoost);
        double horizontalCap = Math.max(0.1, safeForwardBoost);
        double x = clamp(horizontalDirection.x * safeForwardBoost, -horizontalCap, horizontalCap);
        double z = clamp(horizontalDirection.z * safeForwardBoost, -horizontalCap, horizontalCap);
        return new Vec3(x, safeVerticalBoost, z);
    }

    static Vec3 computeSafeDismountMovement(Vec3 rootMovement, Vec3 awayFromTarget) {
        double x = clamp(rootMovement.x * 0.25 + awayFromTarget.x * 0.05, -0.12, 0.12);
        double z = clamp(rootMovement.z * 0.25 + awayFromTarget.z * 0.05, -0.12, 0.12);
        return new Vec3(x, 0.0, z);
    }

    private static boolean maintainStack(ServerLevel level, Zombie zombie) {
        if (supportOf(zombie) == null) {
            // Its support is gone, or this is a standing zombie loaded back from disk.
            TowerLinks.unlink(zombie);
            zombie.removeTag(STACK_RIDER_TAG);
            return false;
        }

        Settings settings = Settings.capture();
        if (directAddonRider(zombie) != null) {
            return true;
        }
        Zombie root = towerRoot(zombie);
        if (!isLinearTowerMember(root, zombie)) {
            releaseStackRider(zombie);
            return true;
        }
        Zombie top = zombie;

        if (!isToweringActive(level)
                || !isHealthyTower(root)
                || isStandingTower(root) != standingPose()) {
            // The pose setting changed since this tower formed; it re-forms in the new pose.
            releaseTower(root);
            return true;
        }

        if (settings.maxStackSize() > 0 && towerSize(root) > settings.maxStackSize()) {
            trimTower(root, settings.maxStackSize());
            return true;
        }

        TargetState targetState = resolveTarget(level, root, settings.maxTargetDistance());
        LivingEntity target = targetState.target();
        if (target == null) {
            if (targetState.graceExpired()) {
                releaseTower(root);
            }
            return true;
        }
        long gameTime = level.getGameTime();
        if (settings.maxTowersPerPlayer() > 0
                && target instanceof ServerPlayer player
                && ZombieBlockBreaker.isScheduledTick(
                        gameTime,
                        root.getId() * 43 + 11,
                        TOWER_LIMIT_CHECK_INTERVAL_TICKS)
                && !hasTowerSlot(
                        countLoadedTowersFor(level, player, settings.maxTargetDistance()) - 1,
                        settings.maxTowersPerPlayer())) {
            releaseTower(root);
            return true;
        }
        TowerMemory memory = TOWER_MEMORIES.get(root.getUUID());
        boolean actionReady = memory == null
                || canPerformTowerAction(gameTime, memory.lastActionTick(), settings.jumpCooldownTicks());

        if (hasEmbeddedBlockCollision(level, root)) {
            if (moveToSafeGround(level, root, root, target)) {
                positionTowerRiders(root);
                rememberAction(root, target, gameTime);
            } else if (actionReady) {
                releaseTopSafely(root, target, gameTime, settings, "embedded tower base");
            }
            return true;
        }
        if (hasEmbeddedTowerRider(level, root)) {
            releaseTopSafely(root, target, gameTime, settings, "blocked rider space");
            return true;
        }

        if (!isHeightAllowed(
                top.getY(),
                target.getY(),
                settings.dynamicHeightEnabled(),
                settings.targetHeightOffset(),
                settings.maxHeightAboveTarget())) {
            if (actionReady) {
                releaseTopSafely(root, target, gameTime, settings, "height limit");
            }
            return true;
        }

        if (settings.smartDismountEnabled()
                && target.onGround()
                && actionReady
                && ZombieBlockBreaker.isScheduledTick(
                        gameTime,
                        root.getId() * 31 + 7,
                        GROUND_REACHABILITY_CHECK_INTERVAL_TICKS)) {
            Vec3 rootDirection = horizontalDirection(root, target);
            boolean hasForwardBarrier = hasForwardBarrier(level, root, rootDirection);
            boolean hasLineOfSight = root.hasLineOfSight(target);
            boolean normalPathReachable = hasNormalGroundRoute(
                    root,
                    target,
                    hasForwardBarrier,
                    hasLineOfSight);
            boolean reachableGround = shouldSmartDismount(
                    true,
                    true,
                    root.getY(),
                    target.getY(),
                    normalPathReachable,
                    hasForwardBarrier,
                    hasLineOfSight);
            updateReachableGround(root, target, gameTime, reachableGround);
            TowerMemory updatedMemory = TOWER_MEMORIES.get(root.getUUID());
            if (reachableGround
                    && updatedMemory != null
                    && isReachableGroundStable(updatedMemory.reachableGroundSinceTick(), gameTime)) {
                releaseTopSafely(root, target, gameTime, settings, "reachable ground");
                return true;
            }
        } else {
            updateReachableGround(root, target, gameTime, false);
        }

        if (!settings.jumpingEnabled() || !actionReady) {
            return true;
        }

        double dx = target.getX() - top.getX();
        double dz = target.getZ() - top.getZ();
        double horizontalDistanceSquared = dx * dx + dz * dz;
        if (!shouldDismount(
                horizontalDistanceSquared,
                top.getY(),
                target.getY(),
                settings.dismountDistance(),
                top.hasLineOfSight(target))) {
            return true;
        }

        int formerStackSize = towerSize(root);
        Vec3 direction = horizontalDirection(top, target);
        if (!detachRiderAtCurrentPosition(top)) {
            return true;
        }
        rememberAction(root, target, gameTime);
        top.setDeltaMovement(computeBoostedMovement(
                direction,
                settings.verticalBoost(),
                settings.forwardBoost()));
        top.setOnGround(false);
        top.hasImpulse = true;
        if (towerSize(root) <= 1) {
            TOWER_MEMORIES.remove(root.getUUID());
        }

        if (settings.debugLogging()) {
            LOGGER.info(
                    "[ZombieApocalypse] {} jumped from a {}-zombie tower toward {}",
                    top.getType().getDescriptionId(),
                    formerStackSize,
                    target.getType().getDescriptionId());
        }
        return true;
    }

    private static CrowdState inspectCrowd(
            ServerLevel level,
            Zombie zombie,
            LivingEntity target,
            Settings settings) {
        AABB searchBounds = zombie.getBoundingBox().inflate(
                settings.crowdRadius(),
                2.5,
                settings.crowdRadius());
        List<Zombie> nearby = level.getEntitiesOfClass(
                Zombie.class,
                searchBounds,
                other -> other != zombie && other.isAlive() && ZombieClassMobs.isZombieClass(other));

        Set<Zombie> roots = new LinkedHashSet<>();
        for (Zombie other : nearby) {
            roots.add(towerRoot(other));
        }

        Zombie bestSupport = null;
        int bestStackSize = 0;
        double bestDistanceSquared = Double.MAX_VALUE;
        for (Zombie root : roots) {
            Zombie support = towerTop(root);
            double dx = root.getX() - zombie.getX();
            double dz = root.getZ() - zombie.getZ();
            double horizontalDistanceSquared = dx * dx + dz * dz;
            int stackSize = towerSize(root);
            double predictedY = stackedPosition(support, zombie, standingPose()).y;
            if (!isSupportPosition(
                    horizontalDistanceSquared,
                    zombie.getY() - root.getY(),
                    Math.min(1.35, settings.crowdRadius()))
                    || !isValidSupport(level, root, support, settings.maxStackSize())
                    || !isTowerTargetCompatible(root, target)
                    || !isHeightAllowed(
                            predictedY,
                            target.getY(),
                            settings.dynamicHeightEnabled(),
                            settings.targetHeightOffset(),
                            settings.maxHeightAboveTarget())) {
                continue;
            }

            if (stackSize > bestStackSize
                    || (stackSize == bestStackSize && horizontalDistanceSquared < bestDistanceSquared)) {
                bestSupport = support;
                bestStackSize = stackSize;
                bestDistanceSquared = horizontalDistanceSquared;
            }
        }
        return new CrowdState(nearby.size(), bestSupport);
    }

    private static boolean isValidSupport(ServerLevel level, Zombie root, Zombie support, int maxStackSize) {
        if (!isHealthyTower(root)
                || hasAnythingOnTop(support)
                || !canGrowStack(towerSize(root), maxStackSize)
                || !hasStableBlockSupport(level, root)) {
            return false;
        }
        if (towerSize(root) > 1 && isStandingTower(root) != standingPose()) {
            return false;
        }
        if (support.isPassenger() && !isAddonStackRider(support)) {
            return false;
        }

        return true;
    }

    private static boolean joinTower(ServerLevel level, Zombie zombie, Zombie support) {
        if (standingPose()) {
            return joinTowerStanding(level, zombie, support);
        }
        Vec3 originalPosition = zombie.position();
        Vec3 originalMovement = zombie.getDeltaMovement();
        Vec3 ridingPosition = TowerRiderPosition.calculate(support, zombie);
        AABB ridingBounds = zombie.getBoundingBox().move(
                ridingPosition.x - zombie.getX(),
                ridingPosition.y - zombie.getY(),
                ridingPosition.z - zombie.getZ());
        if (hasBlockCollision(level, zombie, ridingBounds)) {
            return false;
        }
        if (!zombie.addTag(STACK_RIDER_TAG)) {
            return false;
        }
        if (!zombie.startRiding(support, true)) {
            zombie.removeTag(STACK_RIDER_TAG);
            return false;
        }

        support.positionRider(zombie);
        if (hasEmbeddedBlockCollision(level, zombie)) {
            zombie.stopRiding();
            if (zombie.getVehicle() instanceof Zombie) {
                return true;
            }
            zombie.removeTag(STACK_RIDER_TAG);
            zombie.moveTo(
                    originalPosition.x,
                    originalPosition.y,
                    originalPosition.z,
                    zombie.getYRot(),
                    zombie.getXRot());
            zombie.setDeltaMovement(originalMovement);
            zombie.hasImpulse = true;
            return false;
        }

        zombie.getNavigation().stop();
        zombie.setDeltaMovement(Vec3.ZERO);
        zombie.fallDistance = 0.0F;
        zombie.hasImpulse = true;
        return true;
    }

    private static boolean joinTowerStanding(ServerLevel level, Zombie zombie, Zombie support) {
        Vec3 originalPosition = zombie.position();
        Vec3 originalMovement = zombie.getDeltaMovement();
        Vec3 standingPosition = stackedPosition(support, zombie, true);
        AABB standingBounds = zombie.getBoundingBox().move(
                standingPosition.x - zombie.getX(),
                standingPosition.y - zombie.getY(),
                standingPosition.z - zombie.getZ());
        if (hasBlockCollision(level, zombie, standingBounds)) {
            return false;
        }
        if (!zombie.addTag(STACK_RIDER_TAG)) {
            return false;
        }
        TowerLinks.link(zombie, support);
        placeStanding(zombie, support);
        if (hasEmbeddedBlockCollision(level, zombie)) {
            TowerLinks.unlink(zombie);
            zombie.removeTag(STACK_RIDER_TAG);
            zombie.moveTo(
                    originalPosition.x,
                    originalPosition.y,
                    originalPosition.z,
                    zombie.getYRot(),
                    zombie.getXRot());
            zombie.setDeltaMovement(originalMovement);
            zombie.hasImpulse = true;
            return false;
        }
        zombie.getNavigation().stop();
        return true;
    }

    private static boolean standingPose() {
        return Config.COMMON.zombieToweringStanding.get();
    }

    /** A tower stands when its bottom zombie has a standing link above it instead of a rider. */
    private static boolean isStandingTower(Zombie root) {
        return TowerLinks.upperOf(root) != null;
    }

    /** The zombie this one is stacked on: its vehicle when sitting, its link when standing. */
    private static Zombie supportOf(Zombie zombie) {
        if (zombie.getVehicle() instanceof Zombie vehicle) {
            return vehicle;
        }
        return TowerLinks.supportOf(zombie);
    }

    /** True when anything rides this zombie or stands on it. */
    private static boolean hasAnythingOnTop(Zombie zombie) {
        return zombie.isVehicle() || TowerLinks.upperOf(zombie) != null;
    }

    private static Vec3 stackedPosition(Zombie support, Zombie zombie, boolean standing) {
        return standing
                ? standingPosition(support.getX(), support.getY(), support.getZ(), support.getBbHeight())
                : TowerRiderPosition.calculate(support, zombie);
    }

    /** Where a standing zombie goes: centered on the support, just above its head. */
    static Vec3 standingPosition(double supportX, double supportY, double supportZ, double supportHeight) {
        return new Vec3(supportX, supportY + supportHeight + STANDING_GAP, supportZ);
    }

    private static void placeStanding(Zombie upper, Zombie support) {
        Vec3 position = stackedPosition(support, upper, true);
        upper.setPos(position.x, position.y, position.z);
        upper.setDeltaMovement(Vec3.ZERO);
        upper.fallDistance = 0.0F;
        upper.setOnGround(true);
        upper.hasImpulse = true;
    }

    /**
     * Ends a standing zombie's tower membership. With toSafeGround it is moved to a nearby safe
     * spot when one exists; otherwise it stays where it is and falls normally.
     */
    private static void dropStandingLink(ServerLevel level, Zombie upper, boolean toSafeGround) {
        TowerLinks.unlink(upper);
        upper.removeTag(STACK_RIDER_TAG);
        if (toSafeGround) {
            Vec3 safePosition = findSafeGroundPosition(level, upper, upper, null, true);
            if (safePosition != null) {
                upper.moveTo(safePosition.x, safePosition.y, safePosition.z, upper.getYRot(), upper.getXRot());
                upper.setDeltaMovement(Vec3.ZERO);
            }
        }
        upper.fallDistance = 0.0F;
        upper.hasImpulse = true;
    }

    private static int stackSizeBelow(Zombie zombie) {
        int size = 1;
        Zombie current = zombie;
        Zombie support;
        while ((support = supportOf(current)) != null && size < MAX_CHAIN_STEPS) {
            size++;
            current = support;
        }
        return size;
    }

    private static Zombie towerRoot(Zombie zombie) {
        Zombie root = zombie;
        Zombie support;
        int steps = 0;
        while (isAddonStackRider(root) && (support = supportOf(root)) != null && steps++ < MAX_CHAIN_STEPS) {
            root = support;
        }
        return root;
    }

    private static Zombie towerTop(Zombie root) {
        Zombie top = root;
        while (true) {
            Zombie rider = directAddonRider(top);
            if (rider == null) {
                return top;
            }
            top = rider;
        }
    }

    private static int towerSize(Zombie root) {
        int size = 1;
        Zombie current = root;
        while ((current = directAddonRider(current)) != null) {
            size++;
        }
        return size;
    }

    private static boolean isLinearTowerMember(Zombie root, Zombie expected) {
        Zombie current = root;
        while (current != null) {
            if (current == expected) {
                return true;
            }
            current = directAddonRider(current);
        }
        return false;
    }

    private static Zombie directAddonRider(Zombie support) {
        Zombie standing = TowerLinks.upperOf(support);
        if (standing != null) {
            return isAddonStackRider(standing) ? standing : null;
        }
        List<Entity> passengers = support.getPassengers();
        if (passengers.size() != 1) {
            return null;
        }
        Entity passenger = passengers.get(0);
        return passenger instanceof Zombie rider && isAddonStackRider(rider) ? rider : null;
    }

    private static boolean isHealthyTower(Zombie root) {
        Zombie current = root;
        while (current != null) {
            if (!current.isAlive()
                    || current.isNoAi()
                    || current.isInWaterOrBubble()
                    || current.isInLava()) {
                return false;
            }
            current = directAddonRider(current);
        }
        return true;
    }

    private static LivingEntity findLiveChainTarget(Zombie root) {
        Zombie current = root;
        while (current != null) {
            LivingEntity target = getValidTarget(current);
            if (target != null) {
                return target;
            }
            current = directAddonRider(current);
        }
        return null;
    }

    private static TargetState resolveTarget(ServerLevel level, Zombie root, int maxTargetDistance) {
        long gameTime = level.getGameTime();
        LivingEntity target = findLiveChainTarget(root);
        if (target != null && isTargetDistanceAllowed(root.distanceToSqr(target), maxTargetDistance)) {
            rememberTarget(root, target, gameTime);
            if (root.getTarget() == null) {
                root.setTarget(target);
            }
            return new TargetState(target, false);
        }

        TowerMemory memory = TOWER_MEMORIES.get(root.getUUID());
        if (memory != null && memory.targetId() != null) {
            Entity remembered = level.getEntity(memory.targetId());
            if (remembered instanceof LivingEntity living
                    && isValidTarget(living)
                    && isTargetDistanceAllowed(root.distanceToSqr(living), maxTargetDistance)) {
                rememberTarget(root, living, gameTime);
                root.setTarget(living);
                return new TargetState(living, false);
            }
        }

        if (memory == null) {
            TOWER_MEMORIES.put(root.getUUID(), new TowerMemory(null, gameTime, -1L, -1L));
            return new TargetState(null, false);
        }
        return new TargetState(null, isTargetLossGraceExpired(memory.lastValidTargetTick(), gameTime));
    }

    private static void rememberTarget(Zombie root, LivingEntity target, long gameTime) {
        TowerMemory current = TOWER_MEMORIES.get(root.getUUID());
        boolean sameTarget = current != null && target.getUUID().equals(current.targetId());
        long lastActionTick = sameTarget ? current.lastActionTick() : -1L;
        long reachableGroundSinceTick = sameTarget ? current.reachableGroundSinceTick() : -1L;
        TOWER_MEMORIES.put(root.getUUID(), new TowerMemory(
                target.getUUID(),
                gameTime,
                lastActionTick,
                reachableGroundSinceTick));
    }

    private static void rememberAction(Zombie root, LivingEntity target, long gameTime) {
        TowerMemory current = TOWER_MEMORIES.get(root.getUUID());
        long reachableGroundSinceTick = current == null ? -1L : current.reachableGroundSinceTick();
        TOWER_MEMORIES.put(root.getUUID(), new TowerMemory(
                target.getUUID(),
                gameTime,
                gameTime,
                reachableGroundSinceTick));
    }

    private static void updateReachableGround(
            Zombie root,
            LivingEntity target,
            long gameTime,
            boolean reachableGround) {
        TowerMemory current = TOWER_MEMORIES.get(root.getUUID());
        long lastActionTick = current == null ? -1L : current.lastActionTick();
        boolean sameTarget = current != null && target.getUUID().equals(current.targetId());
        long reachableSince = reachableGround
                ? sameTarget && current.reachableGroundSinceTick() >= 0L
                        ? current.reachableGroundSinceTick()
                        : gameTime
                : -1L;
        TOWER_MEMORIES.put(root.getUUID(), new TowerMemory(
                target.getUUID(),
                gameTime,
                lastActionTick,
                reachableSince));
    }

    private static boolean isTowerTargetCompatible(Zombie root, LivingEntity target) {
        if (directAddonRider(root) == null) {
            TOWER_MEMORIES.remove(root.getUUID());
            return true;
        }
        LivingEntity currentTarget = findLiveChainTarget(root);
        if (currentTarget != null) {
            return currentTarget.getUUID().equals(target.getUUID());
        }
        TowerMemory memory = TOWER_MEMORIES.get(root.getUUID());
        return memory == null || memory.targetId() == null || memory.targetId().equals(target.getUUID());
    }

    private static void pruneTowerMemories(MinecraftServer server, long gameTime) {
        if (lastMemoryPruneTick != Long.MIN_VALUE
                && gameTime >= lastMemoryPruneTick
                && gameTime - lastMemoryPruneTick < MEMORY_PRUNE_INTERVAL_TICKS) {
            return;
        }
        lastMemoryPruneTick = gameTime;
        if (TOWER_MEMORIES.isEmpty()) {
            return;
        }

        Set<UUID> loadedRoots = new LinkedHashSet<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (Zombie root : loadedTowerRoots(level)) {
                loadedRoots.add(root.getUUID());
            }
        }
        TOWER_MEMORIES.keySet().removeIf(rootId -> !loadedRoots.contains(rootId));
    }

    private static int countLoadedTowersFor(ServerLevel level, ServerPlayer player, int maxTargetDistance) {
        int count = 0;
        AABB bounds = player.getBoundingBox().inflate(Math.max(4, maxTargetDistance));
        List<Zombie> possibleRoots = level.getEntitiesOfClass(
                Zombie.class,
                bounds,
                zombie -> !zombie.isPassenger() && !TowerLinks.isLinked(zombie) && directAddonRider(zombie) != null);
        for (Zombie root : possibleRoots) {
            LivingEntity target = findLiveChainTarget(root);
            TowerMemory memory = TOWER_MEMORIES.get(root.getUUID());
            UUID targetId = target != null ? target.getUUID() : memory == null ? null : memory.targetId();
            if (player.getUUID().equals(targetId)) {
                count++;
            }
        }
        return count;
    }

    private static List<Zombie> loadedTowerRoots(ServerLevel level) {
        List<Zombie> roots = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof Zombie zombie
                    && !zombie.isPassenger()
                    && !TowerLinks.isLinked(zombie)
                    && directAddonRider(zombie) != null) {
                roots.add(zombie);
            }
        }
        return roots;
    }

    private static int trimTower(Zombie root, int maximumStackSize) {
        List<Zombie> riders = ridersBottomToTop(root);
        int keepRiders = Math.max(1, maximumStackSize) - 1;
        int released = 0;
        for (int i = riders.size() - 1; i >= keepRiders; i--) {
            if (releaseStackRider(riders.get(i))) {
                released++;
            }
        }
        if (towerSize(root) <= 1) {
            TOWER_MEMORIES.remove(root.getUUID());
        }
        return released;
    }

    private static boolean releaseTopSafely(
            Zombie root,
            LivingEntity target,
            long gameTime,
            Settings settings,
            String reason) {
        Zombie top = towerTop(root);
        if (top == root) {
            TOWER_MEMORIES.remove(root.getUUID());
            return false;
        }

        int formerStackSize = towerSize(root);
        if (!(root.level() instanceof ServerLevel level)
                || !releaseStackRiderToGround(level, top, root, target)) {
            return false;
        }

        top.setTarget(target);
        if (towerSize(root) <= 1) {
            TOWER_MEMORIES.remove(root.getUUID());
        } else {
            rememberAction(root, target, gameTime);
        }

        if (settings.debugLogging()) {
            LOGGER.info(
                    "[ZombieApocalypse] Safely released {} from a {}-zombie tower ({})",
                    top.getType().getDescriptionId(),
                    formerStackSize,
                    reason);
        }
        return true;
    }

    private static int releaseTower(Zombie root) {
        List<Zombie> riders = ridersBottomToTop(root);
        int released = 0;
        for (int i = riders.size() - 1; i >= 0; i--) {
            if (releaseStackRider(riders.get(i))) {
                released++;
            }
        }
        TOWER_MEMORIES.remove(root.getUUID());
        return released;
    }

    private static List<Zombie> ridersBottomToTop(Zombie root) {
        List<Zombie> riders = new ArrayList<>();
        Zombie current = directAddonRider(root);
        while (current != null) {
            riders.add(current);
            current = directAddonRider(current);
        }
        return riders;
    }

    private static boolean isAddonStackRider(Zombie zombie) {
        return zombie.getTags().contains(STACK_RIDER_TAG);
    }

    private static boolean releaseStackRider(Zombie zombie) {
        if (!isAddonStackRider(zombie)) {
            return false;
        }
        if (!zombie.isAlive() && TowerLinks.isLinked(zombie)) {
            // Nothing to move. Just drop the tower bookkeeping for a dead standing zombie.
            TowerLinks.unlink(zombie);
            return false;
        }

        if (zombie.level() instanceof ServerLevel level && supportOf(zombie) != null) {
            Zombie root = towerRoot(zombie);
            LivingEntity target = findLiveChainTarget(root);
            if (releaseStackRiderToGround(level, zombie, root, target)) {
                return true;
            }
            if (hasEmbeddedBlockCollision(level, zombie)) {
                return false;
            }
        }

        return detachRiderAtCurrentPosition(zombie);
    }

    private static boolean releaseStackRiderToGround(
            ServerLevel level,
            Zombie zombie,
            Zombie root,
            LivingEntity target) {
        Vec3 safePosition = findSafeGroundPosition(level, zombie, root, target, false);
        if (safePosition == null) {
            return false;
        }

        Vec3 awayFromTarget = target == null ? Vec3.ZERO : horizontalDirectionAway(zombie, target);
        if (!detachRiderAtCurrentPosition(zombie)) {
            return false;
        }
        zombie.moveTo(
                safePosition.x,
                safePosition.y,
                safePosition.z,
                zombie.getYRot(),
                zombie.getXRot());
        zombie.setDeltaMovement(computeSafeDismountMovement(Vec3.ZERO, awayFromTarget));
        zombie.fallDistance = 0.0F;
        zombie.hasImpulse = true;
        return true;
    }

    private static boolean detachRiderAtCurrentPosition(Zombie zombie) {
        if (!isAddonStackRider(zombie)) {
            return false;
        }
        Vec3 supportMovement = Vec3.ZERO;
        if (zombie.getVehicle() instanceof Zombie support) {
            supportMovement = support.getDeltaMovement();
            zombie.stopRiding();
            if (zombie.getVehicle() instanceof Zombie) {
                return false;
            }
        } else if (TowerLinks.isLinked(zombie)) {
            Zombie support = TowerLinks.supportOf(zombie);
            if (support != null) {
                supportMovement = support.getDeltaMovement();
            }
            TowerLinks.unlink(zombie);
        }
        zombie.removeTag(STACK_RIDER_TAG);
        zombie.setDeltaMovement(computeSafeDismountMovement(supportMovement, Vec3.ZERO));
        zombie.fallDistance = 0.0F;
        zombie.hasImpulse = true;
        return true;
    }

    private static boolean canUseNormalNavigation(Zombie zombie, LivingEntity target) {
        if (!target.onGround() || target.getY() > zombie.getY() + 1.0) {
            return false;
        }
        Path path = zombie.getNavigation().createPath(target, 0);
        return path != null && (path.canReach() || path.getDistToTarget() <= 1.5F);
    }

    private static boolean hasNormalGroundRoute(
            Zombie zombie,
            LivingEntity target,
            boolean hasForwardBarrier,
            boolean hasLineOfSight) {
        if (!target.onGround() || target.getY() > zombie.getY() + 1.0) {
            return false;
        }
        return (!hasForwardBarrier && hasLineOfSight) || canUseNormalNavigation(zombie, target);
    }

    private static boolean hasForwardBarrier(ServerLevel level, Zombie zombie, Vec3 direction) {
        AABB shiftedBounds = zombie.getBoundingBox()
                .move(direction.x * 0.45, 0.0, direction.z * 0.45)
                .deflate(0.05, 0.08, 0.05);
        return hasBlockCollision(level, zombie, shiftedBounds);
    }

    private static boolean hasStableBlockSupport(ServerLevel level, Zombie zombie) {
        if (!zombie.onGround() || hasEmbeddedBlockCollision(level, zombie)) {
            return false;
        }
        AABB bounds = zombie.getBoundingBox();
        double inset = Math.min(0.05, Math.max(0.0, zombie.getBbWidth() * 0.2));
        AABB feet = new AABB(
                bounds.minX + inset,
                bounds.minY - 0.08,
                bounds.minZ + inset,
                bounds.maxX - inset,
                bounds.minY + 0.02,
                bounds.maxZ - inset);
        return hasBlockCollision(level, zombie, feet);
    }

    private static boolean hasEmbeddedBlockCollision(ServerLevel level, Zombie zombie) {
        return hasBlockCollision(level, zombie, zombie.getBoundingBox().deflate(COLLISION_EPSILON));
    }

    private static boolean hasEmbeddedTowerRider(ServerLevel level, Zombie root) {
        Zombie current = directAddonRider(root);
        while (current != null) {
            if (hasEmbeddedBlockCollision(level, current)) {
                return true;
            }
            current = directAddonRider(current);
        }
        return false;
    }

    private static boolean moveToSafeGround(
            ServerLevel level,
            Zombie zombie,
            Zombie root,
            LivingEntity target) {
        Vec3 safePosition = findSafeGroundPosition(level, zombie, root, target, true);
        if (safePosition == null) {
            return false;
        }
        zombie.moveTo(
                safePosition.x,
                safePosition.y,
                safePosition.z,
                zombie.getYRot(),
                zombie.getXRot());
        zombie.setDeltaMovement(Vec3.ZERO);
        zombie.fallDistance = 0.0F;
        zombie.hasImpulse = true;
        return true;
    }

    private static Vec3 findSafeGroundPosition(
            ServerLevel level,
            Zombie zombie,
            Zombie root,
            LivingEntity target,
            boolean includeCenter) {
        BlockPos origin = BlockPos.containing(root.getX(), root.getY(), root.getZ());
        Vec3 away = target == null ? Vec3.ZERO : horizontalDirectionAway(root, target);
        List<int[]> offsets = new ArrayList<>();
        if (includeCenter) {
            offsets.add(new int[] { 0, 0 });
        }
        for (int radius = 1; radius <= SAFE_DISMOUNT_RADIUS; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) == radius) {
                        offsets.add(new int[] { x, z });
                    }
                }
            }
        }
        offsets.sort(Comparator
                .<int[]>comparingDouble(offset -> -(offset[0] * away.x + offset[1] * away.z))
                .thenComparingInt(offset -> offset[0] * offset[0] + offset[1] * offset[1]));

        int[] verticalOffsets = { 0, 1, -1, 2, -2 };
        for (int[] offset : offsets) {
            for (int verticalOffset : verticalOffsets) {
                BlockPos candidatePos = origin.offset(offset[0], verticalOffset, offset[1]);
                if (!level.hasChunk(candidatePos.getX() >> 4, candidatePos.getZ() >> 4)) {
                    continue;
                }
                Vec3 candidate = DismountHelper.findSafeDismountLocation(
                        zombie.getType(),
                        level,
                        candidatePos,
                        true);
                if (candidate == null) {
                    continue;
                }
                AABB candidateBounds = zombie.getBoundingBox().move(
                        candidate.x - zombie.getX(),
                        candidate.y - zombie.getY(),
                        candidate.z - zombie.getZ());
                if (level.noCollision(zombie, candidateBounds)
                        && (zombie == root || !intersectsTower(candidateBounds, root, zombie))) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static boolean intersectsTower(AABB bounds, Zombie root, Zombie excluded) {
        Zombie current = root;
        while (current != null) {
            if (current != excluded && current.getBoundingBox().intersects(bounds)) {
                return true;
            }
            current = directAddonRider(current);
        }
        return false;
    }

    private static void positionTowerRiders(Zombie support) {
        Zombie rider = directAddonRider(support);
        if (rider == null) {
            return;
        }
        if (TowerLinks.isLinked(rider)) {
            placeStanding(rider, support);
        } else {
            support.positionRider(rider);
        }
        positionTowerRiders(rider);
    }

    private static boolean hasBlockCollision(ServerLevel level, Zombie zombie, AABB bounds) {
        return level.getBlockCollisions(zombie, bounds).iterator().hasNext();
    }

    private static LivingEntity getValidTarget(Zombie zombie) {
        LivingEntity target = zombie.getTarget();
        return isValidTarget(target) ? target : null;
    }

    private static boolean isValidTarget(LivingEntity target) {
        return target != null
                && target.isAlive()
                && (!(target instanceof ServerPlayer player) || (!player.isCreative() && !player.isSpectator()));
    }

    private static Vec3 horizontalDirection(Zombie zombie, LivingEntity target) {
        double dx = target.getX() - zombie.getX();
        double dz = target.getZ() - zombie.getZ();
        double lengthSquared = dx * dx + dz * dz;
        if (lengthSquared < 1.0E-6) {
            Vec3 look = zombie.getLookAngle();
            dx = look.x;
            dz = look.z;
            lengthSquared = dx * dx + dz * dz;
        }
        if (lengthSquared < 1.0E-6) {
            return new Vec3(0.0, 0.0, 0.0);
        }

        double inverseLength = 1.0 / Math.sqrt(lengthSquared);
        return new Vec3(dx * inverseLength, 0.0, dz * inverseLength);
    }

    private static Vec3 horizontalDirectionAway(Zombie zombie, LivingEntity target) {
        Vec3 towardTarget = horizontalDirection(zombie, target);
        return new Vec3(-towardTarget.x, 0.0, -towardTarget.z);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
