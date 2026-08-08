package com.rique.zombieapocalypse.commands;

import com.rique.zombieapocalypse.Config;

public enum GameplayPreset {

    CASUAL(
            "Casual",
            "A slower start, fewer zombies, no baby zombies, and torch-protected bases.",
            180, 0.25, 1, 30, 10, 7, 0.0,
            7, 0.25, 4, 2.0, 3, 30,
            0.05, 3.0, 2,
            10, 75, 0.10, 6, 0.15, 0.10,
            1.25),

    STANDARD(
            "Standard",
            "The recommended default balance for most survival servers.",
            120, 0.50, 2, 50, 0, -1, 0.05,
            5, 0.50, 5, 3.0, 5, 20,
            0.15, 5.0, 4,
            3, 50, 0.20, 10, 0.30, 0.20,
            1.50),

    HARDCORE(
            "Hardcore",
            "More frequent waves, stronger events, and faster difficulty growth.",
            80, 0.75, 3, 80, 0, -1, 0.08,
            3, 0.75, 8, 4.0, 8, 15,
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

    public void apply() {
        Config.Common config = Config.COMMON;
        Config.edit(() -> {
            Config.set(config.enableDaySpawning, true);
            Config.set(config.enableZombieVariants, true);
            Config.set(config.enableNightBoost, true);
            Config.set(config.enableHordeEvents, true);
            Config.set(config.enableBloodMoon, true);
            Config.set(config.enableDifficultyScaling, true);

            Config.set(config.daySpawnInterval, spawnInterval);
            Config.set(config.daySpawnChance, spawnChance);
            Config.set(config.zombiesPerSpawn, waveSize);
            Config.set(config.maxDayZombiesPerPlayer, nearbyCap);
            Config.set(config.spawnRange, SAFE_SPAWN_RANGE);
            Config.set(config.minSpawnDistance, SAFE_MIN_SPAWN_DISTANCE);
            Config.set(config.spawnAttemptsPerZombie, SAFE_SPAWN_ATTEMPTS);
            Config.set(config.requireOpenSkyForOverworldSpawns, true);
            Config.set(config.daylightSpawnStartDay, daylightStartDay);
            Config.set(config.maxBlockLightForSpawning, maxBlockLight);
            Config.set(config.babyZombieChance, babyChance);

            Config.set(config.hordeIntervalDays, hordeIntervalDays);
            Config.set(config.hordeStartChance, hordeChance);
            Config.set(config.hordeDurationMinutes, hordeDurationMinutes);
            Config.set(config.hordeSpawnMultiplier, hordeMultiplier);
            Config.set(config.hordeZombiesPerSpawn, hordeWaveSize);
            Config.set(config.eventSpawnInterval, eventSpawnInterval);

            Config.set(config.bloodMoonChance, bloodMoonChance);
            Config.set(config.bloodMoonSpawnMultiplier, bloodMoonMultiplier);
            Config.set(config.bloodMoonZombiesPerSpawn, bloodMoonWaveSize);

            Config.set(config.scalingStartDay, scalingStartDay);
            Config.set(config.maxScalingDay, maxScalingDay);
            Config.set(config.maxSpeedBoost, maxSpeedBoost);
            Config.set(config.maxHealthBoost, maxHealthBoost);
            Config.set(config.maxArmorChance, maxArmorChance);
            Config.set(config.maxWeaponChance, maxWeaponChance);
            Config.set(config.nightSpawnMultiplier, nightSpawnMultiplier);
        });
    }

    public boolean matchesCurrentSettings() {
        Config.Common config = Config.COMMON;
        return config.enableDaySpawning.get()
                && config.enableZombieVariants.get()
                && config.enableNightBoost.get()
                && config.enableHordeEvents.get()
                && config.enableBloodMoon.get()
                && config.enableDifficultyScaling.get()
                && config.daySpawnInterval.get() == spawnInterval
                && same(config.daySpawnChance.get(), spawnChance)
                && config.zombiesPerSpawn.get() == waveSize
                && config.maxDayZombiesPerPlayer.get() == nearbyCap
                && config.spawnRange.get() == SAFE_SPAWN_RANGE
                && config.minSpawnDistance.get() == SAFE_MIN_SPAWN_DISTANCE
                && config.spawnAttemptsPerZombie.get() == SAFE_SPAWN_ATTEMPTS
                && config.requireOpenSkyForOverworldSpawns.get()
                && config.daylightSpawnStartDay.get() == daylightStartDay
                && config.maxBlockLightForSpawning.get() == maxBlockLight
                && same(config.babyZombieChance.get(), babyChance)
                && config.hordeIntervalDays.get() == hordeIntervalDays
                && same(config.hordeStartChance.get(), hordeChance)
                && config.hordeDurationMinutes.get() == hordeDurationMinutes
                && same(config.hordeSpawnMultiplier.get(), hordeMultiplier)
                && config.hordeZombiesPerSpawn.get() == hordeWaveSize
                && config.eventSpawnInterval.get() == eventSpawnInterval
                && same(config.bloodMoonChance.get(), bloodMoonChance)
                && same(config.bloodMoonSpawnMultiplier.get(), bloodMoonMultiplier)
                && config.bloodMoonZombiesPerSpawn.get() == bloodMoonWaveSize
                && config.scalingStartDay.get() == scalingStartDay
                && config.maxScalingDay.get() == maxScalingDay
                && same(config.maxSpeedBoost.get(), maxSpeedBoost)
                && config.maxHealthBoost.get() == maxHealthBoost
                && same(config.maxArmorChance.get(), maxArmorChance)
                && same(config.maxWeaponChance.get(), maxWeaponChance)
                && same(config.nightSpawnMultiplier.get(), nightSpawnMultiplier);
    }

    public static String currentName() {
        for (GameplayPreset preset : values()) {
            if (preset.matchesCurrentSettings()) {
                return preset.displayName;
            }
        }
        return "Custom";
    }

    private static boolean same(double actual, double expected) {
        return Double.compare(actual, expected) == 0;
    }
}
