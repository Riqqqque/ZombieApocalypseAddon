package com.rique.zombieapocalypse;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.monster.Zombie;

/**
 * Idle zombies seek out and ride nearby zombie horses. Ported from the
 * "Zombies Eat Animals" mount goal: 32-block scan, 3-block mount distance,
 * repath every 12 ticks, 60-tick failed-path cooldown.
 */
public final class ZombieMountZombieHorseGoal extends Goal {

    private static final double SEARCH_RADIUS = 32.0;
    private static final double MOUNT_DISTANCE = 3.0;
    private static final int SCAN_INTERVAL_TICKS = 20;
    private static final int REPATH_INTERVAL_TICKS = 12;
    private static final int FAILED_PATH_COOLDOWN_TICKS = 60;

    private final Zombie zombie;
    private final double mountDistanceSqr = MOUNT_DISTANCE * MOUNT_DISTANCE;

    private ZombieHorse targetHorse;
    private long nextScanTick;
    private long nextRepathTick;
    private long failedPathCooldownUntil;

    public ZombieMountZombieHorseGoal(Zombie zombie) {
        this.zombie = zombie;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!ZombieHunt.isEnabled() || !Config.COMMON.rideZombieHorses.get()) {
            return false;
        }
        if (!zombie.isAlive() || zombie.isPassenger() || zombie.getVehicle() != null
                || zombie.getTarget() != null) {
            return false;
        }

        long now = zombie.level().getGameTime();
        if (now < failedPathCooldownUntil || now < nextScanTick) {
            return false;
        }
        nextScanTick = now + SCAN_INTERVAL_TICKS + (zombie.getId() % 5L);

        targetHorse = findNearestMountableHorse();
        return targetHorse != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!ZombieHunt.isEnabled() || !Config.COMMON.rideZombieHorses.get()
                || targetHorse == null || !targetHorse.isAlive()
                || zombie.isPassenger() || targetHorse.isVehicle()) {
            return false;
        }
        double maxDistance = SEARCH_RADIUS * SEARCH_RADIUS * 1.5D;
        return zombie.distanceToSqr(targetHorse) <= maxDistance;
    }

    @Override
    public void start() {
        nextRepathTick = 0L;
        tryMoveToTarget(zombie.level().getGameTime());
    }

    @Override
    public void stop() {
        zombie.getNavigation().stop();
        targetHorse = null;
        nextRepathTick = 0L;
    }

    @Override
    public void tick() {
        if (targetHorse == null) {
            return;
        }
        zombie.getLookControl().setLookAt(targetHorse, 30.0F, 30.0F);
        if (targetHorse.isVehicle()) {
            stop();
            return;
        }
        if (zombie.distanceToSqr(targetHorse) <= mountDistanceSqr) {
            zombie.startRiding(targetHorse, true);
            zombie.getNavigation().stop();
            return;
        }
        long now = zombie.level().getGameTime();
        if (now >= nextRepathTick) {
            tryMoveToTarget(now);
        }
    }

    private void tryMoveToTarget(long now) {
        if (targetHorse == null || !targetHorse.isAlive()) {
            return;
        }
        if (zombie.getNavigation().moveTo(targetHorse, 1.0D)) {
            nextRepathTick = now + REPATH_INTERVAL_TICKS;
        } else {
            failedPathCooldownUntil = now + FAILED_PATH_COOLDOWN_TICKS;
            nextRepathTick = now + FAILED_PATH_COOLDOWN_TICKS;
            zombie.getNavigation().stop();
            targetHorse = null;
        }
    }

    private ZombieHorse findNearestMountableHorse() {
        List<ZombieHorse> horses = zombie.level().getEntitiesOfClass(
                ZombieHorse.class,
                zombie.getBoundingBox().inflate(SEARCH_RADIUS),
                horse -> horse.isAlive() && !horse.isVehicle());
        ZombieHorse nearest = null;
        double bestDistance = Double.MAX_VALUE;
        for (ZombieHorse horse : horses) {
            double distance = zombie.distanceToSqr(horse);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = horse;
            }
        }
        return nearest;
    }
}
