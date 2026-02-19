package com.rique.zombieapocalypse;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = ZombieApocalypseAddon.MODID)
public final class EventHandler {

    private static final Logger LOGGER = LogUtils.getLogger();

    private EventHandler() {
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Zombie zombie) || !Config.COMMON.preventSunBurn.get()) {
            return;
        }

        if (event.getSource().is(DamageTypes.ON_FIRE)
                && event.getSource().getEntity() == null
                && event.getSource().getDirectEntity() == null
                && isLikelySunBurnContext(zombie)) {
            event.setCanceled(true);
            zombie.clearFire();
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof Zombie zombie) || zombie.level().isClientSide) {
            return;
        }

        if (!Config.COMMON.preventSunBurn.get() || !zombie.isOnFire()) {
            return;
        }

        if (isLikelySunBurnContext(zombie)) {
            zombie.clearFire();
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Zombie) || !Config.COMMON.enableExtraDrops.get()) {
            return;
        }

        RandomSource random = event.getEntity().getRandom();
        addDrop(event, Items.BONE, ConfigValidator.probability(Config.COMMON.boneChance.get()), random);
        addDrop(event, Items.STRING, ConfigValidator.probability(Config.COMMON.stringChance.get()), random);
        addDrop(event, Items.ENDER_PEARL, ConfigValidator.probability(Config.COMMON.enderPearlChance.get()), random);
        addDrop(event, Items.PHANTOM_MEMBRANE, ConfigValidator.probability(Config.COMMON.phantomMembraneChance.get()),
                random);
    }

    private static void addDrop(LivingDropsEvent event, Item item, double chance, RandomSource random) {
        if (random.nextDouble() >= chance) {
            return;
        }

        ItemEntity itemEntity = new ItemEntity(
                event.getEntity().level(),
                event.getEntity().getX(),
                event.getEntity().getY(),
                event.getEntity().getZ(),
                new ItemStack(item));
        event.getDrops().add(itemEntity);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Zombie && event.getEntity().level() instanceof ServerLevel serverLevel) {
            if (event.getSource().getEntity() instanceof ServerPlayer player) {
                StatisticsManager.get(serverLevel).addKill(player.getUUID());
            }
        }

        if (event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel serverLevel) {
            StatisticsManager.get(serverLevel).startDeathCooldown(player.getUUID(), serverLevel.getGameTime());

            if (Config.COMMON.enableDebugLogging.get()) {
                LOGGER.info("[ZombieApocalypse] Death cooldown started for {}", player.getGameProfile().getName());
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (level.dimension() == Level.OVERWORLD) {
            HordeManager.tick(level);
        }

        if (!Config.COMMON.enableDaySpawning.get() || !canSpawnInDimension(level)) {
            return;
        }

        boolean eventActive = HordeManager.isHordeActive(level) || HordeManager.isBloodMoonActive(level);
        int interval = ConfigValidator.spawnIntervalTicks(eventActive);
        if (level.getGameTime() % interval != 0L) {
            return;
        }

        double effectiveChance = ConfigValidator.probability(Config.COMMON.daySpawnChance.get());
        if (!level.isDay() && Config.COMMON.enableNightBoost.get()) {
            effectiveChance *= Config.COMMON.nightSpawnMultiplier.get();
        }

        effectiveChance *= HordeManager.getSpawnMultiplier(level);
        effectiveChance = Math.min(1.0, effectiveChance);

        if (effectiveChance <= 0.0) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator() && !player.isCreative()) {
                attemptSpawnZombie(level, player, effectiveChance);
            }
        }
    }

    private static boolean canSpawnInDimension(ServerLevel level) {
        if (level.dimension() == Level.NETHER) {
            return Config.COMMON.netherSpawning.get();
        }
        if (level.dimension() == Level.END) {
            return Config.COMMON.endSpawning.get();
        }
        return true;
    }

    private static void attemptSpawnZombie(ServerLevel level, ServerPlayer player, double baseChance) {
        RandomSource random = level.getRandom();
        double effectiveChance = baseChance;

        if (Config.COMMON.enableDeathCooldown.get()) {
            StatisticsManager stats = StatisticsManager.get(level);
            effectiveChance *= stats.getSpawnFactor(player.getUUID(), level.getGameTime());
        }

        if (effectiveChance <= 0.0 || random.nextDouble() >= effectiveChance) {
            return;
        }

        int detectionRange = Config.COMMON.spawnRange.get();
        int nearbyZombies = countNearbyZombies(level, player, detectionRange);
        int maxZombies = Config.COMMON.maxDayZombiesPerPlayer.get();

        if (nearbyZombies >= maxZombies) {
            if (Config.COMMON.enableDebugLogging.get()) {
                LOGGER.debug("[ZombieApocalypse] Skip spawn near {} because nearby={}/{}",
                        player.getGameProfile().getName(), nearbyZombies, maxZombies);
            }
            return;
        }

        int countToSpawn = Math.max(1, HordeManager.getZombiesPerSpawn(level));
        int maxAttempts = ConfigValidator.spawnAttemptsForWave(countToSpawn);
        int horizontalRange = Math.max(1, Config.COMMON.spawnRange.get());
        int minDistance = Math.max(0, Config.COMMON.minSpawnDistance.get());
        int minDistanceSq = minDistance * minDistance;

        boolean requireSky = level.dimension() == Level.OVERWORLD && Config.COMMON.requireOpenSkyForOverworldSpawns.get();

        int spawned = 0;
        for (int i = 0; i < maxAttempts && spawned < countToSpawn; i++) {
            int x = Mth.floor(player.getX()) + random.nextInt(horizontalRange * 2 + 1) - horizontalRange;
            int z = Mth.floor(player.getZ()) + random.nextInt(horizontalRange * 2 + 1) - horizontalRange;
            int y = chooseSpawnY(level, player, random, x, z);

            BlockPos spawnPos = new BlockPos(x, y, z);

            if (spawnPos.distSqr(player.blockPosition()) < minDistanceSq) {
                continue;
            }

            if (requireSky && !level.canSeeSky(spawnPos)) {
                continue;
            }

            if (!canSpawnInBiome(level, spawnPos)) {
                continue;
            }

            if (!isValidSpawnSpace(level, spawnPos)) {
                continue;
            }

            Zombie zombie = createZombie(level, spawnPos, random);
            if (zombie == null) {
                continue;
            }

            zombie.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                    random.nextFloat() * 360.0F, 0.0F);

            if (random.nextDouble() < ConfigValidator.probability(Config.COMMON.babyZombieChance.get())) {
                zombie.setBaby(true);
            }

            if (!level.noCollision(zombie)) {
                continue;
            }

            zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null);
            DifficultyManager.applyScaling(zombie, level, spawnPos);
            level.addFreshEntity(zombie);
            spawned++;

            if (Config.COMMON.enableSpawnEffects.get()) {
                playSpawnEffects(level, spawnPos);
            }

            if (Config.COMMON.enableDebugLogging.get()) {
                LOGGER.debug("[ZombieApocalypse] Spawned {} at {} for {}",
                        zombie.getType().getDescriptionId(),
                        spawnPos,
                        player.getGameProfile().getName());
            }
        }

        if (Config.COMMON.enableDebugLogging.get() && spawned > 0) {
            LOGGER.info("[ZombieApocalypse] Spawned {}/{} near {}",
                    spawned,
                    countToSpawn,
                    player.getGameProfile().getName());
        }
    }

    private static int chooseSpawnY(ServerLevel level, ServerPlayer player, RandomSource random, int x, int z) {
        if (level.dimension() == Level.NETHER) {
            int minY = Math.max(level.getMinBuildHeight() + 1, Mth.floor(player.getY()) - 16);
            int maxY = Math.min(level.getMaxBuildHeight() - 2, Mth.floor(player.getY()) + 16);
            if (minY >= maxY) {
                return minY;
            }
            return Mth.nextInt(random, minY, maxY);
        }

        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    private static boolean isValidSpawnSpace(ServerLevel level, BlockPos spawnPos) {
        if (!level.getWorldBorder().isWithinBounds(spawnPos)) {
            return false;
        }

        BlockPos belowPos = spawnPos.below();
        BlockPos abovePos = spawnPos.above();

        if (!level.getBlockState(belowPos).isFaceSturdy(level, belowPos, Direction.UP)) {
            return false;
        }

        if (!level.getBlockState(spawnPos).isAir() || !level.getBlockState(abovePos).isAir()) {
            return false;
        }

        if (!level.getFluidState(spawnPos).isEmpty() || !level.getFluidState(abovePos).isEmpty()) {
            return false;
        }

        return true;
    }

    private static int countNearbyZombies(ServerLevel level, ServerPlayer player, int range) {
        return level.getEntitiesOfClass(
                Zombie.class,
                player.getBoundingBox().inflate(range),
                Zombie::isAlive).size();
    }

    private static boolean canSpawnInBiome(ServerLevel level, BlockPos pos) {
        if (!Config.COMMON.enableBiomeModifiers.get()) {
            return true;
        }

        Holder<Biome> biomeHolder = level.getBiome(pos);
        if (Config.COMMON.mushroomSafeZone.get() && biomeHolder.is(Biomes.MUSHROOM_FIELDS)) {
            return false;
        }

        return true;
    }

    private static Zombie createZombie(ServerLevel level, BlockPos pos, RandomSource random) {
        if (!Config.COMMON.enableZombieVariants.get()) {
            return net.minecraft.world.entity.EntityType.ZOMBIE.create(level);
        }

        Holder<Biome> biomeHolder = level.getBiome(pos);
        boolean desertBiome = ConfigValidator.isDesertStyleBiome(biomeHolder);
        boolean waterBiome = ConfigValidator.isWaterStyleBiome(biomeHolder);

        SpawnMath.VariantWeights weights = ConfigValidator.biomeAdjustedVariantWeights(desertBiome, waterBiome);
        double roll = random.nextDouble();

        if (roll < weights.zombieVillagerChance()) {
            return net.minecraft.world.entity.EntityType.ZOMBIE_VILLAGER.create(level);
        }

        roll -= weights.zombieVillagerChance();
        if (roll < weights.huskChance()) {
            return net.minecraft.world.entity.EntityType.HUSK.create(level);
        }

        roll -= weights.huskChance();
        if (roll < weights.drownedChance()) {
            return net.minecraft.world.entity.EntityType.DROWNED.create(level);
        }

        return net.minecraft.world.entity.EntityType.ZOMBIE.create(level);
    }

    private static void playSpawnEffects(ServerLevel level, BlockPos pos) {
        if (Config.COMMON.spawnSound.get()) {
            level.playSound(null, pos, SoundEvents.ZOMBIE_AMBIENT, SoundSource.HOSTILE, 1.0F, 1.0F);
        }

        if (Config.COMMON.spawnParticles.get()) {
            level.sendParticles(ParticleTypes.SMOKE,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    10,
                    0.5D,
                    0.5D,
                    0.5D,
                    0.02D);
        }
    }

    private static boolean isLikelySunBurnContext(Zombie zombie) {
        if (!zombie.level().isDay() || zombie.level().isClientSide) {
            return false;
        }

        if (zombie.isInWaterRainOrBubble() || zombie.isInLava()) {
            return false;
        }

        BlockPos eyePos = BlockPos.containing(zombie.getX(), zombie.getEyeY(), zombie.getZ());
        if (!zombie.level().canSeeSky(eyePos)) {
            return false;
        }

        return zombie.getLightLevelDependentMagicValue() > 0.5F;
    }
}
