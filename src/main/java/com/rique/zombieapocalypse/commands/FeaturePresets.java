package com.rique.zombieapocalypse.commands;

import com.rique.zombieapocalypse.Config;

final class FeaturePresets {

    private FeaturePresets() {
    }

    static void enableSpawning() {
        Config.Common config = Config.COMMON;
        Config.edit(() -> {
            applySpawnBaseline(config, true);
            Config.set(config.enableZombieVariants, true);
            Config.set(config.babyZombieChance, 0.05);
            Config.set(config.enableNightBoost, true);
            Config.set(config.nightSpawnMultiplier, 1.50);
        });
    }

    static void enableHordes() {
        Config.Common config = Config.COMMON;
        Config.edit(() -> {
            applySpawnBaseline(config, true);
            Config.set(config.enableHordeEvents, true);
            Config.set(config.hordeIntervalDays, 5);
            Config.set(config.hordeStartChance, 0.50);
            Config.set(config.hordeDurationMinutes, 5);
            Config.set(config.hordeSpawnMultiplier, 3.0);
            Config.set(config.hordeZombiesPerSpawn, 5);
            Config.set(config.eventSpawnInterval, 20);
            Config.set(config.enableEventNotifications, true);
        });
    }

    static void enableVariants() {
        Config.Common config = Config.COMMON;
        Config.edit(() -> {
            Config.set(config.enableZombieVariants, true);
            Config.set(config.huskChance, 0.15);
            Config.set(config.drownedChance, 0.10);
            Config.set(config.babyZombieChance, 0.05);
            Config.set(config.zombieVillagerChance, 0.05);
        });
    }

    static void enableNightBoost() {
        Config.edit(() -> {
            Config.set(Config.COMMON.enableNightBoost, true);
            Config.set(Config.COMMON.nightSpawnMultiplier, 1.50);
        });
    }

    static void enableBloodMoons() {
        Config.Common config = Config.COMMON;
        Config.edit(() -> {
            applySpawnBaseline(config, false);
            Config.set(config.enableBloodMoon, true);
            Config.set(config.bloodMoonChance, 0.15);
            Config.set(config.bloodMoonSpawnMultiplier, 5.0);
            Config.set(config.bloodMoonZombiesPerSpawn, 4);
            Config.set(config.eventSpawnInterval, 20);
            Config.set(config.enableEventNotifications, true);
        });
    }

    static void enableScaling() {
        Config.Common config = Config.COMMON;
        Config.edit(() -> {
            Config.set(config.enableDifficultyScaling, true);
            Config.set(config.scalingStartDay, 3);
            Config.set(config.maxScalingDay, 50);
            Config.set(config.maxSpeedBoost, 0.20);
            Config.set(config.maxHealthBoost, 10);
            Config.set(config.maxArmorChance, 0.30);
            Config.set(config.maxWeaponChance, 0.20);
        });
    }

    static void enableBlockBreaking() {
        Config.Common config = Config.COMMON;
        Config.edit(() -> {
            Config.set(config.enableZombieBlockBreaking, true);
            Config.set(config.zombieBlockBreakingStartDay, 0);
            Config.set(config.zombieBlockBreakingInterval, 100);
            Config.set(config.zombieBlockBreakingChance, 0.20);
            Config.set(config.zombieBlockBreakingRange, 1);
            Config.set(config.zombieBlockBreakingMaxHardness, 3.0);
            Config.set(config.zombieBlockBreakingDropBlocks, false);
            Config.set(config.zombieBlockBreakingRequireTarget, true);
            Config.set(config.zombieBlockBreakingRequireObstacle, true);
            Config.set(config.zombieBlockBreakingRespectMobGriefing, true);
            Config.set(config.zombieBlockBreakingAllowBlockEntities, false);
            Config.set(config.zombieBlockBreakingAllowToolRequiredBlocks, false);
            Config.set(config.zombieBlockBreakingAllowLightBlocks, false);
        });
    }

    static void enableBlockPlacing() {
        Config.Common config = Config.COMMON;
        Config.edit(() -> {
            Config.set(config.enableZombieBlockPlacing, true);
            Config.set(config.zombieBlockPlacingStartDay, 0);
            Config.set(config.zombieBlockPlacingInterval, 100);
            Config.set(config.zombieBlockPlacingChance, 0.15);
            Config.set(config.zombieBlockPlacingBlock, "minecraft:cobblestone");
            Config.set(config.zombieBlockPlacingMaxPerZombie, 8);
            Config.set(config.zombieBlockPlacingMaxTargetDistance, 32);
            Config.set(config.zombieBlockPlacingRequireTarget, true);
            Config.set(config.zombieBlockPlacingRequireObstacle, true);
            Config.set(config.zombieBlockPlacingRespectMobGriefing, true);
            Config.set(config.zombieBlockPlacingAllowBridges, true);
            Config.set(config.zombieBlockPlacingAllowSteps, true);
            Config.set(config.zombieBlockPlacingReplaceFluids, false);
            Config.set(config.zombieBlockPlacingReplaceReplaceableBlocks, false);
        });
    }

