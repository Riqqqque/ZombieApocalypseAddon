package com.rique.zombieapocalypse;

import java.util.List;

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
                    Math.max(2, Config.COMMON.zombieToweringMaxStackSize.get()),
                    Math.max(1.0, Config.COMMON.zombieToweringDismountDistance.get()),
                    Math.max(0.1, Config.COMMON.zombieToweringVerticalBoost.get()),
                    Math.max(0.0, Config.COMMON.zombieToweringForwardBoost.get()),
                    Math.max(1, Config.COMMON.zombieToweringMaxHeightAboveTarget.get()),
                    Config.COMMON.zombieToweringRequireObstacle.get());
        }
    }

    private record CrowdState(int nearbyZombies, Zombie support) {
    }

    private static final String STACK_RIDER_TAG = "zombieapocalypse.tower_rider";
    private static final Logger LOGGER = LogUtils.getLogger();

    private ZombieTowering() {
    }

    public static boolean tick(Zombie zombie) {
        if (!(zombie.level() instanceof ServerLevel level) || !zombie.isAlive()) {
            return false;
        }

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
        if (!hasClearStackSpace(level, zombie, support) || !zombie.addTag(STACK_RIDER_TAG)) {
            return false;
        }
        if (!zombie.startRiding(support, true)) {
            zombie.removeTag(STACK_RIDER_TAG);
            return false;
        }

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
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Zombie zombie && releaseStackRider(zombie)) {
                    released++;
                }
            }
        }
        return released;
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
        if (!(zombie.getVehicle() instanceof Zombie support)) {
            zombie.removeTag(STACK_RIDER_TAG);
            return false;
        }

        Settings settings = Settings.capture();
        LivingEntity target = getValidTarget(zombie);
        boolean active = !zombie.isNoAi()
                && !zombie.isInWaterOrBubble()
                && !zombie.isInLava()
                && support.isAlive()
                && isToweringActive(
                        Config.COMMON.enableZombieTowering.get(),
                        DifficultyManager.getCurrentDay(level),
                        Config.COMMON.zombieToweringStartDay.get())
                && stackSizeBelow(zombie) <= settings.maxStackSize()
                && target != null
                && isTargetDistanceAllowed(zombie.distanceToSqr(target), settings.maxTargetDistance())
                && isHeightAllowed(zombie.getY(), target.getY(), settings.maxHeightAboveTarget());
        if (!active) {
            releaseStackRider(zombie);
            return true;
        }

        if (zombie.isVehicle()) {
            return true;
        }

        double dx = target.getX() - zombie.getX();
        double dz = target.getZ() - zombie.getZ();
        double horizontalDistanceSquared = dx * dx + dz * dz;
        if (!shouldDismount(
                horizontalDistanceSquared,
                zombie.getY(),
                target.getY(),
                settings.dismountDistance(),
                zombie.hasLineOfSight(target))) {
            return true;
        }

        int formerStackSize = stackSizeBelow(zombie);
        Vec3 direction = horizontalDirection(zombie, target);
        if (!releaseStackRider(zombie)) {
            return true;
        }
        zombie.setDeltaMovement(computeBoostedMovement(
                zombie.getDeltaMovement(),
                direction,
                settings.verticalBoost(),
                settings.forwardBoost()));
        zombie.setOnGround(false);
        zombie.hasImpulse = true;

        if (settings.debugLogging()) {
            LOGGER.info(
                    "[ZombieApocalypse] {} jumped from a {}-zombie tower toward {}",
                    zombie.getType().getDescriptionId(),
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
        double verticalSearch = Math.max(2.5, settings.maxStackSize() * 1.75);
        AABB searchBounds = zombie.getBoundingBox().inflate(
                settings.crowdRadius(),
                verticalSearch,
                settings.crowdRadius());
        List<Zombie> nearby = level.getEntitiesOfClass(
                Zombie.class,
                searchBounds,
                other -> other != zombie && other.isAlive() && ZombieClassMobs.isZombieClass(other));

        Zombie bestSupport = null;
        int bestStackSize = 0;
        double bestDistanceSquared = Double.MAX_VALUE;
        for (Zombie other : nearby) {
            double dx = other.getX() - zombie.getX();
            double dz = other.getZ() - zombie.getZ();
            double horizontalDistanceSquared = dx * dx + dz * dz;
            int stackSize = stackSizeBelow(other);
            double predictedY = other.getY() + other.getBbHeight() * 0.75 + 0.1;
            if (!isSupportPosition(
                    horizontalDistanceSquared,
                    zombie.getY() - other.getY(),
                    Math.min(1.35, settings.crowdRadius()))
                    || !isValidSupport(other, settings.maxStackSize())
                    || !isHeightAllowed(predictedY, target.getY(), settings.maxHeightAboveTarget())) {
                continue;
            }

            if (stackSize > bestStackSize
                    || (stackSize == bestStackSize && horizontalDistanceSquared < bestDistanceSquared)) {
                bestSupport = other;
                bestStackSize = stackSize;
                bestDistanceSquared = horizontalDistanceSquared;
            }
        }
        return new CrowdState(nearby.size(), bestSupport);
    }

    private static boolean isValidSupport(Zombie support, int maxStackSize) {
        if (support.isNoAi()
                || support.isInWaterOrBubble()
                || support.isInLava()
                || support.isVehicle()
                || !canGrowStack(stackSizeBelow(support), maxStackSize)) {
            return false;
        }
        if (support.isPassenger() && !isAddonStackRider(support)) {
            return false;
        }

        Entity bottom = support;
        while (bottom.getVehicle() instanceof Zombie vehicle) {
            bottom = vehicle;
        }
        return bottom.onGround();
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
        if (target == null || !target.isAlive()) {
            return null;
        }
        if (target instanceof ServerPlayer player && (player.isCreative() || player.isSpectator())) {
            return null;
        }
        return target;
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
