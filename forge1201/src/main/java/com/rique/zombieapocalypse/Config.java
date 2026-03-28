package com.rique.zombieapocalypse;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Config {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    private Config() {
    }

    public static final class Common {

        // General
        public final ForgeConfigSpec.BooleanValue preventSunBurn;
        public final ForgeConfigSpec.BooleanValue enableExtraDrops;
        public final ForgeConfigSpec.BooleanValue enableDebugLogging;

        // Day spawning core
        public final ForgeConfigSpec.BooleanValue enableDaySpawning;
        public final ForgeConfigSpec.IntValue daySpawnInterval;
        public final ForgeConfigSpec.DoubleValue daySpawnChance;
        public final ForgeConfigSpec.IntValue maxDayZombiesPerPlayer;
        public final ForgeConfigSpec.IntValue zombiesPerSpawn;
        public final ForgeConfigSpec.IntValue spawnRange;
        public final ForgeConfigSpec.IntValue minSpawnDistance;
        public final ForgeConfigSpec.IntValue spawnAttemptsPerZombie;
        public final ForgeConfigSpec.BooleanValue requireOpenSkyForOverworldSpawns;
        public final ForgeConfigSpec.IntValue daylightSpawnStartDay;

        // Zombie variants
        public final ForgeConfigSpec.BooleanValue enableZombieVariants;
        public final ForgeConfigSpec.DoubleValue huskChance;
        public final ForgeConfigSpec.DoubleValue drownedChance;
        public final ForgeConfigSpec.DoubleValue babyZombieChance;
        public final ForgeConfigSpec.DoubleValue zombieVillagerChance;

        // Horde events
        public final ForgeConfigSpec.BooleanValue enableHordeEvents;
        public final ForgeConfigSpec.IntValue hordeIntervalDays;
        public final ForgeConfigSpec.DoubleValue hordeStartChance;
        public final ForgeConfigSpec.IntValue hordeDurationMinutes;
        public final ForgeConfigSpec.DoubleValue hordeSpawnMultiplier;
        public final ForgeConfigSpec.IntValue hordeZombiesPerSpawn;
        public final ForgeConfigSpec.IntValue eventSpawnInterval;
        public final ForgeConfigSpec.BooleanValue enableEventNotifications;
        public final ForgeConfigSpec.BooleanValue enableDayCounterAnnouncements;

        // Blood moon
        public final ForgeConfigSpec.BooleanValue enableBloodMoon;
        public final ForgeConfigSpec.DoubleValue bloodMoonChance;
        public final ForgeConfigSpec.DoubleValue bloodMoonSpawnMultiplier;
        public final ForgeConfigSpec.IntValue bloodMoonZombiesPerSpawn;

        // Difficulty scaling
        public final ForgeConfigSpec.BooleanValue enableDifficultyScaling;
        public final ForgeConfigSpec.IntValue scalingStartDay;
        public final ForgeConfigSpec.IntValue maxScalingDay;
        public final ForgeConfigSpec.DoubleValue maxSpeedBoost;
        public final ForgeConfigSpec.IntValue maxHealthBoost;
        public final ForgeConfigSpec.DoubleValue maxArmorChance;
        public final ForgeConfigSpec.DoubleValue maxWeaponChance;

        // Attribute tuning for spawned zombie-class mobs
        public final ForgeConfigSpec.BooleanValue enableAttributeModifiers;
        public final ForgeConfigSpec.DoubleValue baseHealthMultiplier;
        public final ForgeConfigSpec.DoubleValue baseHealthBonus;
        public final ForgeConfigSpec.DoubleValue baseAttackMultiplier;
        public final ForgeConfigSpec.DoubleValue baseAttackBonus;
        public final ForgeConfigSpec.DoubleValue baseSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue baseSpeedBonus;
        public final ForgeConfigSpec.DoubleValue baseArmorMultiplier;
        public final ForgeConfigSpec.DoubleValue baseArmorBonus;
        public final ForgeConfigSpec.DoubleValue baseFollowRangeMultiplier;
        public final ForgeConfigSpec.DoubleValue baseFollowRangeBonus;
        public final ForgeConfigSpec.DoubleValue baseKnockbackResistanceMultiplier;
        public final ForgeConfigSpec.DoubleValue baseKnockbackResistanceBonus;
        public final ForgeConfigSpec.BooleanValue scaleAttributesWithDifficulty;
        public final ForgeConfigSpec.DoubleValue maxHealthScaleMultiplier;
        public final ForgeConfigSpec.DoubleValue maxHealthScaleBonus;
        public final ForgeConfigSpec.DoubleValue maxAttackScaleMultiplier;
        public final ForgeConfigSpec.DoubleValue maxAttackScaleBonus;
        public final ForgeConfigSpec.DoubleValue maxSpeedScaleMultiplier;
        public final ForgeConfigSpec.DoubleValue maxSpeedScaleBonus;
        public final ForgeConfigSpec.DoubleValue maxArmorScaleMultiplier;
        public final ForgeConfigSpec.DoubleValue maxArmorScaleBonus;
        public final ForgeConfigSpec.DoubleValue maxFollowRangeScaleMultiplier;
        public final ForgeConfigSpec.DoubleValue maxFollowRangeScaleBonus;
        public final ForgeConfigSpec.DoubleValue maxKnockbackResistanceScaleMultiplier;
        public final ForgeConfigSpec.DoubleValue maxKnockbackResistanceScaleBonus;
        public final ForgeConfigSpec.BooleanValue enableVariantAttributeProfiles;
        public final ForgeConfigSpec.DoubleValue zombieHealthMultiplier;
        public final ForgeConfigSpec.DoubleValue zombieHealthBonus;
        public final ForgeConfigSpec.DoubleValue zombieAttackMultiplier;
        public final ForgeConfigSpec.DoubleValue zombieAttackBonus;
        public final ForgeConfigSpec.DoubleValue zombieSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue zombieSpeedBonus;
        public final ForgeConfigSpec.DoubleValue zombieArmorMultiplier;
        public final ForgeConfigSpec.DoubleValue zombieArmorBonus;
        public final ForgeConfigSpec.DoubleValue zombieFollowRangeMultiplier;
        public final ForgeConfigSpec.DoubleValue zombieFollowRangeBonus;
        public final ForgeConfigSpec.DoubleValue zombieKnockbackResistanceMultiplier;
        public final ForgeConfigSpec.DoubleValue zombieKnockbackResistanceBonus;
        public final ForgeConfigSpec.DoubleValue huskHealthMultiplier;
        public final ForgeConfigSpec.DoubleValue huskHealthBonus;
        public final ForgeConfigSpec.DoubleValue huskAttackMultiplier;
        public final ForgeConfigSpec.DoubleValue huskAttackBonus;
        public final ForgeConfigSpec.DoubleValue huskSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue huskSpeedBonus;
        public final ForgeConfigSpec.DoubleValue huskArmorMultiplier;
        public final ForgeConfigSpec.DoubleValue huskArmorBonus;
        public final ForgeConfigSpec.DoubleValue huskFollowRangeMultiplier;
        public final ForgeConfigSpec.DoubleValue huskFollowRangeBonus;
        public final ForgeConfigSpec.DoubleValue huskKnockbackResistanceMultiplier;
        public final ForgeConfigSpec.DoubleValue huskKnockbackResistanceBonus;
        public final ForgeConfigSpec.DoubleValue drownedHealthMultiplier;
        public final ForgeConfigSpec.DoubleValue drownedHealthBonus;
        public final ForgeConfigSpec.DoubleValue drownedAttackMultiplier;
        public final ForgeConfigSpec.DoubleValue drownedAttackBonus;
        public final ForgeConfigSpec.DoubleValue drownedSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue drownedSpeedBonus;
        public final ForgeConfigSpec.DoubleValue drownedArmorMultiplier;
        public final ForgeConfigSpec.DoubleValue drownedArmorBonus;
        public final ForgeConfigSpec.DoubleValue drownedFollowRangeMultiplier;
        public final ForgeConfigSpec.DoubleValue drownedFollowRangeBonus;
        public final ForgeConfigSpec.DoubleValue drownedKnockbackResistanceMultiplier;
        public final ForgeConfigSpec.DoubleValue drownedKnockbackResistanceBonus;
        public final ForgeConfigSpec.DoubleValue zombieVillagerHealthMultiplier;
        public final ForgeConfigSpec.DoubleValue zombieVillagerHealthBonus;
        public final ForgeConfigSpec.DoubleValue zombieVillagerAttackMultiplier;
        public final ForgeConfigSpec.DoubleValue zombieVillagerAttackBonus;
        public final ForgeConfigSpec.DoubleValue zombieVillagerSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue zombieVillagerSpeedBonus;
        public final ForgeConfigSpec.DoubleValue zombieVillagerArmorMultiplier;
        public final ForgeConfigSpec.DoubleValue zombieVillagerArmorBonus;
        public final ForgeConfigSpec.DoubleValue zombieVillagerFollowRangeMultiplier;
        public final ForgeConfigSpec.DoubleValue zombieVillagerFollowRangeBonus;
        public final ForgeConfigSpec.DoubleValue zombieVillagerKnockbackResistanceMultiplier;
        public final ForgeConfigSpec.DoubleValue zombieVillagerKnockbackResistanceBonus;
        public final ForgeConfigSpec.BooleanValue enableBiomeDimensionAttributeMultipliers;
        public final ForgeConfigSpec.DoubleValue desertHealthMultiplier;
        public final ForgeConfigSpec.DoubleValue desertAttackMultiplier;
        public final ForgeConfigSpec.DoubleValue desertSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue desertArmorMultiplier;
        public final ForgeConfigSpec.DoubleValue desertFollowRangeMultiplier;
        public final ForgeConfigSpec.DoubleValue desertKnockbackResistanceMultiplier;
        public final ForgeConfigSpec.DoubleValue waterHealthMultiplier;
        public final ForgeConfigSpec.DoubleValue waterAttackMultiplier;
        public final ForgeConfigSpec.DoubleValue waterSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue waterArmorMultiplier;
        public final ForgeConfigSpec.DoubleValue waterFollowRangeMultiplier;
        public final ForgeConfigSpec.DoubleValue waterKnockbackResistanceMultiplier;
        public final ForgeConfigSpec.DoubleValue mushroomHealthMultiplier;
        public final ForgeConfigSpec.DoubleValue mushroomAttackMultiplier;
        public final ForgeConfigSpec.DoubleValue mushroomSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue mushroomArmorMultiplier;
        public final ForgeConfigSpec.DoubleValue mushroomFollowRangeMultiplier;
        public final ForgeConfigSpec.DoubleValue mushroomKnockbackResistanceMultiplier;
        public final ForgeConfigSpec.DoubleValue netherHealthMultiplier;
        public final ForgeConfigSpec.DoubleValue netherAttackMultiplier;
        public final ForgeConfigSpec.DoubleValue netherSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue netherArmorMultiplier;
        public final ForgeConfigSpec.DoubleValue netherFollowRangeMultiplier;
        public final ForgeConfigSpec.DoubleValue netherKnockbackResistanceMultiplier;
        public final ForgeConfigSpec.DoubleValue endHealthMultiplier;
        public final ForgeConfigSpec.DoubleValue endAttackMultiplier;
        public final ForgeConfigSpec.DoubleValue endSpeedMultiplier;
        public final ForgeConfigSpec.DoubleValue endArmorMultiplier;
        public final ForgeConfigSpec.DoubleValue endFollowRangeMultiplier;
        public final ForgeConfigSpec.DoubleValue endKnockbackResistanceMultiplier;

        // Night boost
        public final ForgeConfigSpec.BooleanValue enableNightBoost;
        public final ForgeConfigSpec.DoubleValue nightSpawnMultiplier;

        // Biome and dimension controls
        public final ForgeConfigSpec.BooleanValue enableBiomeModifiers;
        public final ForgeConfigSpec.DoubleValue desertHuskBonus;
        public final ForgeConfigSpec.DoubleValue waterDrownedBonus;
        public final ForgeConfigSpec.BooleanValue mushroomSafeZone;
        public final ForgeConfigSpec.BooleanValue netherSpawning;
        public final ForgeConfigSpec.BooleanValue endSpawning;

        // Death cooldown
        public final ForgeConfigSpec.BooleanValue enableDeathCooldown;
        public final ForgeConfigSpec.IntValue deathCooldownSeconds;
        public final ForgeConfigSpec.DoubleValue cooldownSpawnReduction;

        // Spawn feedback
        public final ForgeConfigSpec.BooleanValue enableSpawnEffects;
        public final ForgeConfigSpec.BooleanValue spawnSound;
        public final ForgeConfigSpec.BooleanValue spawnParticles;

        // Statistics
        public final ForgeConfigSpec.BooleanValue enableStatistics;

        // Extra drops
        public final ForgeConfigSpec.DoubleValue boneChance;
        public final ForgeConfigSpec.DoubleValue stringChance;
        public final ForgeConfigSpec.DoubleValue enderPearlChance;
        public final ForgeConfigSpec.DoubleValue phantomMembraneChance;

        public Common(ForgeConfigSpec.Builder builder) {
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

            daylightSpawnStartDay = builder
                    .comment(
                            "Disable custom daytime spawning until this day counter is reached.",
                            "0 = daytime spawning is allowed immediately.",
                            "Nighttime spawning is unaffected.")
                    .defineInRange("daylightSpawnStartDay", 0, 0, 3650);
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

            enableDayCounterAnnouncements = builder
                    .comment(
                            "Show the current day on screen each morning.",
                            "Uses the same title-style pop-up as apocalypse events.")
                    .define("enableDayCounterAnnouncements", true);
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

            builder.push("attributes");
            enableAttributeModifiers = builder
                    .comment(
                            "Master switch for spawned zombie-class mob attribute tuning.",
                            "Applies to zombies, husks, drowned, and zombie villagers spawned by this mod.")
                    .define("enableAttributeModifiers", true);

            scaleAttributesWithDifficulty = builder
                    .comment(
                            "When true, attribute scaling values below are applied using the difficulty factor.",
                            "Difficulty factor is 0.0 to 1.0 based on scalingStartDay/maxScalingDay.")
                    .define("scaleAttributesWithDifficulty", true);

            enableVariantAttributeProfiles = builder
                    .comment(
                            "When true, per-variant profiles apply on top of base values.",
                            "Profiles exist for zombie, husk, drowned, and zombie villager.")
                    .define("enableVariantAttributeProfiles", true);

            enableBiomeDimensionAttributeMultipliers = builder
                    .comment(
                            "When true, biome and dimension multipliers apply on top of base/variant values.",
                            "Biome contexts: desert, water, mushroom.",
                            "Dimension contexts: nether, end.",
                            "If multiple contexts match, their multipliers stack multiplicatively.")
                    .define("enableBiomeDimensionAttributeMultipliers", true);

            builder.push("base");
            baseHealthMultiplier = builder
                    .comment(
                            "Base max health multiplier for mod-spawned mobs.",
                            "Typical vanilla zombie baseline: max health = 20.0.")
                    .defineInRange("baseHealthMultiplier", 1.0, 0.0, 10.0);

            baseHealthBonus = builder
                    .comment(
                            "Flat max health bonus applied after baseHealthMultiplier.",
                            "Typical vanilla zombie baseline: max health = 20.0.")
                    .defineInRange("baseHealthBonus", 0.0, -200.0, 200.0);

            baseAttackMultiplier = builder
                    .comment(
                            "Base attack damage multiplier for mod-spawned mobs.",
                            "Typical vanilla zombie baseline: attack damage = 3.0.",
                            "Final damage can still vary with vanilla difficulty and effects.")
                    .defineInRange("baseAttackMultiplier", 1.0, 0.0, 10.0);

            baseAttackBonus = builder
                    .comment(
                            "Flat attack damage bonus applied after baseAttackMultiplier.",
                            "Typical vanilla zombie baseline: attack damage = 3.0.")
                    .defineInRange("baseAttackBonus", 0.0, -50.0, 50.0);

            baseSpeedMultiplier = builder
                    .comment(
                            "Base movement speed multiplier for mod-spawned mobs.",
                            "Typical vanilla zombie baseline: movement speed = 0.23.")
                    .defineInRange("baseSpeedMultiplier", 1.0, 0.0, 10.0);

            baseSpeedBonus = builder
                    .comment(
                            "Flat movement speed bonus applied after baseSpeedMultiplier.",
                            "Typical vanilla zombie baseline: movement speed = 0.23.")
                    .defineInRange("baseSpeedBonus", 0.0, -1.0, 1.0);

            baseArmorMultiplier = builder
                    .comment(
                            "Base armor multiplier for mod-spawned mobs.",
                            "Typical vanilla zombie baseline: armor = 2.0.")
                    .defineInRange("baseArmorMultiplier", 1.0, 0.0, 10.0);

            baseArmorBonus = builder
                    .comment(
                            "Flat armor bonus applied after baseArmorMultiplier.",
                            "Typical vanilla zombie baseline: armor = 2.0.")
                    .defineInRange("baseArmorBonus", 0.0, -30.0, 30.0);

            baseFollowRangeMultiplier = builder
                    .comment(
                            "Base follow range multiplier for mod-spawned mobs.",
                            "Typical vanilla zombie baseline: follow range = 35.0.")
                    .defineInRange("baseFollowRangeMultiplier", 1.0, 0.0, 10.0);

            baseFollowRangeBonus = builder
                    .comment(
                            "Flat follow range bonus applied after baseFollowRangeMultiplier.",
                            "Typical vanilla zombie baseline: follow range = 35.0.")
                    .defineInRange("baseFollowRangeBonus", 0.0, -100.0, 100.0);

            baseKnockbackResistanceMultiplier = builder
                    .comment(
                            "Base knockback resistance multiplier for mod-spawned mobs.",
                            "Typical vanilla zombie baseline: knockback resistance = 0.0.")
                    .defineInRange("baseKnockbackResistanceMultiplier", 1.0, 0.0, 10.0);

            baseKnockbackResistanceBonus = builder
                    .comment(
                            "Flat knockback resistance bonus applied after baseKnockbackResistanceMultiplier.",
                            "Typical vanilla zombie baseline: knockback resistance = 0.0.",
                            "Useful range is usually 0.0 to 1.0.")
                    .defineInRange("baseKnockbackResistanceBonus", 0.0, -1.0, 1.0);
            builder.pop();

            builder.push("scaling");
            maxHealthScaleMultiplier = builder
                    .comment(
                            "At 100% difficulty factor, additional health multiplier.",
                            "Formula: value *= (1 + maxHealthScaleMultiplier * factor).")
                    .defineInRange("maxHealthScaleMultiplier", 0.0, 0.0, 10.0);

            maxHealthScaleBonus = builder
                    .comment(
                            "At 100% difficulty factor, additional flat health bonus.",
                            "Formula: value += maxHealthScaleBonus * factor.")
                    .defineInRange("maxHealthScaleBonus", 0.0, -200.0, 200.0);

            maxAttackScaleMultiplier = builder
                    .comment("At 100% difficulty factor, additional attack damage multiplier.")
                    .defineInRange("maxAttackScaleMultiplier", 0.0, 0.0, 10.0);

            maxAttackScaleBonus = builder
                    .comment("At 100% difficulty factor, additional flat attack damage bonus.")
                    .defineInRange("maxAttackScaleBonus", 0.0, -50.0, 50.0);

            maxSpeedScaleMultiplier = builder
                    .comment("At 100% difficulty factor, additional movement speed multiplier.")
                    .defineInRange("maxSpeedScaleMultiplier", 0.0, 0.0, 10.0);

            maxSpeedScaleBonus = builder
                    .comment("At 100% difficulty factor, additional flat movement speed bonus.")
                    .defineInRange("maxSpeedScaleBonus", 0.0, -1.0, 1.0);

            maxArmorScaleMultiplier = builder
                    .comment("At 100% difficulty factor, additional armor multiplier.")
                    .defineInRange("maxArmorScaleMultiplier", 0.0, 0.0, 10.0);

            maxArmorScaleBonus = builder
                    .comment("At 100% difficulty factor, additional flat armor bonus.")
                    .defineInRange("maxArmorScaleBonus", 0.0, -30.0, 30.0);

            maxFollowRangeScaleMultiplier = builder
                    .comment("At 100% difficulty factor, additional follow range multiplier.")
                    .defineInRange("maxFollowRangeScaleMultiplier", 0.0, 0.0, 10.0);

            maxFollowRangeScaleBonus = builder
                    .comment("At 100% difficulty factor, additional flat follow range bonus.")
                    .defineInRange("maxFollowRangeScaleBonus", 0.0, -100.0, 100.0);

            maxKnockbackResistanceScaleMultiplier = builder
                    .comment("At 100% difficulty factor, additional knockback resistance multiplier.")
                    .defineInRange("maxKnockbackResistanceScaleMultiplier", 0.0, 0.0, 10.0);

            maxKnockbackResistanceScaleBonus = builder
                    .comment(
                            "At 100% difficulty factor, additional flat knockback resistance bonus.",
                            "Useful range is usually 0.0 to 1.0.")
                    .defineInRange("maxKnockbackResistanceScaleBonus", 0.0, -1.0, 1.0);
            builder.pop();

            builder.push("variants");

            builder.push("zombie");
            zombieHealthMultiplier = builder
                    .comment(
                            "Per-variant profile for normal zombies.",
                            "Stacks on top of attributes.base.* values.",
                            "Vanilla baseline reference: health 20.0, attack 3.0, speed 0.23, armor 2.0, follow range 35.0, knockback resistance 0.0.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            zombieHealthBonus = builder.defineInRange("healthBonus", 0.0, -200.0, 200.0);
            zombieAttackMultiplier = builder.defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            zombieAttackBonus = builder.defineInRange("attackBonus", 0.0, -50.0, 50.0);
            zombieSpeedMultiplier = builder.defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            zombieSpeedBonus = builder.defineInRange("speedBonus", 0.0, -1.0, 1.0);
            zombieArmorMultiplier = builder.defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            zombieArmorBonus = builder.defineInRange("armorBonus", 0.0, -30.0, 30.0);
            zombieFollowRangeMultiplier = builder.defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            zombieFollowRangeBonus = builder.defineInRange("followRangeBonus", 0.0, -100.0, 100.0);
            zombieKnockbackResistanceMultiplier = builder.defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            zombieKnockbackResistanceBonus = builder.defineInRange("knockbackResistanceBonus", 0.0, -1.0, 1.0);
            builder.pop();

            builder.push("husk");
            huskHealthMultiplier = builder
                    .comment("Per-variant profile for husks. Stacks on top of attributes.base.* values.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            huskHealthBonus = builder.defineInRange("healthBonus", 0.0, -200.0, 200.0);
            huskAttackMultiplier = builder.defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            huskAttackBonus = builder.defineInRange("attackBonus", 0.0, -50.0, 50.0);
            huskSpeedMultiplier = builder.defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            huskSpeedBonus = builder.defineInRange("speedBonus", 0.0, -1.0, 1.0);
            huskArmorMultiplier = builder.defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            huskArmorBonus = builder.defineInRange("armorBonus", 0.0, -30.0, 30.0);
            huskFollowRangeMultiplier = builder.defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            huskFollowRangeBonus = builder.defineInRange("followRangeBonus", 0.0, -100.0, 100.0);
            huskKnockbackResistanceMultiplier = builder.defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            huskKnockbackResistanceBonus = builder.defineInRange("knockbackResistanceBonus", 0.0, -1.0, 1.0);
            builder.pop();

            builder.push("drowned");
            drownedHealthMultiplier = builder
                    .comment("Per-variant profile for drowned. Stacks on top of attributes.base.* values.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            drownedHealthBonus = builder.defineInRange("healthBonus", 0.0, -200.0, 200.0);
            drownedAttackMultiplier = builder.defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            drownedAttackBonus = builder.defineInRange("attackBonus", 0.0, -50.0, 50.0);
            drownedSpeedMultiplier = builder.defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            drownedSpeedBonus = builder.defineInRange("speedBonus", 0.0, -1.0, 1.0);
            drownedArmorMultiplier = builder.defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            drownedArmorBonus = builder.defineInRange("armorBonus", 0.0, -30.0, 30.0);
            drownedFollowRangeMultiplier = builder.defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            drownedFollowRangeBonus = builder.defineInRange("followRangeBonus", 0.0, -100.0, 100.0);
            drownedKnockbackResistanceMultiplier = builder.defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            drownedKnockbackResistanceBonus = builder.defineInRange("knockbackResistanceBonus", 0.0, -1.0, 1.0);
            builder.pop();

            builder.push("zombieVillager");
            zombieVillagerHealthMultiplier = builder
                    .comment("Per-variant profile for zombie villagers. Stacks on top of attributes.base.* values.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            zombieVillagerHealthBonus = builder.defineInRange("healthBonus", 0.0, -200.0, 200.0);
            zombieVillagerAttackMultiplier = builder.defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            zombieVillagerAttackBonus = builder.defineInRange("attackBonus", 0.0, -50.0, 50.0);
            zombieVillagerSpeedMultiplier = builder.defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            zombieVillagerSpeedBonus = builder.defineInRange("speedBonus", 0.0, -1.0, 1.0);
            zombieVillagerArmorMultiplier = builder.defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            zombieVillagerArmorBonus = builder.defineInRange("armorBonus", 0.0, -30.0, 30.0);
            zombieVillagerFollowRangeMultiplier = builder.defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            zombieVillagerFollowRangeBonus = builder.defineInRange("followRangeBonus", 0.0, -100.0, 100.0);
            zombieVillagerKnockbackResistanceMultiplier = builder
                    .defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            zombieVillagerKnockbackResistanceBonus = builder.defineInRange("knockbackResistanceBonus", 0.0, -1.0, 1.0);
            builder.pop();
            builder.pop();

            builder.push("contexts");

            builder.push("desert");
            desertHealthMultiplier = builder
                    .comment("Biome context multiplier in desert/badlands biomes.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            desertAttackMultiplier = builder.defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            desertSpeedMultiplier = builder.defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            desertArmorMultiplier = builder.defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            desertFollowRangeMultiplier = builder.defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            desertKnockbackResistanceMultiplier = builder.defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            builder.pop();

            builder.push("water");
            waterHealthMultiplier = builder
                    .comment("Biome context multiplier in ocean/river/swamp biomes.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            waterAttackMultiplier = builder.defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            waterSpeedMultiplier = builder.defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            waterArmorMultiplier = builder.defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            waterFollowRangeMultiplier = builder.defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            waterKnockbackResistanceMultiplier = builder.defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            builder.pop();

            builder.push("mushroom");
            mushroomHealthMultiplier = builder
                    .comment("Biome context multiplier in mushroom fields.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            mushroomAttackMultiplier = builder.defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            mushroomSpeedMultiplier = builder.defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            mushroomArmorMultiplier = builder.defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            mushroomFollowRangeMultiplier = builder.defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            mushroomKnockbackResistanceMultiplier = builder
                    .defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            builder.pop();

            builder.push("nether");
            netherHealthMultiplier = builder
                    .comment("Dimension context multiplier in the Nether.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            netherAttackMultiplier = builder.defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            netherSpeedMultiplier = builder.defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            netherArmorMultiplier = builder.defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            netherFollowRangeMultiplier = builder.defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            netherKnockbackResistanceMultiplier = builder
                    .defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            builder.pop();

            builder.push("end");
            endHealthMultiplier = builder
                    .comment("Dimension context multiplier in the End.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            endAttackMultiplier = builder.defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            endSpeedMultiplier = builder.defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            endArmorMultiplier = builder.defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            endFollowRangeMultiplier = builder.defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            endKnockbackResistanceMultiplier = builder.defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            builder.pop();

            builder.pop();
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
                    .comment("Track zombie kills for /zstats info.")
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
