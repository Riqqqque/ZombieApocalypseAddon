package com.rique.zombieapocalypse;

import org.apache.commons.lang3.tuple.Pair;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {

    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    private Config() {
    }

    public static final class Common {

        // General
        public final ModConfigSpec.BooleanValue preventSunBurn;
        public final ModConfigSpec.BooleanValue enableExtraDrops;
        public final ModConfigSpec.BooleanValue enableDebugLogging;

        // Day spawning core
        public final ModConfigSpec.BooleanValue enableDaySpawning;
        public final ModConfigSpec.IntValue daySpawnInterval;
        public final ModConfigSpec.DoubleValue daySpawnChance;
        public final ModConfigSpec.IntValue maxDayZombiesPerPlayer;
        public final ModConfigSpec.IntValue zombiesPerSpawn;
        public final ModConfigSpec.IntValue spawnRange;
        public final ModConfigSpec.IntValue minSpawnDistance;
        public final ModConfigSpec.IntValue spawnAttemptsPerZombie;
        public final ModConfigSpec.BooleanValue requireOpenSkyForOverworldSpawns;

        // Zombie variants
        public final ModConfigSpec.BooleanValue enableZombieVariants;
        public final ModConfigSpec.DoubleValue huskChance;
        public final ModConfigSpec.DoubleValue drownedChance;
        public final ModConfigSpec.DoubleValue babyZombieChance;
        public final ModConfigSpec.DoubleValue zombieVillagerChance;

        // Horde events
        public final ModConfigSpec.BooleanValue enableHordeEvents;
        public final ModConfigSpec.IntValue hordeIntervalDays;
        public final ModConfigSpec.DoubleValue hordeStartChance;
        public final ModConfigSpec.IntValue hordeDurationMinutes;
        public final ModConfigSpec.DoubleValue hordeSpawnMultiplier;
        public final ModConfigSpec.IntValue hordeZombiesPerSpawn;
        public final ModConfigSpec.IntValue eventSpawnInterval;
        public final ModConfigSpec.BooleanValue enableEventNotifications;

        // Blood moon
        public final ModConfigSpec.BooleanValue enableBloodMoon;
        public final ModConfigSpec.DoubleValue bloodMoonChance;
        public final ModConfigSpec.DoubleValue bloodMoonSpawnMultiplier;
        public final ModConfigSpec.IntValue bloodMoonZombiesPerSpawn;

        // Difficulty scaling
        public final ModConfigSpec.BooleanValue enableDifficultyScaling;
        public final ModConfigSpec.IntValue scalingStartDay;
        public final ModConfigSpec.IntValue maxScalingDay;
        public final ModConfigSpec.DoubleValue maxSpeedBoost;
        public final ModConfigSpec.IntValue maxHealthBoost;
        public final ModConfigSpec.DoubleValue maxArmorChance;
        public final ModConfigSpec.DoubleValue maxWeaponChance;

        // Night boost
        public final ModConfigSpec.BooleanValue enableNightBoost;
        public final ModConfigSpec.DoubleValue nightSpawnMultiplier;

        // Biome and dimension controls
        public final ModConfigSpec.BooleanValue enableBiomeModifiers;
        public final ModConfigSpec.DoubleValue desertHuskBonus;
        public final ModConfigSpec.DoubleValue waterDrownedBonus;
        public final ModConfigSpec.BooleanValue mushroomSafeZone;
        public final ModConfigSpec.BooleanValue netherSpawning;
        public final ModConfigSpec.BooleanValue endSpawning;

        // Death cooldown
        public final ModConfigSpec.BooleanValue enableDeathCooldown;
        public final ModConfigSpec.IntValue deathCooldownSeconds;
        public final ModConfigSpec.DoubleValue cooldownSpawnReduction;

        // Spawn feedback
        public final ModConfigSpec.BooleanValue enableSpawnEffects;
        public final ModConfigSpec.BooleanValue spawnSound;
        public final ModConfigSpec.BooleanValue spawnParticles;

        // Statistics
        public final ModConfigSpec.BooleanValue enableStatistics;

        // Extra drops
        public final ModConfigSpec.DoubleValue boneChance;
        public final ModConfigSpec.DoubleValue stringChance;
        public final ModConfigSpec.DoubleValue enderPearlChance;
        public final ModConfigSpec.DoubleValue phantomMembraneChance;

        public Common(ModConfigSpec.Builder builder) {
            builder.push("general");
            preventSunBurn = builder
                    .comment(
                            "When true, zombies do not take sunlight burn damage.",
                            "Only natural sunlight burn is blocked; normal fire damage should still apply.")
                    .define("preventSunBurn", true);

            enableExtraDrops = builder
                    .comment(
                            "Allow zombies to drop extra configured loot.")
                    .define("enableExtraDrops", true);

            enableDebugLogging = builder
                    .comment(
                            "Verbose logging for troubleshooting spawn and event behavior.")
                    .define("enableDebugLogging", false);
            builder.pop();

            builder.push("dayspawning");
            enableDaySpawning = builder
                    .comment("Master switch for custom zombie spawning.")
                    .define("enableDaySpawning", true);

            daySpawnInterval = builder
                    .comment(
                            "How often spawn attempts happen (ticks).",
                            "20 ticks = 1 second.",
                            "Default 120 = every 6 seconds.")
                    .defineInRange("daySpawnInterval", 120, 1, 72000);

            daySpawnChance = builder
                    .comment(
                            "Chance of a spawn wave per interval (0.0 to 1.0).")
                    .defineInRange("daySpawnChance", 0.5, 0.0, 1.0);

            maxDayZombiesPerPlayer = builder
                    .comment(
                            "Hard cap of nearby zombie-class mobs per player.",
                            "Higher values can increase server load.")
                    .defineInRange("maxDayZombiesPerPlayer", 50, 1, 500);

            zombiesPerSpawn = builder
                    .comment("How many zombies are attempted in each successful wave.")
                    .defineInRange("zombiesPerSpawn", 2, 1, 50);

            spawnRange = builder
                    .comment("Horizontal spawn radius around each player (blocks).")
                    .defineInRange("spawnRange", 30, 16, 128);

            minSpawnDistance = builder
                    .comment("Minimum spawn distance from player (blocks).")
                    .defineInRange("minSpawnDistance", 12, 8, 64);

            spawnAttemptsPerZombie = builder
                    .comment(
                            "Position checks per zombie in a wave.",
                            "Higher values improve fill rate but cost more CPU.")
                    .defineInRange("spawnAttemptsPerZombie", 10, 1, 40);

            requireOpenSkyForOverworldSpawns = builder
                    .comment(
                            "When true, overworld custom spawns require open sky.",
                            "Nether and End use dimension-specific checks instead.")
                    .define("requireOpenSkyForOverworldSpawns", true);
            builder.pop();

            builder.push("variants");
            enableZombieVariants = builder
                    .comment("Allow husk, drowned, and zombie villager variants.")
                    .define("enableZombieVariants", true);

            huskChance = builder
                    .comment("Base husk chance before biome bonuses (0.0 to 1.0).")
                    .defineInRange("huskChance", 0.15, 0.0, 1.0);

            drownedChance = builder
                    .comment("Base drowned chance before biome bonuses (0.0 to 1.0).")
                    .defineInRange("drownedChance", 0.10, 0.0, 1.0);

            babyZombieChance = builder
                    .comment("Chance for spawned zombies to be babies (0.0 to 1.0).")
                    .defineInRange("babyZombieChance", 0.05, 0.0, 1.0);

            zombieVillagerChance = builder
                    .comment("Base zombie villager chance (0.0 to 1.0).")
                    .defineInRange("zombieVillagerChance", 0.05, 0.0, 1.0);
            builder.pop();

            builder.push("horde");
            enableHordeEvents = builder
                    .comment("Enable scheduled horde days.")
                    .define("enableHordeEvents", true);

            hordeIntervalDays = builder
                    .comment("Day interval for horde scheduling checks.")
                    .defineInRange("hordeIntervalDays", 5, 1, 30);

            hordeStartChance = builder
                    .comment(
                            "Chance that a scheduled horde day actually starts a horde.",
                            "Applied once at dawn on scheduled days (0.0 to 1.0).")
                    .defineInRange("hordeStartChance", 0.5, 0.0, 1.0);

            hordeDurationMinutes = builder
                    .comment("Horde duration in real-time minutes.")
                    .defineInRange("hordeDurationMinutes", 5, 1, 60);

            hordeSpawnMultiplier = builder
                    .comment("Spawn chance multiplier while horde is active.")
                    .defineInRange("hordeSpawnMultiplier", 3.0, 1.0, 20.0);

            hordeZombiesPerSpawn = builder
                    .comment("Wave size override during horde.")
                    .defineInRange("hordeZombiesPerSpawn", 5, 1, 100);

            eventSpawnInterval = builder
                    .comment("Spawn interval (ticks) while horde or blood moon is active.")
                    .defineInRange("eventSpawnInterval", 20, 1, 200);

            enableEventNotifications = builder
                    .comment("Show title notifications for horde/blood moon transitions.")
                    .define("enableEventNotifications", true);
            builder.pop();

            builder.push("bloodmoon");
            enableBloodMoon = builder
                    .comment("Enable random blood moon nights.")
                    .define("enableBloodMoon", true);

            bloodMoonChance = builder
                    .comment("Chance each night becomes a blood moon (0.0 to 1.0).")
                    .defineInRange("bloodMoonChance", 0.15, 0.0, 1.0);

            bloodMoonSpawnMultiplier = builder
                    .comment("Spawn chance multiplier during blood moon.")
                    .defineInRange("bloodMoonSpawnMultiplier", 5.0, 1.0, 50.0);

            bloodMoonZombiesPerSpawn = builder
                    .comment("Wave size override during blood moon.")
                    .defineInRange("bloodMoonZombiesPerSpawn", 4, 1, 100);
            builder.pop();

            builder.push("scaling");
            enableDifficultyScaling = builder
                    .comment("Enable day-based zombie stat and gear scaling.")
                    .define("enableDifficultyScaling", true);

            scalingStartDay = builder
                    .comment("Day when scaling begins.")
                    .defineInRange("scalingStartDay", 3, 0, 100);

            maxScalingDay = builder
                    .comment("Day when scaling reaches 100%.")
                    .defineInRange("maxScalingDay", 50, 1, 365);

            maxSpeedBoost = builder
                    .comment("Maximum movement speed boost at full scaling.")
                    .defineInRange("maxSpeedBoost", 0.2, 0.0, 1.0);

            maxHealthBoost = builder
                    .comment("Maximum extra health points at full scaling.")
                    .defineInRange("maxHealthBoost", 10, 0, 40);

            maxArmorChance = builder
                    .comment("Chance for armor at full scaling.")
                    .defineInRange("maxArmorChance", 0.3, 0.0, 1.0);

            maxWeaponChance = builder
                    .comment("Chance for weapon at full scaling.")
                    .defineInRange("maxWeaponChance", 0.2, 0.0, 1.0);
            builder.pop();

            builder.push("nightspawning");
            enableNightBoost = builder
                    .comment("Increase spawn chance at night.")
                    .define("enableNightBoost", true);

            nightSpawnMultiplier = builder
                    .comment("Night spawn chance multiplier.")
                    .defineInRange("nightSpawnMultiplier", 1.5, 1.0, 10.0);
            builder.pop();

            builder.push("biomes");
            enableBiomeModifiers = builder
                    .comment("Apply biome-based variant behavior.")
                    .define("enableBiomeModifiers", true);

            desertHuskBonus = builder
                    .comment("Extra husk chance in desert/badlands.")
                    .defineInRange("desertHuskBonus", 0.5, 0.0, 1.0);

            waterDrownedBonus = builder
                    .comment("Extra drowned chance in ocean/river/swamp biomes.")
                    .defineInRange("waterDrownedBonus", 0.4, 0.0, 1.0);

            mushroomSafeZone = builder
                    .comment("Disable custom spawning in mushroom fields.")
                    .define("mushroomSafeZone", true);

            netherSpawning = builder
                    .comment("Allow custom spawning in the Nether.")
                    .define("netherSpawning", false);

            endSpawning = builder
                    .comment("Allow custom spawning in the End.")
                    .define("endSpawning", false);
            builder.pop();

            builder.push("deathcooldown");
            enableDeathCooldown = builder
                    .comment("Reduce spawn pressure for players after death.")
                    .define("enableDeathCooldown", true);

            deathCooldownSeconds = builder
                    .comment("Cooldown duration in seconds.")
                    .defineInRange("deathCooldownSeconds", 30, 5, 600);

            cooldownSpawnReduction = builder
                    .comment("Spawn chance reduction while in cooldown (0.0 to 1.0).")
                    .defineInRange("cooldownSpawnReduction", 0.5, 0.0, 1.0);
            builder.pop();

            builder.push("effects");
            enableSpawnEffects = builder
                    .comment("Enable audio/particle spawn feedback.")
                    .define("enableSpawnEffects", true);

            spawnSound = builder
                    .comment("Play zombie sound when custom spawns occur.")
                    .define("spawnSound", true);

            spawnParticles = builder
                    .comment("Emit smoke particles when custom spawns occur.")
                    .define("spawnParticles", true);
            builder.pop();

            builder.push("statistics");
            enableStatistics = builder
                    .comment("Track zombie kills and show /zstats info.")
                    .define("enableStatistics", true);
            builder.pop();

            builder.push("drops");
            boneChance = builder
                    .comment("Bonus bone drop chance (0.0 to 1.0).")
                    .defineInRange("boneChance", 0.15, 0.0, 1.0);

            stringChance = builder
                    .comment("Bonus string drop chance (0.0 to 1.0).")
                    .defineInRange("stringChance", 0.15, 0.0, 1.0);

            enderPearlChance = builder
                    .comment("Bonus ender pearl drop chance (0.0 to 1.0).")
                    .defineInRange("enderPearlChance", 0.02, 0.0, 1.0);

            phantomMembraneChance = builder
                    .comment("Bonus phantom membrane drop chance (0.0 to 1.0).")
                    .defineInRange("phantomMembraneChance", 0.03, 0.0, 1.0);
            builder.pop();
        }
    }
}
