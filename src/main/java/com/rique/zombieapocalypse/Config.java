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

        // Attribute tuning for spawned zombie-class mobs
        public final ModConfigSpec.BooleanValue enableAttributeModifiers;
        public final ModConfigSpec.DoubleValue baseHealthMultiplier;
        public final ModConfigSpec.DoubleValue baseHealthBonus;
        public final ModConfigSpec.DoubleValue baseAttackMultiplier;
        public final ModConfigSpec.DoubleValue baseAttackBonus;
        public final ModConfigSpec.DoubleValue baseSpeedMultiplier;
        public final ModConfigSpec.DoubleValue baseSpeedBonus;
        public final ModConfigSpec.DoubleValue baseArmorMultiplier;
        public final ModConfigSpec.DoubleValue baseArmorBonus;
        public final ModConfigSpec.DoubleValue baseFollowRangeMultiplier;
        public final ModConfigSpec.DoubleValue baseFollowRangeBonus;
        public final ModConfigSpec.DoubleValue baseKnockbackResistanceMultiplier;
        public final ModConfigSpec.DoubleValue baseKnockbackResistanceBonus;
        public final ModConfigSpec.BooleanValue scaleAttributesWithDifficulty;
        public final ModConfigSpec.DoubleValue maxHealthScaleMultiplier;
        public final ModConfigSpec.DoubleValue maxHealthScaleBonus;
        public final ModConfigSpec.DoubleValue maxAttackScaleMultiplier;
        public final ModConfigSpec.DoubleValue maxAttackScaleBonus;
        public final ModConfigSpec.DoubleValue maxSpeedScaleMultiplier;
        public final ModConfigSpec.DoubleValue maxSpeedScaleBonus;
        public final ModConfigSpec.DoubleValue maxArmorScaleMultiplier;
        public final ModConfigSpec.DoubleValue maxArmorScaleBonus;
        public final ModConfigSpec.DoubleValue maxFollowRangeScaleMultiplier;
        public final ModConfigSpec.DoubleValue maxFollowRangeScaleBonus;
        public final ModConfigSpec.DoubleValue maxKnockbackResistanceScaleMultiplier;
        public final ModConfigSpec.DoubleValue maxKnockbackResistanceScaleBonus;
        public final ModConfigSpec.BooleanValue enableVariantAttributeProfiles;
        public final ModConfigSpec.DoubleValue zombieHealthMultiplier;
        public final ModConfigSpec.DoubleValue zombieHealthBonus;
        public final ModConfigSpec.DoubleValue zombieAttackMultiplier;
        public final ModConfigSpec.DoubleValue zombieAttackBonus;
        public final ModConfigSpec.DoubleValue zombieSpeedMultiplier;
        public final ModConfigSpec.DoubleValue zombieSpeedBonus;
        public final ModConfigSpec.DoubleValue zombieArmorMultiplier;
        public final ModConfigSpec.DoubleValue zombieArmorBonus;
        public final ModConfigSpec.DoubleValue zombieFollowRangeMultiplier;
        public final ModConfigSpec.DoubleValue zombieFollowRangeBonus;
        public final ModConfigSpec.DoubleValue zombieKnockbackResistanceMultiplier;
        public final ModConfigSpec.DoubleValue zombieKnockbackResistanceBonus;
        public final ModConfigSpec.DoubleValue huskHealthMultiplier;
        public final ModConfigSpec.DoubleValue huskHealthBonus;
        public final ModConfigSpec.DoubleValue huskAttackMultiplier;
        public final ModConfigSpec.DoubleValue huskAttackBonus;
        public final ModConfigSpec.DoubleValue huskSpeedMultiplier;
        public final ModConfigSpec.DoubleValue huskSpeedBonus;
        public final ModConfigSpec.DoubleValue huskArmorMultiplier;
        public final ModConfigSpec.DoubleValue huskArmorBonus;
        public final ModConfigSpec.DoubleValue huskFollowRangeMultiplier;
        public final ModConfigSpec.DoubleValue huskFollowRangeBonus;
        public final ModConfigSpec.DoubleValue huskKnockbackResistanceMultiplier;
        public final ModConfigSpec.DoubleValue huskKnockbackResistanceBonus;
        public final ModConfigSpec.DoubleValue drownedHealthMultiplier;
        public final ModConfigSpec.DoubleValue drownedHealthBonus;
        public final ModConfigSpec.DoubleValue drownedAttackMultiplier;
        public final ModConfigSpec.DoubleValue drownedAttackBonus;
        public final ModConfigSpec.DoubleValue drownedSpeedMultiplier;
        public final ModConfigSpec.DoubleValue drownedSpeedBonus;
        public final ModConfigSpec.DoubleValue drownedArmorMultiplier;
        public final ModConfigSpec.DoubleValue drownedArmorBonus;
        public final ModConfigSpec.DoubleValue drownedFollowRangeMultiplier;
        public final ModConfigSpec.DoubleValue drownedFollowRangeBonus;
        public final ModConfigSpec.DoubleValue drownedKnockbackResistanceMultiplier;
        public final ModConfigSpec.DoubleValue drownedKnockbackResistanceBonus;
        public final ModConfigSpec.DoubleValue zombieVillagerHealthMultiplier;
        public final ModConfigSpec.DoubleValue zombieVillagerHealthBonus;
        public final ModConfigSpec.DoubleValue zombieVillagerAttackMultiplier;
        public final ModConfigSpec.DoubleValue zombieVillagerAttackBonus;
        public final ModConfigSpec.DoubleValue zombieVillagerSpeedMultiplier;
        public final ModConfigSpec.DoubleValue zombieVillagerSpeedBonus;
        public final ModConfigSpec.DoubleValue zombieVillagerArmorMultiplier;
        public final ModConfigSpec.DoubleValue zombieVillagerArmorBonus;
        public final ModConfigSpec.DoubleValue zombieVillagerFollowRangeMultiplier;
        public final ModConfigSpec.DoubleValue zombieVillagerFollowRangeBonus;
        public final ModConfigSpec.DoubleValue zombieVillagerKnockbackResistanceMultiplier;
        public final ModConfigSpec.DoubleValue zombieVillagerKnockbackResistanceBonus;
        public final ModConfigSpec.BooleanValue enableBiomeDimensionAttributeMultipliers;
        public final ModConfigSpec.DoubleValue desertHealthMultiplier;
        public final ModConfigSpec.DoubleValue desertAttackMultiplier;
        public final ModConfigSpec.DoubleValue desertSpeedMultiplier;
        public final ModConfigSpec.DoubleValue desertArmorMultiplier;
        public final ModConfigSpec.DoubleValue desertFollowRangeMultiplier;
        public final ModConfigSpec.DoubleValue desertKnockbackResistanceMultiplier;
        public final ModConfigSpec.DoubleValue waterHealthMultiplier;
        public final ModConfigSpec.DoubleValue waterAttackMultiplier;
        public final ModConfigSpec.DoubleValue waterSpeedMultiplier;
        public final ModConfigSpec.DoubleValue waterArmorMultiplier;
        public final ModConfigSpec.DoubleValue waterFollowRangeMultiplier;
        public final ModConfigSpec.DoubleValue waterKnockbackResistanceMultiplier;
        public final ModConfigSpec.DoubleValue mushroomHealthMultiplier;
        public final ModConfigSpec.DoubleValue mushroomAttackMultiplier;
        public final ModConfigSpec.DoubleValue mushroomSpeedMultiplier;
        public final ModConfigSpec.DoubleValue mushroomArmorMultiplier;
        public final ModConfigSpec.DoubleValue mushroomFollowRangeMultiplier;
        public final ModConfigSpec.DoubleValue mushroomKnockbackResistanceMultiplier;
        public final ModConfigSpec.DoubleValue netherHealthMultiplier;
        public final ModConfigSpec.DoubleValue netherAttackMultiplier;
        public final ModConfigSpec.DoubleValue netherSpeedMultiplier;
        public final ModConfigSpec.DoubleValue netherArmorMultiplier;
        public final ModConfigSpec.DoubleValue netherFollowRangeMultiplier;
        public final ModConfigSpec.DoubleValue netherKnockbackResistanceMultiplier;
        public final ModConfigSpec.DoubleValue endHealthMultiplier;
        public final ModConfigSpec.DoubleValue endAttackMultiplier;
        public final ModConfigSpec.DoubleValue endSpeedMultiplier;
        public final ModConfigSpec.DoubleValue endArmorMultiplier;
        public final ModConfigSpec.DoubleValue endFollowRangeMultiplier;
        public final ModConfigSpec.DoubleValue endKnockbackResistanceMultiplier;

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
