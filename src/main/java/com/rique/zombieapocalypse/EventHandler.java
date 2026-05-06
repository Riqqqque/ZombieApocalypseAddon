package com.rique.zombieapocalypse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = ZombieApocalypseAddon.MODID)
public final class EventHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long EXTERNAL_FIRE_GRACE_TICKS = 30L * 20L;
    private static final Map<UUID, Long> EXTERNAL_FIRE_UNTIL = new HashMap<>();

    private record SpawnRuntimeSettings(
            boolean deathCooldownEnabled,
            boolean debugLogging,
            boolean spawnEffectsEnabled,
            boolean spawnSoundEnabled,
            boolean spawnParticlesEnabled,
            boolean requireOpenSky,
            boolean variantsEnabled,
            boolean biomeModifiersEnabled,
            boolean mushroomSafeZone,
            int maxZombiesPerPlayer,
            int horizontalRange,
            int minDistance,
            int attemptsPerZombie,
            int maxBlockLightForSpawning,
            double babyZombieChance,
            double desertHuskBonus,
            double waterDrownedBonus,
            SpawnMath.VariantWeights baseVariantWeights) {

        static SpawnRuntimeSettings capture(ServerLevel level) {
            boolean variantsEnabled = Config.COMMON.enableZombieVariants.get();
            SpawnMath.VariantWeights baseVariantWeights = variantsEnabled
                    ? SpawnMath.normalizeVariantWeights(
                            ConfigValidator.probability(Config.COMMON.zombieVillagerChance.get()),
                            ConfigValidator.probability(Config.COMMON.huskChance.get()),
                            ConfigValidator.probability(Config.COMMON.drownedChance.get()))
                    : SpawnMath.normalizeVariantWeights(0.0, 0.0, 0.0);

            return new SpawnRuntimeSettings(
                    Config.COMMON.enableDeathCooldown.get(),
                    Config.COMMON.enableDebugLogging.get(),
                    Config.COMMON.enableSpawnEffects.get(),
                    Config.COMMON.spawnSound.get(),
                    Config.COMMON.spawnParticles.get(),
                    level.dimension() == Level.OVERWORLD && Config.COMMON.requireOpenSkyForOverworldSpawns.get(),
                    variantsEnabled,
                    Config.COMMON.enableBiomeModifiers.get(),
                    Config.COMMON.mushroomSafeZone.get(),
                    Config.COMMON.maxDayZombiesPerPlayer.get(),
                    Math.max(1, Config.COMMON.spawnRange.get()),
                    Math.max(0, Config.COMMON.minSpawnDistance.get()),
                    Math.max(1, Config.COMMON.spawnAttemptsPerZombie.get()),
                    Config.COMMON.maxBlockLightForSpawning.get(),
                    ConfigValidator.probability(Config.COMMON.babyZombieChance.get()),
                    Config.COMMON.desertHuskBonus.get(),
                    Config.COMMON.waterDrownedBonus.get(),
                    baseVariantWeights);
        }

        boolean hasImpossibleDistanceConfig() {
            return isSpawnDistanceImpossible(minDistance, horizontalRange);
        }

        int minDistanceSquared() {
            return minDistance * minDistance;
        }

        int maxAttemptsForWave(int requestedSpawns) {
            return Math.max(requestedSpawns, requestedSpawns * attemptsPerZombie);
        }

        SpawnMath.VariantWeights variantWeights(boolean desertBiome, boolean waterBiome) {
            return ConfigValidator.biomeAdjustedVariantWeights(
                    baseVariantWeights,
                    variantsEnabled,
                    biomeModifiersEnabled,
                    desertHuskBonus,
                    waterDrownedBonus,
                    desertBiome,
                    waterBiome);
        }
    }

    private EventHandler() {
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!Config.COMMON.preventSunBurn.get()
                || !(event.getEntity() instanceof Zombie zombie)
                || !ZombieClassMobs.isZombieClass(zombie)) {
            return;
        }

        if (!event.getSource().is(DamageTypeTags.IS_FIRE) || !(zombie.level() instanceof ServerLevel level)) {
            return;
        }

        if (isNaturalSunFireDamage(zombie)) {
            event.setCanceled(true);
            zombie.clearFire();
            return;
        }

        rememberExternalFire(zombie, level.getGameTime());
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!Config.COMMON.preventSunBurn.get()) {
            return;
        }

        if (!(event.getEntity() instanceof Zombie zombie)
                || zombie.level().isClientSide
                || !ZombieClassMobs.isZombieClass(zombie)) {
            return;
        }

        if (!zombie.isOnFire()) {
            clearExpiredExternalFire(zombie);
            return;
        }

        long gameTime = zombie.level().getGameTime();
        if (isLikelySunBurnContext(zombie) && !hasRecentExternalFire(zombie, gameTime)) {
            zombie.clearFire();
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!ZombieClassMobs.isZombieClass(event.getEntity()) || !Config.COMMON.enableExtraDrops.get()) {
            return;
        }

        RandomSource random = event.getEntity().getRandom();
        addDrop(event, Items.BONE, ConfigValidator.probability(Config.COMMON.boneChance.get()), random);
        addDrop(event, Items.STRING, ConfigValidator.probability(Config.COMMON.stringChance.get()), random);
        addDrop(event, Items.GUNPOWDER, ConfigValidator.probability(Config.COMMON.gunpowderChance.get()), random);
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
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk()
                || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof Zombie zombie)
                || !ZombieClassMobs.isZombieClass(zombie)) {
            return;
        }

        if (shouldForceAdultZombie(zombie.isBaby(), Config.COMMON.babyZombieChance.get())) {
            forceAdultZombie(zombie);
        }

        DifficultyManager.applyScaling(zombie, level, zombie.blockPosition());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (ZombieClassMobs.isZombieClass(event.getEntity()) && event.getEntity().level() instanceof ServerLevel serverLevel) {
            EXTERNAL_FIRE_UNTIL.remove(event.getEntity().getUUID());
            if (event.getSource().getEntity() instanceof ServerPlayer player) {
                StatisticsManager.KillUpdate killUpdate = StatisticsManager.get(serverLevel)
                        .recordZombieClassKill(player.getUUID());
                ZombieKillAdvancements.awardForKillCount(player, killUpdate.milestoneKills());
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
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        StatisticsManager stats = StatisticsManager.get(serverLevel);
        if (stats.consumePendingAdvancementReset(player.getUUID())) {
            ZombieKillAdvancements.clearMilestones(player);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        long gameTime = level.getGameTime();
        if (level.dimension() == Level.OVERWORLD) {
            pruneExpiredExternalFire(gameTime);
            HordeManager.tick(level);
        }

        if (!Config.COMMON.enableDaySpawning.get() || !canSpawnInDimension(level)) {
            return;
        }

        HordeManager.EventState eventState = HordeManager.getEventState(level);
        if (isDaylightSpawnBlocked(level, eventState.hordeActive())) {
            return;
        }

        boolean eventActive = eventState.hordeActive() || eventState.bloodMoonActive();
        int interval = ConfigValidator.spawnIntervalTicks(eventActive);
        if (gameTime % interval != 0L) {
            return;
        }

        double effectiveChance = ConfigValidator.probability(Config.COMMON.daySpawnChance.get());
        if (!level.isDay() && Config.COMMON.enableNightBoost.get()) {
            effectiveChance *= Config.COMMON.nightSpawnMultiplier.get();
        }

        effectiveChance *= eventState.spawnMultiplier();
        effectiveChance = Math.min(1.0, effectiveChance);

        if (effectiveChance <= 0.0) {
            return;
        }

        if (level.players().isEmpty()) {
            return;
        }

        SpawnRuntimeSettings settings = SpawnRuntimeSettings.capture(level);
        if (settings.hasImpossibleDistanceConfig()) {
            if (settings.debugLogging()) {
                LOGGER.warn("[ZombieApocalypse] minSpawnDistance ({}) is too large for spawnRange ({}). No spawns possible!",
                        settings.minDistance(), settings.horizontalRange());
            }
            return;
        }

        StatisticsManager stats = settings.deathCooldownEnabled() ? StatisticsManager.get(level) : null;
        for (ServerPlayer player : level.players()) {
            if (!player.isSpectator() && !player.isCreative()) {
                attemptSpawnZombie(level, player, effectiveChance, eventState, stats, settings, gameTime);
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

    private static void attemptSpawnZombie(
            ServerLevel level,
            ServerPlayer player,
            double baseChance,
            HordeManager.EventState eventState,
            StatisticsManager stats,
            SpawnRuntimeSettings settings,
            long gameTime) {
        RandomSource random = level.getRandom();
        double effectiveChance = baseChance;

        if (stats != null) {
            effectiveChance *= stats.getSpawnFactor(player.getUUID(), gameTime);
        }

        if (effectiveChance <= 0.0 || random.nextDouble() >= effectiveChance) {
            return;
        }

        int nearbyZombies = countNearbyZombies(level, player, settings.horizontalRange());
        int maxZombies = settings.maxZombiesPerPlayer();

        if (nearbyZombies >= maxZombies) {
            if (settings.debugLogging()) {
                LOGGER.debug("[ZombieApocalypse] Skip spawn near {} because nearby={}/{}",
                        player.getGameProfile().getName(), nearbyZombies, maxZombies);
            }
            return;
        }

        int countToSpawn = computeSpawnQuota(nearbyZombies, maxZombies, eventState.zombiesPerSpawn());
        if (countToSpawn <= 0) {
            return;
        }

        int maxAttempts = settings.maxAttemptsForWave(countToSpawn);
        int horizontalRange = settings.horizontalRange();
        int minDistanceSq = settings.minDistanceSquared();
        BlockPos playerPos = player.blockPosition();
        int playerX = playerPos.getX();
        int playerY = playerPos.getY();
        int playerZ = playerPos.getZ();
        BlockPos.MutableBlockPos spawnPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos scratchPos = new BlockPos.MutableBlockPos();

        int spawned = 0;
        for (int i = 0; i < maxAttempts && spawned < countToSpawn; i++) {
            int x = playerX + random.nextInt(horizontalRange * 2 + 1) - horizontalRange;
            int z = playerZ + random.nextInt(horizontalRange * 2 + 1) - horizontalRange;
            int y = chooseSpawnY(level, playerY, random, x, z, scratchPos);
            spawnPos.set(x, y, z);

            if (horizontalDistanceSquared(spawnPos, playerPos) < minDistanceSq) {
                continue;
            }

            if (settings.requireOpenSky() && !level.canSeeSky(spawnPos)) {
                continue;
            }

            if (!canSpawnInBiome(level, spawnPos, settings)) {
                continue;
            }

            if (!isValidSpawnSpace(level, spawnPos, scratchPos)) {
                continue;
            }

            if (!isAllowedByBlockLight(level, spawnPos, settings.maxBlockLightForSpawning())) {
                continue;
            }

            Zombie zombie = createZombie(level, spawnPos, random, settings);
            if (zombie == null) {
                continue;
            }

            zombie.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                    random.nextFloat() * 360.0F, 0.0F);

            boolean spawnAsBaby = random.nextDouble() < settings.babyZombieChance();
            zombie.setBaby(spawnAsBaby);

            if (!level.noCollision(zombie)) {
                continue;
            }

            Zombie.ZombieGroupData spawnGroupData = new Zombie.ZombieGroupData(spawnAsBaby, spawnAsBaby);
            zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, spawnGroupData);
            zombie.setBaby(spawnAsBaby);
            DifficultyManager.applyScaling(zombie, level, spawnPos);
            level.addFreshEntity(zombie);
            spawned++;

            if (settings.spawnEffectsEnabled()) {
                playSpawnEffects(level, spawnPos, settings);
            }

            if (settings.debugLogging()) {
                LOGGER.debug("[ZombieApocalypse] Spawned {} at {} for {}",
                        zombie.getType().getDescriptionId(),
                        spawnPos,
                        player.getGameProfile().getName());
            }
        }

        if (settings.debugLogging() && spawned > 0) {
            LOGGER.info("[ZombieApocalypse] Spawned {}/{} near {}",
                    spawned,
                    countToSpawn,
                    player.getGameProfile().getName());
        }
    }

    private static int chooseSpawnY(
            ServerLevel level,
            int playerY,
            RandomSource random,
            int x,
            int z,
            BlockPos.MutableBlockPos scratchPos) {
        if (level.dimension() == Level.NETHER) {
            int minY = Math.max(level.getMinBuildHeight() + 1, playerY - 16);
            int maxY = Math.min(level.getMaxBuildHeight() - 2, playerY + 16);
            if (minY >= maxY) {
                return minY;
            }

            int startY = Mth.nextInt(random, minY, maxY);

            for (int y = startY; y >= minY; y--) {
                scratchPos.set(x, y, z);
                if (level.getBlockState(scratchPos).isAir()) {
                    scratchPos.set(x, y + 1, z);
                    if (!level.getBlockState(scratchPos).isAir()) {
                        continue;
                    }

                    scratchPos.set(x, y - 1, z);
                    if (level.getBlockState(scratchPos).isFaceSturdy(level, scratchPos, Direction.UP)) {
                        return y;
                    }
                }
            }

            return startY;
        }

        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    private static boolean isValidSpawnSpace(ServerLevel level, BlockPos spawnPos, BlockPos.MutableBlockPos scratchPos) {
        if (!level.getWorldBorder().isWithinBounds(spawnPos)) {
            return false;
        }

        scratchPos.set(spawnPos.getX(), spawnPos.getY() - 1, spawnPos.getZ());
        if (!level.getBlockState(scratchPos).isFaceSturdy(level, scratchPos, Direction.UP)) {
            return false;
        }

        if (!level.getBlockState(spawnPos).isAir()) {
            return false;
        }

        scratchPos.set(spawnPos.getX(), spawnPos.getY() + 1, spawnPos.getZ());
        if (!level.getBlockState(scratchPos).isAir()) {
            return false;
        }

        if (!level.getFluidState(spawnPos).isEmpty() || !level.getFluidState(scratchPos).isEmpty()) {
            return false;
        }

        return true;
    }

    private static boolean isAllowedByBlockLight(ServerLevel level, BlockPos spawnPos, int maxBlockLightForSpawning) {
        return isBlockLightSpawnAllowed(
                level.getBrightness(LightLayer.BLOCK, spawnPos),
                maxBlockLightForSpawning);
    }

    private static int countNearbyZombies(ServerLevel level, ServerPlayer player, int range) {
        return level.getEntitiesOfClass(
                Zombie.class,
                player.getBoundingBox().inflate(range),
                zombie -> zombie.isAlive() && ZombieClassMobs.isZombieClass(zombie)).size();
    }

    private static boolean canSpawnInBiome(ServerLevel level, BlockPos pos, SpawnRuntimeSettings settings) {
        if (!settings.biomeModifiersEnabled()) {
            return true;
        }

        Holder<Biome> biomeHolder = level.getBiome(pos);
        if (settings.mushroomSafeZone() && biomeHolder.is(Biomes.MUSHROOM_FIELDS)) {
            return false;
        }

        return true;
    }

    static int computeSpawnQuota(int nearbyZombies, int maxZombies, int requestedSpawns) {
        int availableCapacity = Math.max(0, maxZombies - nearbyZombies);
        return Math.min(Math.max(1, requestedSpawns), availableCapacity);
    }

    static boolean isDaylightSpawnBlocked(boolean isDay, long currentDay, int daylightSpawnStartDay, boolean hordeActive) {
        return isDay && !hordeActive && currentDay < Math.max(0, daylightSpawnStartDay);
    }

    static boolean isSpawnDistanceImpossible(int minDistance, int horizontalRange) {
        long safeRange = Math.max(1, horizontalRange);
        long safeMinDistance = Math.max(0, minDistance);
        return safeMinDistance * safeMinDistance > 2L * safeRange * safeRange;
    }

    static boolean isBlockLightSpawnAllowed(int blockLight, int maxBlockLightForSpawning) {
        if (maxBlockLightForSpawning < 0) {
            return true;
        }

        int cappedBlockLight = Mth.clamp(blockLight, 0, 15);
        int cappedMaxLight = Mth.clamp(maxBlockLightForSpawning, 0, 15);
        return cappedBlockLight <= cappedMaxLight;
    }

    static long horizontalDistanceSquared(BlockPos first, BlockPos second) {
        long dx = (long) first.getX() - second.getX();
        long dz = (long) first.getZ() - second.getZ();
        return dx * dx + dz * dz;
    }

    static boolean shouldForceAdultZombie(boolean isBaby, double babyZombieChance) {
        return isBaby && ConfigValidator.probability(babyZombieChance) <= 0.0;
    }

    private static void forceAdultZombie(Zombie zombie) {
        zombie.setBaby(false);
        if (zombie.isPassenger()) {
            zombie.stopRiding();
        }
    }

    private static boolean isDaylightSpawnBlocked(ServerLevel level, boolean hordeActive) {
        return isDaylightSpawnBlocked(
                level.isDay(),
                DifficultyManager.getCurrentDay(level),
                Config.COMMON.daylightSpawnStartDay.get(),
                hordeActive);
    }

    private static Zombie createZombie(ServerLevel level, BlockPos pos, RandomSource random, SpawnRuntimeSettings settings) {
        if (!settings.variantsEnabled()) {
            return net.minecraft.world.entity.EntityType.ZOMBIE.create(level);
        }

        Holder<Biome> biomeHolder = level.getBiome(pos);
        boolean desertBiome = ConfigValidator.isDesertStyleBiome(biomeHolder);
        boolean waterBiome = ConfigValidator.isWaterStyleBiome(biomeHolder);

        SpawnMath.VariantWeights weights = settings.variantWeights(desertBiome, waterBiome);
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

    private static void playSpawnEffects(ServerLevel level, BlockPos pos, SpawnRuntimeSettings settings) {
        if (settings.spawnSoundEnabled()) {
            level.playSound(null, pos, SoundEvents.ZOMBIE_AMBIENT, SoundSource.HOSTILE, 1.0F, 1.0F);
        }

        if (settings.spawnParticlesEnabled()) {
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

    private static boolean isNaturalSunFireDamage(Zombie zombie) {
        return isLikelySunBurnContext(zombie) && !hasRecentExternalFire(zombie, zombie.level().getGameTime());
    }

    private static void rememberExternalFire(Zombie zombie, long gameTime) {
        EXTERNAL_FIRE_UNTIL.put(zombie.getUUID(), gameTime + EXTERNAL_FIRE_GRACE_TICKS);
    }

    private static boolean hasRecentExternalFire(Zombie zombie, long gameTime) {
        Long until = EXTERNAL_FIRE_UNTIL.get(zombie.getUUID());
        if (until == null) {
            return false;
        }

        if (gameTime >= until) {
            EXTERNAL_FIRE_UNTIL.remove(zombie.getUUID());
            return false;
        }

        return true;
    }

    private static void clearExpiredExternalFire(Zombie zombie) {
        if (!EXTERNAL_FIRE_UNTIL.isEmpty()) {
            EXTERNAL_FIRE_UNTIL.remove(zombie.getUUID());
        }
    }

    static void pruneExpiredExternalFire(long gameTime) {
        if (EXTERNAL_FIRE_UNTIL.isEmpty()) {
            return;
        }
        EXTERNAL_FIRE_UNTIL.entrySet().removeIf(entry -> gameTime >= entry.getValue());
    }
}
