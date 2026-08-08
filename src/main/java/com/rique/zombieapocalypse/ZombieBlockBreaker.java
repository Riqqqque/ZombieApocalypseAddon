package com.rique.zombieapocalypse;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.state.BlockState;

public final class ZombieBlockBreaker {

    @FunctionalInterface
    public interface DestroyPermission {
        boolean canDestroy(ServerLevel level, BlockPos pos, Zombie zombie, boolean respectMobGriefing);
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    private record Settings(
            boolean debugLogging,
            double chance,
            int range,
            double maxHardness,
            boolean dropBlocks,
            boolean requireTarget,
            boolean requireObstacle,
            boolean respectMobGriefing,
            boolean allowBlockEntities,
            boolean allowToolRequiredBlocks,
            boolean allowLightBlocks) {

        static Settings capture() {
            return new Settings(
                    Config.COMMON.enableDebugLogging.get(),
                    ConfigValidator.probability(Config.COMMON.zombieBlockBreakingChance.get()),
                    Math.max(1, Config.COMMON.zombieBlockBreakingRange.get()),
                    Math.max(0.0, Config.COMMON.zombieBlockBreakingMaxHardness.get()),
                    Config.COMMON.zombieBlockBreakingDropBlocks.get(),
                    Config.COMMON.zombieBlockBreakingRequireTarget.get(),
                    Config.COMMON.zombieBlockBreakingRequireObstacle.get(),
                    Config.COMMON.zombieBlockBreakingRespectMobGriefing.get(),
                    Config.COMMON.zombieBlockBreakingAllowBlockEntities.get(),
                    Config.COMMON.zombieBlockBreakingAllowToolRequiredBlocks.get(),
                    Config.COMMON.zombieBlockBreakingAllowLightBlocks.get());
        }
    }

    private ZombieBlockBreaker() {
    }

    public static boolean tick(Zombie zombie, DestroyPermission destroyPermission) {
        if (!(zombie.level() instanceof ServerLevel level) || !zombie.isAlive() || zombie.isNoAi()) {
            return false;
        }

        if (!Config.COMMON.enableZombieBlockBreaking.get()) {
            return false;
        }

        if (!isBlockBreakingActive(true, DifficultyManager.getCurrentDay(level),
                Config.COMMON.zombieBlockBreakingStartDay.get())) {
            return false;
        }

        long gameTime = level.getGameTime();
        if (!isScheduledTick(gameTime, zombie.getId(), Config.COMMON.zombieBlockBreakingInterval.get())) {
            return false;
        }

        Settings settings = Settings.capture();
        LivingEntity target = getValidTarget(zombie);
        if (settings.requireTarget() && target == null) {
            return false;
        }

        if (settings.chance() <= 0.0 || zombie.getRandom().nextDouble() >= settings.chance()) {
            return false;
        }

        boolean hasLineOfSight = target != null && zombie.hasLineOfSight(target);
        if (!isObstacleCheckSatisfied(settings.requireObstacle(), zombie.horizontalCollision, target != null, hasLineOfSight)) {
            return false;
        }

        BlockPos targetPos = findBreakTarget(level, zombie, target, settings, destroyPermission);
        if (targetPos == null) {
            return false;
        }

        boolean destroyed = level.destroyBlock(targetPos, settings.dropBlocks(), zombie);
        if (destroyed && settings.debugLogging()) {
            LOGGER.info("[ZombieApocalypse] {} broke block at {}", zombie.getType().getDescriptionId(), targetPos);
        }
        return destroyed;
    }

    static boolean isBlockBreakingActive(boolean enabled, long currentDay, int startDay) {
        return enabled && currentDay >= Math.max(0, startDay);
    }

    static boolean isScheduledTick(long gameTime, int entityId, int intervalTicks) {
        int safeInterval = Math.max(1, intervalTicks);
        return Math.floorMod(gameTime + entityId, safeInterval) == 0L;
    }

    static boolean isObstacleCheckSatisfied(
            boolean requireObstacle,
            boolean horizontalCollision,
            boolean hasTarget,
            boolean hasLineOfSight) {
        return !requireObstacle || horizontalCollision || (hasTarget && !hasLineOfSight);
    }

    static boolean isHardnessAllowed(float hardness, double maxHardness) {
        return hardness >= 0.0F && hardness <= Math.max(0.0, maxHardness);
    }

