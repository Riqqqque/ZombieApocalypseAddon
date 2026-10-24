package com.rique.zombieapocalypse.commands;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.ConfigGuard;

public enum GameplayPreset {

    CASUAL(
            "Casual",
            "A slower start, fewer zombies, no baby zombies, and torch-protected bases.",
            180, 0.25, 1, 30, 10, 0, 0.0,
            7, 0.25, 6, 2.0, 3, 30,
            0.05, 3.0, 2,
            10, 75, 0.10, 6, 0.15, 0.10,
            1.25),

    STANDARD(
            "Standard",
            "The recommended balance for most survival servers. A fresh install starts here.",
            120, 0.50, 2, 50, 1, 0, 0.05,
            5, 0.50, 10, 3.0, 5, 20,
            0.15, 5.0, 4,
            3, 50, 0.20, 10, 0.30, 0.20,
            1.50),

    HARDCORE(
            "Hardcore",
            "More frequent waves, stronger events, and faster difficulty growth.",
            80, 0.75, 3, 80, 0, -1, 0.08,
            3, 0.75, 12, 4.0, 8, 15,
            0.25, 6.0, 6,
            1, 40, 0.30, 16, 0.45, 0.35,
            2.00);

    private static final int SAFE_SPAWN_RANGE = 30;
    private static final int SAFE_MIN_SPAWN_DISTANCE = 12;
    private static final int SAFE_SPAWN_ATTEMPTS = 10;

    private final String displayName;
    private final String description;
    private final int spawnInterval;
    private final double spawnChance;
    private final int waveSize;
    private final int nearbyCap;
    private final int daylightStartDay;
    private final int maxBlockLight;
    private final double babyChance;
    private final int hordeIntervalDays;
    private final double hordeChance;
    private final int hordeDurationMinutes;
    private final double hordeMultiplier;
    private final int hordeWaveSize;
    private final int eventSpawnInterval;
    private final double bloodMoonChance;
    private final double bloodMoonMultiplier;
    private final int bloodMoonWaveSize;
    private final int scalingStartDay;
    private final int maxScalingDay;
    private final double maxSpeedBoost;
    private final int maxHealthBoost;
    private final double maxArmorChance;
    private final double maxWeaponChance;
    private final double nightSpawnMultiplier;

