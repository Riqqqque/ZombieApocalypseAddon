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

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
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
                    Math.min(ConfigLimits.MAX_TOWER_STACK_SIZE,
                            Math.max(2, Config.COMMON.zombieToweringMaxStackSize.get())),
                    Math.max(0, Config.COMMON.zombieToweringMaxTowersPerPlayer.get()),
                    Config.COMMON.zombieToweringJumpingEnabled.get(),
                    Math.max(1, Config.COMMON.zombieToweringJumpCooldownTicks.get()),
                    Math.max(1.0, Config.COMMON.zombieToweringDismountDistance.get()),
                    Math.max(0.1, Config.COMMON.zombieToweringVerticalBoost.get()),
                    Math.max(0.0, Config.COMMON.zombieToweringForwardBoost.get()),
                    Math.max(1, Config.COMMON.zombieToweringMaxHeightAboveTarget.get()),
                    Config.COMMON.zombieToweringRequireObstacle.get());
        }
    }

    private record CrowdState(int nearbyZombies, Zombie support) {
    }

    private record TowerMemory(UUID targetId, long lastValidTargetTick, long lastJumpTick) {
    }

    private record TargetState(LivingEntity target, boolean graceExpired) {
    }

    private static final String STACK_RIDER_TAG = "zombieapocalypse.tower_rider";
    private static final long TARGET_LOSS_GRACE_TICKS = 100L;
    private static final long MEMORY_PRUNE_INTERVAL_TICKS = 1200L;
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
                || zombie.isVehicle()
                || zombie.isInWaterOrBubble()
                || zombie.isInLava()) {
            return false;
        }

        if (!isToweringActive(
                Config.COMMON.enableZombieTowering.get(),
                DifficultyManager.getCurrentDay(level),
                Config.COMMON.zombieToweringStartDay.get())) {
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
                || !isHeightAllowed(zombie.getY(), target.getY(), settings.maxHeightAboveTarget())
                || settings.chance() <= 0.0
                || zombie.getRandom().nextDouble() >= settings.chance()) {
            return false;
        }

        Vec3 direction = horizontalDirection(zombie, target);
        boolean targetAbove = target.getY() > zombie.getY() + 0.5;
        boolean hasLineOfSight = zombie.hasLineOfSight(target);
        boolean hasForwardBarrier = hasBlockCollision(
                level,
                zombie,
                zombie.getBoundingBox().move(direction.x * 0.35, 0.0, direction.z * 0.35));
        if (!shouldAttemptTower(
                settings.requireObstacle(),
                zombie.horizontalCollision,
                hasForwardBarrier,
                targetAbove,
                hasLineOfSight)) {
            return false;
        }

        CrowdState crowd = inspectCrowd(level, zombie, target, settings);
        if (!hasRequiredCrowd(crowd.nearbyZombies(), settings.minNearbyZombies())
                || crowd.support() == null) {
            return false;
        }

        Zombie support = crowd.support();
        Zombie root = towerRoot(support);
        boolean createsNewTower = towerSize(root) == 1;
        if (createsNewTower
                && target instanceof ServerPlayer player
                && !hasTowerSlot(
                        countLoadedTowersFor(level, player, settings.maxTargetDistance()),
                        settings.maxTowersPerPlayer())) {
            return false;
        }
        if (!hasClearStackSpace(level, zombie, support) || !zombie.addTag(STACK_RIDER_TAG)) {
            return false;
        }
        if (!zombie.startRiding(support, true)) {
            zombie.removeTag(STACK_RIDER_TAG);
            return false;
        }
        rememberTarget(root, target, level.getGameTime());

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
        lastMemoryPruneTick = Long.MIN_VALUE;
        return released;
    }

    public static void clearRuntimeState() {
        TOWER_MEMORIES.clear();
        lastMemoryPruneTick = Long.MIN_VALUE;
    }

    public static int trimLoadedTowers(MinecraftServer server, int maximumStackSize) {
        int released = 0;
        int safeMaximum = Math.min(ConfigLimits.MAX_TOWER_STACK_SIZE, Math.max(2, maximumStackSize));
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

    static boolean isToweringActive(boolean enabled, long currentDay, int startDay) {
        return enabled && currentDay >= Math.max(0, startDay);
    }

    static boolean isTargetDistanceAllowed(double distanceSquared, int maxDistance) {
        long safeDistance = Math.max(1, maxDistance);
        return distanceSquared <= safeDistance * safeDistance;
    }

    static boolean isHeightAllowed(double zombieY, double targetY, int maxHeightAboveTarget) {
        return zombieY <= targetY + Math.max(1, maxHeightAboveTarget);
    }

    static boolean shouldAttemptTower(
            boolean requireObstacle,
            boolean horizontalCollision,
            boolean hasForwardBarrier,
            boolean targetAbove,
            boolean hasLineOfSight) {
        return !requireObstacle
                || horizontalCollision
                || hasForwardBarrier
                || targetAbove
                || !hasLineOfSight;
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
        return Math.max(1, currentStackSize) < Math.max(2, maximumStackSize);
    }

    static boolean hasTowerSlot(int activeTowers, int maximumTowersPerPlayer) {
        return maximumTowersPerPlayer <= 0 || Math.max(0, activeTowers) < maximumTowersPerPlayer;
    }

    static boolean isTargetLossGraceExpired(long lastValidTargetTick, long currentGameTime) {
        return currentGameTime - lastValidTargetTick > TARGET_LOSS_GRACE_TICKS;
    }

    static boolean canJumpFromTower(long currentGameTime, long lastJumpTick, int cooldownTicks) {
        return lastJumpTick < 0L || currentGameTime - lastJumpTick >= Math.max(1, cooldownTicks);
    }

    static boolean shouldDismount(
            double horizontalDistanceSquared,
            double zombieY,
            double targetY,
            double dismountDistance,
            boolean hasLineOfSight) {
        double safeDistance = Math.max(1.0, dismountDistance);
        boolean veryClose = horizontalDistanceSquared <= 2.25;
        return horizontalDistanceSquared <= safeDistance * safeDistance
                && zombieY >= targetY - 0.75
                && (hasLineOfSight || veryClose);
    }

    static Vec3 computeBoostedMovement(
            Vec3 currentMovement,
            Vec3 horizontalDirection,
            double verticalBoost,
            double forwardBoost) {
        double safeVerticalBoost = Math.max(0.1, verticalBoost);
        double safeForwardBoost = Math.max(0.0, forwardBoost);
        double horizontalCap = Math.max(0.1, safeForwardBoost * 1.5);
        double x = clamp(currentMovement.x * 0.5 + horizontalDirection.x * safeForwardBoost,
                -horizontalCap, horizontalCap);
        double z = clamp(currentMovement.z * 0.5 + horizontalDirection.z * safeForwardBoost,
                -horizontalCap, horizontalCap);
        return new Vec3(x, Math.max(currentMovement.y, safeVerticalBoost), z);
    }

    private static boolean maintainStack(ServerLevel level, Zombie zombie) {
        if (!(zombie.getVehicle() instanceof Zombie)) {
            zombie.removeTag(STACK_RIDER_TAG);
            return false;
        }

        Settings settings = Settings.capture();
        Zombie root = towerRoot(zombie);
        if (!isLinearTowerMember(root, zombie)) {
            releaseStackRider(zombie);
            return true;
        }
        Zombie top = towerTop(root);
        if (zombie != top) {
            return true;
        }

        if (!isToweringActive(
                Config.COMMON.enableZombieTowering.get(),
                DifficultyManager.getCurrentDay(level),
                Config.COMMON.zombieToweringStartDay.get())
                || !isHealthyTower(root)) {
            releaseTower(root);
            return true;
        }

        if (towerSize(root) > settings.maxStackSize()) {
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
        if (target instanceof ServerPlayer player
                && !hasTowerSlot(
                        countLoadedTowersFor(level, player, settings.maxTargetDistance()) - 1,
                        settings.maxTowersPerPlayer())) {
            releaseTower(root);
            return true;
        }
        if (!isHeightAllowed(top.getY(), target.getY(), settings.maxHeightAboveTarget())) {
            trimTowerToHeight(root, target, settings.maxHeightAboveTarget());
            return true;
        }
        TowerMemory memory = TOWER_MEMORIES.get(root.getUUID());
        if (!settings.jumpingEnabled()
                || (memory != null
                        && !canJumpFromTower(level.getGameTime(), memory.lastJumpTick(), settings.jumpCooldownTicks()))) {
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
        if (!releaseStackRider(top)) {
            return true;
        }
        rememberJump(root, target, level.getGameTime());
        top.setDeltaMovement(computeBoostedMovement(
                top.getDeltaMovement(),
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
            double predictedY = support.getY() + support.getBbHeight() * 0.75 + 0.1;
            if (!isSupportPosition(
                    horizontalDistanceSquared,
                    zombie.getY() - root.getY(),
                    Math.min(1.35, settings.crowdRadius()))
                    || !isValidSupport(root, support, settings.maxStackSize())
                    || !isTowerTargetCompatible(root, target)
                    || !isHeightAllowed(predictedY, target.getY(), settings.maxHeightAboveTarget())) {
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

    private static boolean isValidSupport(Zombie root, Zombie support, int maxStackSize) {
        if (!isHealthyTower(root)
                || support.isVehicle()
                || !canGrowStack(towerSize(root), maxStackSize)) {
            return false;
        }
        if (support.isPassenger() && !isAddonStackRider(support)) {
            return false;
        }

        return root.onGround();
    }

    private static boolean hasClearStackSpace(ServerLevel level, Zombie zombie, Zombie support) {
        double desiredY = support.getY() + support.getBbHeight() * 0.75 + 0.1;
        AABB desiredBounds = zombie.getBoundingBox().move(
                support.getX() - zombie.getX(),
                desiredY - zombie.getY(),
                support.getZ() - zombie.getZ());
        return !hasBlockCollision(level, zombie, desiredBounds);
    }

    private static int stackSizeBelow(Zombie zombie) {
        int size = 1;
        Entity current = zombie;
        while (current.getVehicle() instanceof Zombie) {
            size++;
            current = current.getVehicle();
        }
        return size;
    }

    private static Zombie towerRoot(Zombie zombie) {
        Zombie root = zombie;
        while (root.getVehicle() instanceof Zombie vehicle && isAddonStackRider(root)) {
            root = vehicle;
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
            TOWER_MEMORIES.put(root.getUUID(), new TowerMemory(null, gameTime, -1L));
            return new TargetState(null, false);
        }
        return new TargetState(null, isTargetLossGraceExpired(memory.lastValidTargetTick(), gameTime));
    }

    private static void rememberTarget(Zombie root, LivingEntity target, long gameTime) {
        TowerMemory current = TOWER_MEMORIES.get(root.getUUID());
        long lastJumpTick = current == null ? -1L : current.lastJumpTick();
        TOWER_MEMORIES.put(root.getUUID(), new TowerMemory(target.getUUID(), gameTime, lastJumpTick));
    }

    private static void rememberJump(Zombie root, LivingEntity target, long gameTime) {
        TOWER_MEMORIES.put(root.getUUID(), new TowerMemory(target.getUUID(), gameTime, gameTime));
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
                zombie -> !zombie.isPassenger() && directAddonRider(zombie) != null);
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

    private static int trimTowerToHeight(Zombie root, LivingEntity target, int maximumHeightAboveTarget) {
        int released = 0;
        Zombie top = towerTop(root);
        while (top != root && !isHeightAllowed(top.getY(), target.getY(), maximumHeightAboveTarget)) {
            if (!releaseStackRider(top)) {
                break;
            }
            released++;
            top = towerTop(root);
        }
        if (top == root) {
            TOWER_MEMORIES.remove(root.getUUID());
        }
        return released;
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
        if (zombie.getVehicle() instanceof Zombie) {
            zombie.stopRiding();
            if (zombie.getVehicle() instanceof Zombie) {
                return false;
            }
        }
        zombie.removeTag(STACK_RIDER_TAG);
        return true;
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

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
