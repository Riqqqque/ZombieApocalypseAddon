package com.rique.zombieapocalypse;

import java.util.List;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
                    Math.max(0.1, Config.COMMON.zombieToweringVerticalBoost.get()),
                    Math.max(0.0, Config.COMMON.zombieToweringForwardBoost.get()),
                    Math.max(1, Config.COMMON.zombieToweringMaxHeightAboveTarget.get()),
                    Config.COMMON.zombieToweringRequireObstacle.get());
        }
    }

    private record CrowdState(int nearbyZombies, boolean hasSupport) {
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    private ZombieTowering() {
    }

    public static boolean tick(Zombie zombie) {
        if (!(zombie.level() instanceof ServerLevel level)
                || !zombie.isAlive()
                || zombie.isNoAi()
                || zombie.isPassenger()
                || zombie.isVehicle()
                || zombie.isInWaterOrBubble()
                || zombie.isInLava()) {
            return false;
        }

        if (!Config.COMMON.enableZombieTowering.get()
                || !isToweringActive(
                        true,
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

        CrowdState crowd = inspectCrowd(level, zombie, settings.crowdRadius());
        if (!hasRequiredCrowd(crowd.nearbyZombies(), settings.minNearbyZombies())
                || (!zombie.onGround() && !crowd.hasSupport())) {
            return false;
        }

        AABB raisedBounds = zombie.getBoundingBox().move(0.0, settings.verticalBoost() + 0.2, 0.0);
        if (hasBlockCollision(level, zombie, raisedBounds)) {
            return false;
        }

        Vec3 boostedMovement = computeBoostedMovement(
                zombie.getDeltaMovement(),
                direction,
                settings.verticalBoost(),
                settings.forwardBoost());
        zombie.setDeltaMovement(boostedMovement);
        zombie.setOnGround(false);
        zombie.hasImpulse = true;

        if (settings.debugLogging()) {
            LOGGER.info(
                    "[ZombieApocalypse] {} used zombie towering toward {} with {} nearby Zombie subclasses",
                    zombie.getType().getDescriptionId(),
                    target.getType().getDescriptionId(),
                    crowd.nearbyZombies());
        }
        return true;
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
                && verticalDifference >= -0.25
                && verticalDifference <= 1.75;
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

    private static CrowdState inspectCrowd(ServerLevel level, Zombie zombie, double crowdRadius) {
        double safeRadius = Math.max(0.5, crowdRadius);
        AABB searchBounds = zombie.getBoundingBox().inflate(safeRadius, 1.75, safeRadius);
        List<Zombie> nearby = level.getEntitiesOfClass(
                Zombie.class,
                searchBounds,
                other -> other != zombie && other.isAlive() && ZombieClassMobs.isZombieClass(other));

        boolean hasSupport = false;
        for (Zombie other : nearby) {
            double dx = other.getX() - zombie.getX();
            double dz = other.getZ() - zombie.getZ();
            if (isSupportPosition(
                    dx * dx + dz * dz,
                    zombie.getY() - other.getY(),
                    Math.min(1.35, safeRadius))) {
                hasSupport = true;
                break;
            }
        }
        return new CrowdState(nearby.size(), hasSupport);
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