    GameplayPreset(
            String displayName,
            String description,
            int spawnInterval,
            double spawnChance,
            int waveSize,
            int nearbyCap,
            int daylightStartDay,
            int maxBlockLight,
            double babyChance,
            int hordeIntervalDays,
            double hordeChance,
            int hordeDurationMinutes,
            double hordeMultiplier,
            int hordeWaveSize,
            int eventSpawnInterval,
            double bloodMoonChance,
            double bloodMoonMultiplier,
            int bloodMoonWaveSize,
            int scalingStartDay,
            int maxScalingDay,
            double maxSpeedBoost,
            int maxHealthBoost,
            double maxArmorChance,
            double maxWeaponChance,
            double nightSpawnMultiplier) {
        this.displayName = displayName;
        this.description = description;
        this.spawnInterval = spawnInterval;
        this.spawnChance = spawnChance;
        this.waveSize = waveSize;
        this.nearbyCap = nearbyCap;
        this.daylightStartDay = daylightStartDay;
        this.maxBlockLight = maxBlockLight;
        this.babyChance = babyChance;
        this.hordeIntervalDays = hordeIntervalDays;
        this.hordeChance = hordeChance;
        this.hordeDurationMinutes = hordeDurationMinutes;
        this.hordeMultiplier = hordeMultiplier;
        this.hordeWaveSize = hordeWaveSize;
        this.eventSpawnInterval = eventSpawnInterval;
        this.bloodMoonChance = bloodMoonChance;
        this.bloodMoonMultiplier = bloodMoonMultiplier;
        this.bloodMoonWaveSize = bloodMoonWaveSize;
        this.scalingStartDay = scalingStartDay;
        this.maxScalingDay = maxScalingDay;
        this.maxSpeedBoost = maxSpeedBoost;
        this.maxHealthBoost = maxHealthBoost;
        this.maxArmorChance = maxArmorChance;
        this.maxWeaponChance = maxWeaponChance;
        this.nightSpawnMultiplier = nightSpawnMultiplier;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    /** Every setting this preset controls, keyed by config path. A fresh install matches STANDARD. */
    Map<List<String>, Object> settings() {
        Config.Common config = Config.COMMON;
        Map<List<String>, Object> values = new LinkedHashMap<>();
        values.put(config.enableDaySpawning.getPath(), true);
        values.put(config.enableDaytimeSpawning.getPath(), true);
        values.put(config.enableZombieVariants.getPath(), true);
        values.put(config.enableNightBoost.getPath(), true);
        values.put(config.enableHordeEvents.getPath(), true);
        values.put(config.enableBloodMoon.getPath(), true);
        values.put(config.enableDifficultyScaling.getPath(), true);

        values.put(config.daySpawnInterval.getPath(), spawnInterval);
        values.put(config.daySpawnChance.getPath(), spawnChance);
        values.put(config.zombiesPerSpawn.getPath(), waveSize);
        values.put(config.maxDayZombiesPerPlayer.getPath(), nearbyCap);
        values.put(config.spawnRange.getPath(), SAFE_SPAWN_RANGE);
        values.put(config.minSpawnDistance.getPath(), SAFE_MIN_SPAWN_DISTANCE);
        values.put(config.spawnAttemptsPerZombie.getPath(), SAFE_SPAWN_ATTEMPTS);
        values.put(config.requireOpenSkyForOverworldSpawns.getPath(), true);
        values.put(config.daylightSpawnStartDay.getPath(), daylightStartDay);
        values.put(config.maxBlockLightForSpawning.getPath(), maxBlockLight);
        values.put(config.babyZombieChance.getPath(), babyChance);

        values.put(config.hordeStartsAtDusk.getPath(), true);
        values.put(config.hordeIntervalDays.getPath(), hordeIntervalDays);
        values.put(config.hordeStartChance.getPath(), hordeChance);
        values.put(config.hordeDurationMinutes.getPath(), hordeDurationMinutes);
        values.put(config.hordeSpawnMultiplier.getPath(), hordeMultiplier);
        values.put(config.hordeZombiesPerSpawn.getPath(), hordeWaveSize);
        values.put(config.eventSpawnInterval.getPath(), eventSpawnInterval);

        values.put(config.bloodMoonChance.getPath(), bloodMoonChance);
        values.put(config.bloodMoonSpawnMultiplier.getPath(), bloodMoonMultiplier);
        values.put(config.bloodMoonZombiesPerSpawn.getPath(), bloodMoonWaveSize);

        values.put(config.scalingStartDay.getPath(), scalingStartDay);
        values.put(config.maxScalingDay.getPath(), maxScalingDay);
        values.put(config.maxSpeedBoost.getPath(), maxSpeedBoost);
        values.put(config.maxHealthBoost.getPath(), maxHealthBoost);
        values.put(config.maxArmorChance.getPath(), maxArmorChance);
        values.put(config.maxWeaponChance.getPath(), maxWeaponChance);
        values.put(config.nightSpawnMultiplier.getPath(), nightSpawnMultiplier);
        return values;
    }

    public void apply() {
        Config.setAll(settings());
    }

    public boolean matchesCurrentSettings() {
        Map<List<String>, Object> current = Config.currentValues();
        for (Map.Entry<List<String>, Object> entry : settings().entrySet()) {
            if (!ConfigGuard.sameValue(current.get(entry.getKey()), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    public static String currentName() {
        for (GameplayPreset preset : values()) {
            if (preset.matchesCurrentSettings()) {
                return preset.displayName;
            }
        }
        return "Custom";
    }
}