    static void enableTowering() {
        Config.Common config = Config.COMMON;
        Config.edit(() -> {
            Config.set(config.enableZombieTowering, true);
            Config.set(config.zombieToweringStartDay, 0);
            Config.set(config.zombieToweringInterval, 20);
            Config.set(config.zombieToweringChance, 0.45);
            Config.set(config.zombieToweringMaxTargetDistance, 32);
            Config.set(config.zombieToweringMinNearbyZombies, 2);
            Config.set(config.zombieToweringCrowdRadius, 2.25);
            Config.set(config.zombieToweringMaxStackSize, 4);
            Config.set(config.zombieToweringDismountDistance, 2.75);
            Config.set(config.zombieToweringVerticalBoost, 0.48);
            Config.set(config.zombieToweringForwardBoost, 0.18);
            Config.set(config.zombieToweringMaxHeightAboveTarget, 8);
            Config.set(config.zombieToweringRequireObstacle, true);
        });
    }

    static void enableAttributes() {
        Config.Common config = Config.COMMON;
        Config.edit(() -> {
            Config.set(config.enableAttributeModifiers, true);
            Config.set(config.enableDifficultyScaling, true);
            Config.set(config.scaleAttributesWithDifficulty, true);
            Config.set(config.enableVariantAttributeProfiles, true);
            Config.set(config.enableBiomeDimensionAttributeMultipliers, true);
            Config.set(config.scalingStartDay, 3);
            Config.set(config.maxScalingDay, 50);
            Config.set(config.maxSpeedBoost, 0.20);
            Config.set(config.maxHealthBoost, 10);
            Config.set(config.maxArmorChance, 0.30);
            Config.set(config.maxWeaponChance, 0.20);
            Config.set(config.maxHealthScaleMultiplier, 0.25);
            Config.set(config.maxHealthScaleBonus, 0.0);
            Config.set(config.maxAttackScaleMultiplier, 0.15);
            Config.set(config.maxAttackScaleBonus, 0.0);
            Config.set(config.maxSpeedScaleMultiplier, 0.0);
            Config.set(config.maxSpeedScaleBonus, 0.0);
            Config.set(config.maxArmorScaleMultiplier, 0.15);
            Config.set(config.maxArmorScaleBonus, 0.0);
            Config.set(config.maxFollowRangeScaleMultiplier, 0.25);
            Config.set(config.maxFollowRangeScaleBonus, 0.0);
            Config.set(config.maxKnockbackResistanceScaleMultiplier, 0.10);
            Config.set(config.maxKnockbackResistanceScaleBonus, 0.0);
        });
    }

    static void enableCompatibility() {
        Config.Common config = Config.COMMON;
        Config.edit(() -> {
            Config.set(config.enableModdedZombieCompatibility, true);
            Config.set(config.applyDifficultyToModdedZombies, true);
            Config.set(config.applyAiFeaturesToModdedZombies, true);
            Config.set(config.respectExternalSpawnRules, true);
            Config.set(config.respectExternalZombieAi, true);
            Config.set(config.respectExternalDifficulty, true);
            Config.set(config.respectZombieDoorBreakingAbility, true);
            Config.set(config.preserveExistingZombieEquipment, true);
        });
    }

    static void enableBiomeModifiers() {
        Config.Common config = Config.COMMON;
        Config.edit(() -> {
            Config.set(config.enableBiomeModifiers, true);
            Config.set(config.desertHuskBonus, 0.50);
            Config.set(config.waterDrownedBonus, 0.40);
            Config.set(config.mushroomSafeZone, true);
        });
    }

    static void enableDeathCooldown() {
        Config.Common config = Config.COMMON;
        Config.edit(() -> {
            Config.set(config.enableDeathCooldown, true);
            Config.set(config.deathCooldownSeconds, 30);
            Config.set(config.cooldownSpawnReduction, 0.50);
        });
    }

    static void enableSpawnEffects() {
        Config.Common config = Config.COMMON;
        Config.edit(() -> {
            Config.set(config.enableSpawnEffects, true);
            Config.set(config.spawnSound, true);
            Config.set(config.spawnParticles, true);
        });
    }

    private static void applySpawnBaseline(Config.Common config, boolean enableDaytime) {
        Config.set(config.enableDaySpawning, true);
        Config.set(config.daySpawnInterval, 120);
        Config.set(config.daySpawnChance, 0.50);
        Config.set(config.maxDayZombiesPerPlayer, 50);
        Config.set(config.zombiesPerSpawn, 2);
        Config.set(config.spawnRange, 30);
        Config.set(config.minSpawnDistance, 12);
        Config.set(config.spawnAttemptsPerZombie, 10);
        Config.set(config.requireOpenSkyForOverworldSpawns, true);
        Config.set(config.maxBlockLightForSpawning, -1);
        if (enableDaytime) {
            Config.set(config.enableDaytimeSpawning, true);
            Config.set(config.daylightSpawnStartDay, 0);
        }
    }
}