    static boolean isProtectedByConfig(
            boolean hasBlockEntity,
            boolean allowBlockEntities,
            boolean requiresCorrectTool,
            boolean allowToolRequiredBlocks,
            int lightEmission,
            boolean allowLightBlocks) {
        return (hasBlockEntity && !allowBlockEntities)
                || (requiresCorrectTool && !allowToolRequiredBlocks)
                || (lightEmission > 0 && !allowLightBlocks);
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

    private static BlockPos findBreakTarget(
            ServerLevel level,
            Zombie zombie,
            LivingEntity target,
            Settings settings,
            DestroyPermission destroyPermission) {
        Direction primaryDirection = getPrimaryDirection(zombie, target);
        BlockPos origin = zombie.blockPosition();

        BlockPos found = findBreakTargetInDirection(
                level, zombie, settings, destroyPermission, origin, primaryDirection, settings.range());
        if (found != null) {
            return found;
        }

        for (Direction direction : HORIZONTAL_DIRECTIONS) {
            if (direction == primaryDirection) {
                continue;
            }

            found = findBreakTargetInDirection(level, zombie, settings, destroyPermission, origin, direction, 1);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    private static BlockPos findBreakTargetInDirection(
            ServerLevel level,
            Zombie zombie,
            Settings settings,
            DestroyPermission destroyPermission,
            BlockPos origin,
            Direction direction,
            int maxDistance) {
        for (int distance = 1; distance <= maxDistance; distance++) {
            boolean encounteredBlockingBlock = false;

            for (int height = 0; height <= 1; height++) {
                BlockPos candidate = origin.relative(direction, distance).above(height);
                if (!level.isLoaded(candidate) || !level.getWorldBorder().isWithinBounds(candidate)) {
                    continue;
                }

                BlockState state = level.getBlockState(candidate);
                boolean passThrough = isSearchPassThrough(
                        state.isAir(),
                        !state.getFluidState().isEmpty(),
                        state.getCollisionShape(level, candidate).isEmpty());
                if (passThrough) {
                    continue;
                }

                encounteredBlockingBlock = true;
                if (canBreakCandidate(level, candidate, state, zombie, settings, destroyPermission)) {
                    return candidate.immutable();
                }
            }

            // Never reach through a protected or unbreakable front block to damage
            // something behind it.
            if (encounteredBlockingBlock) {
                return null;
            }
        }

        return null;
    }

    static boolean isSearchPassThrough(boolean air, boolean hasFluid, boolean collisionShapeEmpty) {
        return air || (hasFluid && collisionShapeEmpty);
    }

    private static Direction getPrimaryDirection(Zombie zombie, LivingEntity target) {
        if (target != null) {
            double dx = target.getX() - zombie.getX();
            double dz = target.getZ() - zombie.getZ();
            if (Math.abs(dx) > Math.abs(dz)) {
                return dx >= 0.0 ? Direction.EAST : Direction.WEST;
            }
            if (Math.abs(dz) > 0.0001D) {
                return dz >= 0.0 ? Direction.SOUTH : Direction.NORTH;
            }
        }

        Direction direction = zombie.getDirection();
        return direction.getAxis().isVertical() ? Direction.NORTH : direction;
    }

    private static boolean canBreakCandidate(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            Zombie zombie,
            Settings settings,
            DestroyPermission destroyPermission) {
        if (isDoorProtected(
                state.is(BlockTags.DOORS),
                ZombieCompatibility.shouldRespectDoorBreakingAbility(),
                zombie.canBreakDoors())) {
            return false;
        }

        if (!isAllowedBlockState(level, pos, state, settings)) {
            return false;
        }

        return destroyPermission.canDestroy(level, pos, zombie, settings.respectMobGriefing());
    }

    static boolean isDoorProtected(boolean door, boolean respectDoorBreakingAbility, boolean zombieCanBreakDoors) {
        return door && respectDoorBreakingAbility && !zombieCanBreakDoors;
    }

    private static boolean isAllowedBlockState(ServerLevel level, BlockPos pos, BlockState state, Settings settings) {
        if (state.isAir() || !state.getFluidState().isEmpty()) {
            return false;
        }

        if (state.is(BlockTags.DRAGON_IMMUNE)
                || state.is(BlockTags.WITHER_IMMUNE)
                || state.is(BlockTags.FEATURES_CANNOT_REPLACE)) {
            return false;
        }

        if (!isHardnessAllowed(state.getDestroySpeed(level, pos), settings.maxHardness())) {
            return false;
        }

        return !isProtectedByConfig(
                state.hasBlockEntity(),
                settings.allowBlockEntities(),
                state.requiresCorrectToolForDrops(),
                settings.allowToolRequiredBlocks(),
                state.getLightEmission(level, pos),
                settings.allowLightBlocks());
    }
}
