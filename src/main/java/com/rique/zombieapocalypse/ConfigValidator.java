package com.rique.zombieapocalypse;

import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * Reads config values and applies small safety rules/invariants.
 */
public final class ConfigValidator {

    private ConfigValidator() {
    }

    public static double probability(double value) {
        return SpawnMath.clampProbability(value);
    }

    public static SpawnMath.VariantWeights baseVariantWeights() {
        if (!Config.COMMON.enableZombieVariants.get()) {
            return SpawnMath.normalizeVariantWeights(0.0, 0.0, 0.0);
        }

        return SpawnMath.normalizeVariantWeights(
                Config.COMMON.zombieVillagerChance.get(),
                Config.COMMON.huskChance.get(),
                Config.COMMON.drownedChance.get());
    }

    public static SpawnMath.VariantWeights biomeAdjustedVariantWeights(boolean desertBiome, boolean waterBiome) {
        return biomeAdjustedVariantWeights(
                baseVariantWeights(),
                Config.COMMON.enableZombieVariants.get(),
                Config.COMMON.enableBiomeModifiers.get(),
                Config.COMMON.desertHuskBonus.get(),
                Config.COMMON.waterDrownedBonus.get(),
                desertBiome,
                waterBiome);
    }

    static SpawnMath.VariantWeights biomeAdjustedVariantWeights(
            SpawnMath.VariantWeights base,
            boolean variantsEnabled,
            boolean biomeModifiersEnabled,
            double desertHuskBonus,
            double waterDrownedBonus,
            boolean desertBiome,
            boolean waterBiome) {
        if (!variantsEnabled) {
            return base;
        }

        double husk = base.huskChance();
        double drowned = base.drownedChance();

        if (biomeModifiersEnabled) {
            if (desertBiome) {
                husk += probability(desertHuskBonus);
            }
            if (waterBiome) {
                drowned += probability(waterDrownedBonus);
            }
        }

        return SpawnMath.normalizeVariantWeights(base.zombieVillagerChance(), husk, drowned);
    }

    public static boolean isDesertStyleBiome(Holder<Biome> biomeHolder) {
        return biomeHolder.is(Biomes.DESERT) || biomeHolder.is(BiomeTags.IS_BADLANDS);
    }

    public static boolean isWaterStyleBiome(Holder<Biome> biomeHolder) {
        return biomeHolder.is(BiomeTags.IS_OCEAN)
                || biomeHolder.is(BiomeTags.IS_RIVER)
                || biomeHolder.is(Biomes.SWAMP)
                || biomeHolder.is(Biomes.MANGROVE_SWAMP);
    }

    public static int spawnIntervalTicks(boolean eventActive) {
        int configured = eventActive
                ? Config.COMMON.eventSpawnInterval.get()
                : Config.COMMON.daySpawnInterval.get();
        return Math.max(1, configured);
    }

    public static int spawnAttemptsForWave(int zombiesPerSpawn) {
        int attemptsPerZombie = Math.max(1, Config.COMMON.spawnAttemptsPerZombie.get());
        return Math.max(zombiesPerSpawn, zombiesPerSpawn * attemptsPerZombie);
    }
}
