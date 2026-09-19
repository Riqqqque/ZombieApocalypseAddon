package com.rique.zombieapocalypse;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.AABB;

/**
 * Zombies seek out dropped food, eat it to heal, and convert surplus nutrition
 * into permanent bonus health. Mirrors the dropped-meat behavior of the
 * "Zombies Eat Animals" mod: scan every 20 ticks within 32 blocks, eat within
 * 1.0 horizontal / 1.25 vertical, 600-tick give-up, combat lock suppression.
 */
public final class ZombieEatFoodGoal extends Goal {

    private static final double SEARCH_RADIUS = 32.0;
    private static final double SEARCH_RADIUS_VERTICAL = 6.0;
    private static final int SCAN_INTERVAL_TICKS = 20;
    private static final int REPATH_INTERVAL_TICKS = 10;
    private static final int FAIL_TTL_TICKS = 600;
    private static final double EAT_RANGE_SQR = 1.0;
    private static final double EAT_VERTICAL_RANGE = 1.25;

    private final Zombie zombie;
    private ItemEntity targetItem;
    private long nextScanTick;
    private long nextRepathTick;
    private long startedTick;

    public ZombieEatFoodGoal(Zombie zombie) {
        this.zombie = zombie;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!(zombie.level() instanceof ServerLevel)
                || !ZombieHunt.canSeekFood(zombie)
                || ZombieHunt.isCombatLocked(zombie)) {
            return false;
        }
        long gameTime = zombie.level().getGameTime();
        if (gameTime < nextScanTick) {
            return false;
        }
        nextScanTick = gameTime + SCAN_INTERVAL_TICKS + (zombie.getId() % 5L);

        targetItem = findNearestFood();
        if (targetItem == null) {
            return false;
        }
        startedTick = gameTime;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!ZombieHunt.isEnabled()
                || !Config.COMMON.eatDroppedFood.get()
                || ZombieHunt.isCombatLocked(zombie)
                || !ZombieHunt.isNotMaxed(zombie)) {
            return false;
        }
        if (targetItem == null || !targetItem.isAlive() || targetItem.getItem().isEmpty()) {
            return false;
        }
        long gameTime = zombie.level().getGameTime();
        return gameTime - startedTick < FAIL_TTL_TICKS;
    }

    @Override
    public void start() {
        zombie.setTarget(null);
        nextRepathTick = 0L;
        moveToItem(zombie.level().getGameTime());
    }

    @Override
    public void stop() {
        zombie.getNavigation().stop();
        targetItem = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (targetItem == null) {
            return;
        }
        zombie.getLookControl().setLookAt(targetItem, 30.0F, 30.0F);

        if (isCloseEnoughToEat()) {
            ZombieHunt.eatFoodItem(zombie, targetItem);
            stop();
            return;
        }

        long gameTime = zombie.level().getGameTime();
        if (gameTime >= nextRepathTick) {
            moveToItem(gameTime);
        }
    }

    private void moveToItem(long gameTime) {
        if (targetItem == null) {
            return;
        }
        boolean accepted = zombie.getNavigation().moveTo(targetItem, 1.0D);
        nextRepathTick = gameTime + (accepted ? REPATH_INTERVAL_TICKS : 40L);
        if (!accepted) {
            zombie.getNavigation().stop();
        }
    }

    private boolean isCloseEnoughToEat() {
        double dyReference = zombie.getVehicle() != null ? zombie.getVehicle().getY() : zombie.getY();
        double dx = zombie.getX() - targetItem.getX();
        double dz = zombie.getZ() - targetItem.getZ();
        double dy = Math.abs(dyReference - targetItem.getY());
        return dx * dx + dz * dz <= EAT_RANGE_SQR && dy <= EAT_VERTICAL_RANGE;
    }

    private ItemEntity findNearestFood() {
        AABB bounds = zombie.getBoundingBox()
                .inflate(SEARCH_RADIUS, SEARCH_RADIUS_VERTICAL, SEARCH_RADIUS);
        List<ItemEntity> items = zombie.level().getEntitiesOfClass(
                ItemEntity.class,
                bounds,
                item -> item.isAlive() && ZombieHunt.isEdibleFood(item.getItem(), zombie));
        ItemEntity nearest = null;
        double bestDistance = Double.MAX_VALUE;
        for (ItemEntity item : items) {
            double distance = zombie.distanceToSqr(item);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = item;
            }
        }
        return nearest;
    }
}
