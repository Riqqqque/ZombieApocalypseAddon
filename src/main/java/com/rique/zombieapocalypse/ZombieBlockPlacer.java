package com.rique.zombieapocalypse;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ZombieBlockPlacer {

    @FunctionalInterface
    public interface PlacePermission {
        boolean canPlace(
                ServerLevel level,
                BlockPos pos,
                Zombie zombie,
                boolean respectMobGriefing,
                Direction placementDirection);
    }

    private enum PlacementKind {
        BRIDGE,
        STEP
    }

    private record Placement(BlockPos pos, Direction placementDirection, PlacementKind kind) {
    }

    private record Settings(
            boolean debugLogging,
            double chance,
            Block block,
            int maxPerZombie,
            int maxTargetDistance,
            boolean requireTarget,
            boolean requireObstacle,
            boolean respectMobGriefing,
            boolean allowBridges,
            boolean allowSteps,
            boolean replaceFluids,
            boolean replaceReplaceableBlocks) {

        static Settings capture(Block block) {
            return new Settings(
                    Config.COMMON.enableDebugLogging.get(),
                    ConfigValidator.probability(Config.COMMON.zombieBlockPlacingChance.get()),
                    block,
                    Math.max(0, Config.COMMON.zombieBlockPlacingMaxPerZombie.get()),
                    Math.max(1, Config.COMMON.zombieBlockPlacingMaxTargetDistance.get()),
                    Config.COMMON.zombieBlockPlacingRequireTarget.get(),
                    Config.COMMON.zombieBlockPlacingRequireObstacle.get(),
                    Config.COMMON.zombieBlockPlacingRespectMobGriefing.get(),
                    Config.COMMON.zombieBlockPlacingAllowBridges.get(),
                    Config.COMMON.zombieBlockPlacingAllowSteps.get(),
                    Config.COMMON.zombieBlockPlacingReplaceFluids.get(),
                    Config.COMMON.zombieBlockPlacingReplaceReplaceableBlocks.get());
        }
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PLACED_COUNT_TAG = ZombieApocalypseAddon.MODID + "_blocks_placed";
    private static String cachedBlockId;
    private static Block cachedBlock;
    private static String lastWarnedInvalidBlockId;

    private ZombieBlockPlacer() {
    }

    public static void tick(Zombie zombie, PlacePermission placePermission) {
        if (!(zombie.level() instanceof ServerLevel level) || !zombie.isAlive()) {
            return;
        }

        if (!Config.COMMON.enableZombieBlockPlacing.get()) {
            return;
        }

        if (!isBlockPlacingActive(
                true,
                DifficultyManager.getCurrentDay(level),
                Config.COMMON.zombieBlockPlacingStartDay.get())) {
            return;
        }

        long gameTime = level.getGameTime();
        int scheduleSalt = zombie.getId() * 31 + 17;
        if (!ZombieBlockBreaker.isScheduledTick(
                gameTime,
                scheduleSalt,
                Config.COMMON.zombieBlockPlacingInterval.get())) {
            return;
        }

        Block block = resolveConfiguredBlock(level, zombie.blockPosition());
        if (block == null) {
            return;
        }

        Settings settings = Settings.capture(block);
        int placedCount = getPlacedCount(zombie);
        if (!hasPlacementBudget(placedCount, settings.maxPerZombie())) {
            return;
        }

        LivingEntity target = getValidTarget(zombie);
        if (settings.requireTarget() && target == null) {
            return;
        }

        if (target != null
                && !isTargetDistanceAllowed(zombie.distanceToSqr(target), settings.maxTargetDistance())) {
            return;
        }

        if (settings.chance() <= 0.0 || zombie.getRandom().nextDouble() >= settings.chance()) {
            return;
        }

        Placement placement = findPlacement(level, zombie, target, settings);
        if (placement == null) {
            return;
        }

        BlockState placedState = settings.block().defaultBlockState();
        if (!isSafePlacementMaterial(level, placement.pos(), placedState)) {
            cachedBlock = null;
            warnInvalidBlock(Config.COMMON.zombieBlockPlacingBlock.get());
            return;
        }

        if (!placePermission.canPlace(
                level,
                placement.pos(),
                zombie,
                settings.respectMobGriefing(),
                placement.placementDirection())) {
            return;
        }

        if (!level.setBlock(placement.pos(), placedState, 3)) {
            return;
        }

        setPlacedCount(zombie, placedCount + 1);
        level.gameEvent(GameEvent.BLOCK_PLACE, placement.pos(), GameEvent.Context.of(zombie, placedState));
        zombie.getNavigation().recomputePath();

        if (settings.debugLogging()) {
            LOGGER.info(
                    "[ZombieApocalypse] {} placed {} at {} as a {}",
                    zombie.getType().getDescriptionId(),
                    level.registryAccess().registryOrThrow(Registries.BLOCK).getKey(settings.block()),
                    placement.pos(),
                    placement.kind().name().toLowerCase());
        }
    }

    public static void clearRuntimeState() {
        cachedBlockId = null;
        cachedBlock = null;
        lastWarnedInvalidBlockId = null;
    }

    public static void resetPlacementCount(Zombie zombie) {
        zombie.getPersistentData().remove(PLACED_COUNT_TAG);
    }

    public static String validatePlacementBlock(ServerLevel level, BlockPos validationPos, String blockId) {
        Block block = findRegisteredBlock(level, blockId);
        if (block == null) {
            return "Unknown or unsafe block ID: " + blockId;
        }

        if (!isSafePlacementMaterial(level, validationPos, block.defaultBlockState())) {
            return "Block must be stable, solid, breakable, non-fluid, and have no block entity: " + blockId;
        }

        return null;
    }

    static boolean isBlockPlacingActive(boolean enabled, long currentDay, int startDay) {
        return enabled && currentDay >= Math.max(0, startDay);
    }

    static boolean hasPlacementBudget(int placedCount, int maxPerZombie) {
        int safePlacedCount = Math.max(0, placedCount);
        return safePlacedCount < Integer.MAX_VALUE
                && (maxPerZombie <= 0 || safePlacedCount < maxPerZombie);
    }

    static boolean isTargetDistanceAllowed(double distanceSquared, int maxDistance) {
        long safeDistance = Math.max(1, maxDistance);
        return distanceSquared <= safeDistance * safeDistance;
    }

    static boolean isDestinationReplaceable(
            boolean air,
            boolean hasFluid,
            boolean replaceable,
            boolean replaceFluids,
            boolean replaceReplaceableBlocks) {
        if (air) {
            return true;
        }
        if (!replaceable) {
            return false;
        }
        return hasFluid ? replaceFluids : replaceReplaceableBlocks;
    }

    static boolean shouldAttemptStep(
            boolean requireObstacle,
            boolean horizontalCollision,
            boolean hasTarget,
            boolean hasLineOfSight,
            boolean targetIsAbove) {
        return !requireObstacle
                || horizontalCollision
                || (hasTarget && (!hasLineOfSight || targetIsAbove));
    }

    private static Placement findPlacement(
            ServerLevel level,
            Zombie zombie,
            LivingEntity target,
            Settings settings) {
        Direction direction = getPrimaryDirection(zombie, target);
        BlockPos origin = zombie.blockPosition();
        BlockPos ahead = origin.relative(direction);

        if (settings.allowBridges()) {
            BlockPos bridgePos = ahead.below();
            BlockPos supportPos = origin.below();
            if (isDestinationAllowed(level, bridgePos, settings)
                    && isPassageOpen(level, ahead)
                    && isPassageOpen(level, ahead.above())
                    && Block.canSupportCenter(level, supportPos, direction)
                    && hasNoEntityCollision(level, zombie, bridgePos)) {
                return new Placement(bridgePos.immutable(), direction, PlacementKind.BRIDGE);
            }
        }

        if (!settings.allowSteps()) {
            return null;
        }

        boolean hasTarget = target != null;
        boolean hasLineOfSight = hasTarget && zombie.hasLineOfSight(target);
        boolean targetIsAbove = hasTarget && target.getY() > zombie.getY() + 1.0D;
        if (!shouldAttemptStep(
                settings.requireObstacle(),
                zombie.horizontalCollision,
                hasTarget,
                hasLineOfSight,
                targetIsAbove)) {
            return null;
        }

        if (isDestinationAllowed(level, ahead, settings)
                && isPassageOpen(level, ahead.above())
                && Block.canSupportCenter(level, ahead.below(), Direction.UP)
                && hasNoEntityCollision(level, zombie, ahead)) {
            return new Placement(ahead.immutable(), Direction.UP, PlacementKind.STEP);
        }

        return null;
    }

    private static boolean isDestinationAllowed(ServerLevel level, BlockPos pos, Settings settings) {
        if (level.isOutsideBuildHeight(pos)
                || !level.isLoaded(pos)
                || !level.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        return !state.hasBlockEntity()
                && isDestinationReplaceable(
                        state.isAir(),
                        !state.getFluidState().isEmpty(),
                        state.canBeReplaced(),
                        settings.replaceFluids(),
                        settings.replaceReplaceableBlocks());
    }

    private static boolean isPassageOpen(ServerLevel level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos) || !level.isLoaded(pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        return !state.hasBlockEntity() && state.getCollisionShape(level, pos).isEmpty();
    }

    private static boolean hasNoEntityCollision(ServerLevel level, Zombie zombie, BlockPos pos) {
        AABB blockBounds = AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(pos));
        return level.getEntities(zombie, blockBounds).isEmpty();
    }

    private static boolean isSafePlacementMaterial(ServerLevel level, BlockPos pos, BlockState state) {
        return state.getBlock() != Blocks.AIR
                && !(state.getBlock() instanceof EntityBlock)
                && !(state.getBlock() instanceof FallingBlock)
                && state.getFluidState().isEmpty()
                && state.getDestroySpeed(level, pos) >= 0.0F
                && state.isCollisionShapeFullBlock(level, pos)
                && state.canSurvive(level, pos);
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

    private static Block resolveConfiguredBlock(ServerLevel level, BlockPos validationPos) {
        String configuredId = Config.COMMON.zombieBlockPlacingBlock.get().trim();
        if (configuredId.equals(cachedBlockId)) {
            return cachedBlock;
        }

        cachedBlockId = configuredId;
        cachedBlock = findRegisteredBlock(level, configuredId);
        if (cachedBlock == null
                || !isSafePlacementMaterial(level, validationPos, cachedBlock.defaultBlockState())) {
            cachedBlock = null;
            warnInvalidBlock(configuredId);
            return null;
        }

        lastWarnedInvalidBlockId = null;
        return cachedBlock;
    }

    private static Block findRegisteredBlock(ServerLevel level, String blockId) {
        ResourceLocation id = ResourceLocation.tryParse(blockId == null ? "" : blockId.trim());
        Registry<Block> registry = level.registryAccess().registryOrThrow(Registries.BLOCK);
        if (id == null || !registry.containsKey(id)) {
            return null;
        }

        Block block = registry.get(id);
        if (block == Blocks.AIR || block instanceof EntityBlock || block instanceof FallingBlock) {
            return null;
        }
        return block;
    }

    private static void warnInvalidBlock(String blockId) {
        if (blockId != null && blockId.equals(lastWarnedInvalidBlockId)) {
            return;
        }

        lastWarnedInvalidBlockId = blockId;
        LOGGER.warn(
                "[ZombieApocalypse] Zombie block placing is paused because '{}' is not a safe solid block ID. "
                        + "Use a breakable full block such as minecraft:cobblestone.",
                blockId);
    }

    private static int getPlacedCount(Zombie zombie) {
        return Math.max(0, zombie.getPersistentData().getInt(PLACED_COUNT_TAG));
    }

    private static void setPlacedCount(Zombie zombie, int count) {
        CompoundTag data = zombie.getPersistentData();
        data.putInt(PLACED_COUNT_TAG, Math.max(0, count));
    }
}
