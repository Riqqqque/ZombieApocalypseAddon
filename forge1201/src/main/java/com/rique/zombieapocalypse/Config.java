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

        // Attribute tuning for zombie-class mobs
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
        public final ForgeConfigSpec.DoubleValue gunpowderChance;
        public final ForgeConfigSpec.DoubleValue enderPearlChance;
        public final ForgeConfigSpec.DoubleValue phantomMembraneChance;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            preventSunBurn = builder
                    .comment(
                            "Controls whether zombie-class mobs can burn in daylight.",
                            "true = they ignore sunlight burning, false = they burn like normal vanilla zombies.",
                            "Only sunlight burning is blocked here; other fire damage still works normally.")
                    .define("preventSunBurn", true);

            enableExtraDrops = builder
                    .comment(
                            "Enables the bonus loot chances in the [drops] section below.",
                            "Set this to false if you want to disable every extra drop with one switch.")
                    .define("enableExtraDrops", true);

            enableDebugLogging = builder
                    .comment(
                            "Writes extra spawn and event details to the log for troubleshooting.",
                            "Best left off unless you are testing or tracking down a problem.")
                    .define("enableDebugLogging", false);
            builder.pop();

            builder.push("dayspawning");
            enableDaySpawning = builder
                    .comment(
                            "Main on/off switch for the mod's custom zombie spawning.",
                            "If false, the mod stops creating its own zombie waves around players.")
                    .define("enableDaySpawning", true);

            daySpawnInterval = builder
                    .comment(
                            "How often the mod checks whether to spawn a custom wave.",
                            "20 ticks = 1 second.",
                            "Default 120 = one check every 6 seconds.")
                    .defineInRange("daySpawnInterval", 120, 1, 72000);

            daySpawnChance = builder
                    .comment(
                            "Chance that each spawn check turns into a real wave.",
                            "0.0 = never spawn, 1.0 = every check spawns a wave.")
                    .defineInRange("daySpawnChance", 0.5, 0.0, 1.0);

            maxDayZombiesPerPlayer = builder
                    .comment(
                            "Maximum nearby zombie-class mobs allowed around each player before new custom spawns are skipped.",
                            "This helps stop overcrowding and keeps performance under control.")
                    .defineInRange("maxDayZombiesPerPlayer", 50, 1, 500);

            zombiesPerSpawn = builder
                    .comment(
                            "How many mobs the mod tries to place in each successful wave.",
                            "The real number can be lower if it cannot find enough valid spawn spots.")
                    .defineInRange("zombiesPerSpawn", 2, 1, 50);

            spawnRange = builder
                    .comment(
                            "How far from each player custom spawns are allowed to appear horizontally.",
                            "Higher values spread mobs farther out and can make them feel less concentrated.")
                    .defineInRange("spawnRange", 30, 16, 128);

            minSpawnDistance = builder
                    .comment(
                            "Closest distance a custom spawn is allowed to appear from a player.",
                            "Raise this if mobs feel like they are popping in too close.")
                    .defineInRange("minSpawnDistance", 12, 8, 64);

            spawnAttemptsPerZombie = builder
                    .comment(
                            "How many location tries the mod makes for each mob in a wave.",
                            "Higher values help waves fill more reliably, but they also use more CPU time.")
                    .defineInRange("spawnAttemptsPerZombie", 10, 1, 40);

            requireOpenSkyForOverworldSpawns = builder
                    .comment(
                            "If true, overworld custom spawns only happen where the sky is open.",
                            "Turn this off if you also want custom spawns under trees, roofs, or in more covered areas.")
                    .define("requireOpenSkyForOverworldSpawns", true);

            daylightSpawnStartDay = builder
                    .comment(
                            "Blocks daytime custom spawning until the world reaches this day number.",
                            "0 = daytime spawning starts immediately.",
                            "Nighttime custom spawning still works before this day.")
                    .defineInRange("daylightSpawnStartDay", 0, 0, 3650);
            builder.pop();

            builder.push("variants");
            enableZombieVariants = builder
                    .comment(
                            "Allows the mod to spawn special zombie variants instead of only normal zombies.",
                            "Variants include husks, drowned, and zombie villagers.")
                    .define("enableZombieVariants", true);

            huskChance = builder
                    .comment(
                            "Base chance for a custom spawn to become a husk before biome bonuses are added.",
                            "0.0 = never, 1.0 = always when variant spawning picks a type.")
                    .defineInRange("huskChance", 0.15, 0.0, 1.0);

            drownedChance = builder
                    .comment(
                            "Base chance for a custom spawn to become a drowned before biome bonuses are added.",
                            "0.0 = never, 1.0 = always when variant spawning picks a type.")
                    .defineInRange("drownedChance", 0.10, 0.0, 1.0);

            babyZombieChance = builder
                    .comment(
                            "Chance for a spawned zombie-type mob to be a baby when that mob type supports it.",
                            "0.0 = never, 1.0 = always.")
                    .defineInRange("babyZombieChance", 0.05, 0.0, 1.0);

            zombieVillagerChance = builder
                    .comment(
                            "Base chance for a custom spawn to become a zombie villager.",
                            "Any remaining chance after the variant rolls becomes a normal zombie.")
                    .defineInRange("zombieVillagerChance", 0.05, 0.0, 1.0);
            builder.pop();

            builder.push("horde");
            enableHordeEvents = builder
                    .comment(
                            "Enables scheduled horde days that can trigger large pressure spikes.",
                            "Admins can still use horde commands even if this automatic system is disabled.")
                    .define("enableHordeEvents", true);

            hordeIntervalDays = builder
                    .comment(
                            "How often the mod reaches a scheduled horde day.",
                            "Example: 5 means every 5th day is checked for a horde.")
                    .defineInRange("hordeIntervalDays", 5, 1, 30);

            hordeStartChance = builder
                    .comment(
                            "Chance that a scheduled horde day actually starts a horde at dawn.",
                            "0.0 = scheduled days never trigger, 1.0 = every scheduled day triggers.")
                    .defineInRange("hordeStartChance", 0.5, 0.0, 1.0);

            hordeDurationMinutes = builder
                    .comment(
                            "How long a horde lasts in real-world minutes.",
                            "Longer hordes keep the stronger event settings active for more time.")
                    .defineInRange("hordeDurationMinutes", 5, 1, 60);

            hordeSpawnMultiplier = builder
                    .comment(
                            "Extra spawn chance multiplier used while a horde is active.",
                            "1.0 = no extra boost, 3.0 = three times the normal chance.")
                    .defineInRange("hordeSpawnMultiplier", 3.0, 1.0, 20.0);

            hordeZombiesPerSpawn = builder
                    .comment(
                            "How many mobs each custom wave tries to spawn during a horde.",
                            "This overrides the normal wave size while the event is active.")
                    .defineInRange("hordeZombiesPerSpawn", 5, 1, 100);

            eventSpawnInterval = builder
                    .comment(
                            "Spawn check interval used during hordes and blood moons.",
                            "Lower values mean more frequent checks. 20 ticks = 1 second.")
                    .defineInRange("eventSpawnInterval", 20, 1, 200);

            enableEventNotifications = builder
                    .comment(
                            "Shows on-screen title popups when major events start or end.",
                            "This includes horde and blood moon announcements.")
                    .define("enableEventNotifications", true);

            enableDayCounterAnnouncements = builder
                    .comment(
                            "Shows the current day on screen each morning as a title popup.",
                            "Useful as a visible day counter for players without checking commands.")
                    .define("enableDayCounterAnnouncements", true);
            builder.pop();

            builder.push("bloodmoon");
            enableBloodMoon = builder
                    .comment(
                            "Enables random blood moon nights.",
                            "Admins can still force a blood moon with the command even if this is off.")
                    .define("enableBloodMoon", true);

            bloodMoonChance = builder
                    .comment(
                            "Chance each night becomes a blood moon.",
                            "0.0 = never, 1.0 = every night.")
                    .defineInRange("bloodMoonChance", 0.15, 0.0, 1.0);

            bloodMoonSpawnMultiplier = builder
                    .comment(
                            "Extra spawn chance multiplier used during a blood moon.",
                            "1.0 = no extra boost, 5.0 = five times the normal chance.")
                    .defineInRange("bloodMoonSpawnMultiplier", 5.0, 1.0, 50.0);

            bloodMoonZombiesPerSpawn = builder
                    .comment(
                            "How many mobs each custom wave tries to spawn during a blood moon.",
                            "This overrides the normal wave size while the blood moon is active.")
                    .defineInRange("bloodMoonZombiesPerSpawn", 4, 1, 100);
            builder.pop();

            builder.push("scaling");
            enableDifficultyScaling = builder
                    .comment(
                            "Enables day-based scaling so zombies get tougher as the world gets older.",
                            "This affects the legacy stat and gear progression system.")
                    .define("enableDifficultyScaling", true);

            scalingStartDay = builder
                    .comment(
                            "The first day where scaling starts to increase.",
                            "Before this day, the scaling factor stays at 0%.")
                    .defineInRange("scalingStartDay", 3, 0, 100);

            maxScalingDay = builder
                    .comment(
                            "The day where scaling reaches full strength.",
                            "After this day, the legacy scaling factor stays at 100%.")
                    .defineInRange("maxScalingDay", 50, 1, 365);

            maxSpeedBoost = builder
                    .comment(
                            "Maximum extra movement speed from the legacy scaling system at 100% scaling.",
                            "0.2 means up to 20% more movement speed.")
                    .defineInRange("maxSpeedBoost", 0.2, 0.0, 1.0);

            maxHealthBoost = builder
                    .comment(
                            "Maximum extra health added by the legacy scaling system at 100% scaling.",
                            "10 means up to 10 extra health points, which equals 5 extra hearts.")
                    .defineInRange("maxHealthBoost", 10, 0, 40);

            maxArmorChance = builder
                    .comment(
                            "Chance a scaled zombie gets armor at 100% scaling.",
                            "0.0 = never, 1.0 = always.")
                    .defineInRange("maxArmorChance", 0.3, 0.0, 1.0);

            maxWeaponChance = builder
                    .comment(
                            "Chance a scaled zombie gets a weapon at 100% scaling.",
                            "0.0 = never, 1.0 = always.")
                    .defineInRange("maxWeaponChance", 0.2, 0.0, 1.0);
            builder.pop();

            builder.push("attributes");
            enableAttributeModifiers = builder
                    .comment(
                            "Main switch for the advanced attribute system.",
                            "Applies to zombie-class mobs when they enter the world: zombies, husks, drowned, and zombie villagers.")
                    .define("enableAttributeModifiers", true);

            scaleAttributesWithDifficulty = builder
                    .comment(
                            "Makes the attribute scaling values below grow over time with the day-based difficulty factor.",
                            "The factor goes from 0.0 to 1.0 between scalingStartDay and maxScalingDay.")
                    .define("scaleAttributesWithDifficulty", true);

            enableVariantAttributeProfiles = builder
                    .comment(
                            "Lets each zombie type have its own stat profile on top of the base values.",
                            "Profiles exist for zombie, husk, drowned, and zombie villager.")
                    .define("enableVariantAttributeProfiles", true);

            enableBiomeDimensionAttributeMultipliers = builder
                    .comment(
                            "Lets biomes and dimensions further modify zombie stats.",
                            "Biome contexts: desert, water, mushroom. Dimension contexts: nether, end.",
                            "If more than one context matches, their multipliers stack together.")
                    .define("enableBiomeDimensionAttributeMultipliers", true);

            builder.push("base");
            baseHealthMultiplier = builder
                    .comment(
                            "Base max health multiplier for zombie-class mobs.",
                            "1.0 = vanilla-style value, 1.5 = 50% more health.",
                            "Typical vanilla zombie max health is 20.0.")
                    .defineInRange("baseHealthMultiplier", 1.0, 0.0, 10.0);

            baseHealthBonus = builder
                    .comment(
                            "Flat max health bonus added after the health multiplier.",
                            "0.0 = no extra health. Negative values reduce health.",
                            "Typical vanilla zombie max health is 20.0.")
                    .defineInRange("baseHealthBonus", 0.0, -200.0, 200.0);

            baseAttackMultiplier = builder
                    .comment(
                            "Base attack damage multiplier for zombie-class mobs.",
                            "1.0 = vanilla-style value, 1.25 = 25% more damage.",
                            "Typical vanilla zombie attack damage is 3.0.",
                            "Final damage can still vary with vanilla difficulty and effects.")
                    .defineInRange("baseAttackMultiplier", 1.0, 0.0, 10.0);

            baseAttackBonus = builder
                    .comment(
                            "Flat attack damage bonus added after the attack multiplier.",
                            "0.0 = no extra damage. Negative values reduce damage.",
                            "Typical vanilla zombie attack damage is 3.0.")
                    .defineInRange("baseAttackBonus", 0.0, -50.0, 50.0);

            baseSpeedMultiplier = builder
                    .comment(
                            "Base movement speed multiplier for zombie-class mobs.",
                            "1.0 = vanilla-style value, 1.2 = 20% faster.",
                            "Typical vanilla zombie movement speed is about 0.23.")
                    .defineInRange("baseSpeedMultiplier", 1.0, 0.0, 10.0);

            baseSpeedBonus = builder
                    .comment(
                            "Flat movement speed bonus added after the speed multiplier.",
                            "0.0 = no extra speed. Negative values reduce speed.",
                            "Typical vanilla zombie movement speed is about 0.23.")
                    .defineInRange("baseSpeedBonus", 0.0, -1.0, 1.0);

            baseArmorMultiplier = builder
                    .comment(
                            "Base armor multiplier for zombie-class mobs.",
                            "1.0 = vanilla-style value, 2.0 = double armor.",
                            "Typical vanilla zombie armor is 2.0.")
                    .defineInRange("baseArmorMultiplier", 1.0, 0.0, 10.0);

            baseArmorBonus = builder
                    .comment(
                            "Flat armor bonus added after the armor multiplier.",
                            "0.0 = no extra armor. Negative values reduce armor.",
                            "Typical vanilla zombie armor is 2.0.")
                    .defineInRange("baseArmorBonus", 0.0, -30.0, 30.0);

            baseFollowRangeMultiplier = builder
                    .comment(
                            "Base follow range multiplier for zombie-class mobs.",
                            "Higher values let mobs notice players from farther away.",
                            "Typical vanilla zombie follow range is 35.0.")
                    .defineInRange("baseFollowRangeMultiplier", 1.0, 0.0, 10.0);

            baseFollowRangeBonus = builder
                    .comment(
                            "Flat follow range bonus added after the follow range multiplier.",
                            "0.0 = no extra range. Negative values reduce the range.",
                            "Typical vanilla zombie follow range is 35.0.")
                    .defineInRange("baseFollowRangeBonus", 0.0, -100.0, 100.0);

            baseKnockbackResistanceMultiplier = builder
                    .comment(
                            "Base knockback resistance multiplier for zombie-class mobs.",
                            "Higher values make mobs harder to knock back.",
                            "Typical vanilla zombie knockback resistance is 0.0.")
                    .defineInRange("baseKnockbackResistanceMultiplier", 1.0, 0.0, 10.0);

            baseKnockbackResistanceBonus = builder
                    .comment(
                            "Flat knockback resistance bonus added after the multiplier.",
                            "0.0 = no extra resistance. Negative values reduce resistance.",
                            "Useful values are usually between 0.0 and 1.0.")
                    .defineInRange("baseKnockbackResistanceBonus", 0.0, -1.0, 1.0);
            builder.pop();

            builder.push("scaling");
            maxHealthScaleMultiplier = builder
                    .comment(
                            "Extra health multiplier added by the advanced attribute system at 100% difficulty.",
                            "0.0 = no extra scaling. 1.0 = up to 100% more health at full scaling.")
                    .defineInRange("maxHealthScaleMultiplier", 0.0, 0.0, 10.0);

            maxHealthScaleBonus = builder
                    .comment(
                            "Extra flat health bonus added by the advanced attribute system at 100% difficulty.",
                            "The full value is only reached when the difficulty factor reaches 1.0.")
                    .defineInRange("maxHealthScaleBonus", 0.0, -200.0, 200.0);

            maxAttackScaleMultiplier = builder
                    .comment(
                            "Extra attack damage multiplier added at 100% difficulty.",
                            "0.0 = no extra scaling. 1.0 = up to 100% more damage at full scaling.")
                    .defineInRange("maxAttackScaleMultiplier", 0.0, 0.0, 10.0);

            maxAttackScaleBonus = builder
                    .comment(
                            "Extra flat attack damage bonus added at 100% difficulty.",
                            "The full value is only reached when the difficulty factor reaches 1.0.")
                    .defineInRange("maxAttackScaleBonus", 0.0, -50.0, 50.0);

            maxSpeedScaleMultiplier = builder
                    .comment(
                            "Extra movement speed multiplier added at 100% difficulty.",
                            "0.0 = no extra scaling. 0.2 = up to 20% faster at full scaling.")
                    .defineInRange("maxSpeedScaleMultiplier", 0.0, 0.0, 10.0);

            maxSpeedScaleBonus = builder
                    .comment(
                            "Extra flat movement speed bonus added at 100% difficulty.",
                            "The full value is only reached when the difficulty factor reaches 1.0.")
                    .defineInRange("maxSpeedScaleBonus", 0.0, -1.0, 1.0);

            maxArmorScaleMultiplier = builder
                    .comment(
                            "Extra armor multiplier added at 100% difficulty.",
                            "0.0 = no extra scaling. 1.0 = up to 100% more armor at full scaling.")
                    .defineInRange("maxArmorScaleMultiplier", 0.0, 0.0, 10.0);

            maxArmorScaleBonus = builder
                    .comment(
                            "Extra flat armor bonus added at 100% difficulty.",
                            "The full value is only reached when the difficulty factor reaches 1.0.")
                    .defineInRange("maxArmorScaleBonus", 0.0, -30.0, 30.0);

            maxFollowRangeScaleMultiplier = builder
                    .comment(
                            "Extra follow range multiplier added at 100% difficulty.",
                            "Higher values let late-game mobs notice players from farther away.")
                    .defineInRange("maxFollowRangeScaleMultiplier", 0.0, 0.0, 10.0);

            maxFollowRangeScaleBonus = builder
                    .comment(
                            "Extra flat follow range bonus added at 100% difficulty.",
                            "The full value is only reached when the difficulty factor reaches 1.0.")
                    .defineInRange("maxFollowRangeScaleBonus", 0.0, -100.0, 100.0);

            maxKnockbackResistanceScaleMultiplier = builder
                    .comment(
                            "Extra knockback resistance multiplier added at 100% difficulty.",
                            "Higher values make late-game mobs harder to knock back.")
                    .defineInRange("maxKnockbackResistanceScaleMultiplier", 0.0, 0.0, 10.0);

            maxKnockbackResistanceScaleBonus = builder
                    .comment(
                            "Extra flat knockback resistance bonus added at 100% difficulty.",
                            "Useful values are usually between 0.0 and 1.0.")
                    .defineInRange("maxKnockbackResistanceScaleBonus", 0.0, -1.0, 1.0);
            builder.pop();

            builder.push("variants");

            builder.push("zombie");
            zombieHealthMultiplier = builder
                    .comment(
                            "Normal zombie health multiplier.",
                            "1.0 = no change. This stacks on top of attributes.base.* values.",
                            "Vanilla baseline reference: health 20.0, attack 3.0, speed 0.23, armor 2.0, follow range 35.0, knockback resistance 0.0.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            zombieHealthBonus = builder
                    .comment(bonusComment("Normal zombie max health"))
                    .defineInRange("healthBonus", 0.0, -200.0, 200.0);
            zombieAttackMultiplier = builder
                    .comment(multiplierComment("Normal zombie attack damage"))
                    .defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            zombieAttackBonus = builder
                    .comment(bonusComment("Normal zombie attack damage"))
                    .defineInRange("attackBonus", 0.0, -50.0, 50.0);
            zombieSpeedMultiplier = builder
                    .comment(multiplierComment("Normal zombie movement speed"))
                    .defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            zombieSpeedBonus = builder
                    .comment(bonusComment("Normal zombie movement speed"))
                    .defineInRange("speedBonus", 0.0, -1.0, 1.0);
            zombieArmorMultiplier = builder
                    .comment(multiplierComment("Normal zombie armor"))
                    .defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            zombieArmorBonus = builder
                    .comment(bonusComment("Normal zombie armor"))
                    .defineInRange("armorBonus", 0.0, -30.0, 30.0);
            zombieFollowRangeMultiplier = builder
                    .comment(multiplierComment("Normal zombie follow range"))
                    .defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            zombieFollowRangeBonus = builder
                    .comment(bonusComment("Normal zombie follow range"))
                    .defineInRange("followRangeBonus", 0.0, -100.0, 100.0);
            zombieKnockbackResistanceMultiplier = builder
                    .comment(multiplierComment("Normal zombie knockback resistance"))
                    .defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            zombieKnockbackResistanceBonus = builder
                    .comment(bonusComment("Normal zombie knockback resistance", "Useful values are usually between 0.0 and 1.0."))
                    .defineInRange("knockbackResistanceBonus", 0.0, -1.0, 1.0);
            builder.pop();

            builder.push("husk");
            huskHealthMultiplier = builder
                    .comment(
                            "Husk health multiplier.",
                            "1.0 = no change. This stacks on top of attributes.base.* values.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            huskHealthBonus = builder
                    .comment(bonusComment("Husk max health"))
                    .defineInRange("healthBonus", 0.0, -200.0, 200.0);
            huskAttackMultiplier = builder
                    .comment(multiplierComment("Husk attack damage"))
                    .defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            huskAttackBonus = builder
                    .comment(bonusComment("Husk attack damage"))
                    .defineInRange("attackBonus", 0.0, -50.0, 50.0);
            huskSpeedMultiplier = builder
                    .comment(multiplierComment("Husk movement speed"))
                    .defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            huskSpeedBonus = builder
                    .comment(bonusComment("Husk movement speed"))
                    .defineInRange("speedBonus", 0.0, -1.0, 1.0);
            huskArmorMultiplier = builder
                    .comment(multiplierComment("Husk armor"))
                    .defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            huskArmorBonus = builder
                    .comment(bonusComment("Husk armor"))
                    .defineInRange("armorBonus", 0.0, -30.0, 30.0);
            huskFollowRangeMultiplier = builder
                    .comment(multiplierComment("Husk follow range"))
                    .defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            huskFollowRangeBonus = builder
                    .comment(bonusComment("Husk follow range"))
                    .defineInRange("followRangeBonus", 0.0, -100.0, 100.0);
            huskKnockbackResistanceMultiplier = builder
                    .comment(multiplierComment("Husk knockback resistance"))
                    .defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            huskKnockbackResistanceBonus = builder
                    .comment(bonusComment("Husk knockback resistance", "Useful values are usually between 0.0 and 1.0."))
                    .defineInRange("knockbackResistanceBonus", 0.0, -1.0, 1.0);
            builder.pop();

            builder.push("drowned");
            drownedHealthMultiplier = builder
                    .comment(
                            "Drowned health multiplier.",
                            "1.0 = no change. This stacks on top of attributes.base.* values.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            drownedHealthBonus = builder
                    .comment(bonusComment("Drowned max health"))
                    .defineInRange("healthBonus", 0.0, -200.0, 200.0);
            drownedAttackMultiplier = builder
                    .comment(multiplierComment("Drowned attack damage"))
                    .defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            drownedAttackBonus = builder
                    .comment(bonusComment("Drowned attack damage"))
                    .defineInRange("attackBonus", 0.0, -50.0, 50.0);
            drownedSpeedMultiplier = builder
                    .comment(multiplierComment("Drowned movement speed"))
                    .defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            drownedSpeedBonus = builder
                    .comment(bonusComment("Drowned movement speed"))
                    .defineInRange("speedBonus", 0.0, -1.0, 1.0);
            drownedArmorMultiplier = builder
                    .comment(multiplierComment("Drowned armor"))
                    .defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            drownedArmorBonus = builder
                    .comment(bonusComment("Drowned armor"))
                    .defineInRange("armorBonus", 0.0, -30.0, 30.0);
            drownedFollowRangeMultiplier = builder
                    .comment(multiplierComment("Drowned follow range"))
                    .defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            drownedFollowRangeBonus = builder
                    .comment(bonusComment("Drowned follow range"))
                    .defineInRange("followRangeBonus", 0.0, -100.0, 100.0);
            drownedKnockbackResistanceMultiplier = builder
                    .comment(multiplierComment("Drowned knockback resistance"))
                    .defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            drownedKnockbackResistanceBonus = builder
                    .comment(bonusComment("Drowned knockback resistance", "Useful values are usually between 0.0 and 1.0."))
                    .defineInRange("knockbackResistanceBonus", 0.0, -1.0, 1.0);
            builder.pop();

            builder.push("zombieVillager");
            zombieVillagerHealthMultiplier = builder
                    .comment(
                            "Zombie villager health multiplier.",
                            "1.0 = no change. This stacks on top of attributes.base.* values.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            zombieVillagerHealthBonus = builder
                    .comment(bonusComment("Zombie villager max health"))
                    .defineInRange("healthBonus", 0.0, -200.0, 200.0);
            zombieVillagerAttackMultiplier = builder
                    .comment(multiplierComment("Zombie villager attack damage"))
                    .defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            zombieVillagerAttackBonus = builder
                    .comment(bonusComment("Zombie villager attack damage"))
                    .defineInRange("attackBonus", 0.0, -50.0, 50.0);
            zombieVillagerSpeedMultiplier = builder
                    .comment(multiplierComment("Zombie villager movement speed"))
                    .defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            zombieVillagerSpeedBonus = builder
                    .comment(bonusComment("Zombie villager movement speed"))
                    .defineInRange("speedBonus", 0.0, -1.0, 1.0);
            zombieVillagerArmorMultiplier = builder
                    .comment(multiplierComment("Zombie villager armor"))
                    .defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            zombieVillagerArmorBonus = builder
                    .comment(bonusComment("Zombie villager armor"))
                    .defineInRange("armorBonus", 0.0, -30.0, 30.0);
            zombieVillagerFollowRangeMultiplier = builder
                    .comment(multiplierComment("Zombie villager follow range"))
                    .defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            zombieVillagerFollowRangeBonus = builder
                    .comment(bonusComment("Zombie villager follow range"))
                    .defineInRange("followRangeBonus", 0.0, -100.0, 100.0);
            zombieVillagerKnockbackResistanceMultiplier = builder
                    .comment(multiplierComment("Zombie villager knockback resistance"))
                    .defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            zombieVillagerKnockbackResistanceBonus = builder
                    .comment(bonusComment("Zombie villager knockback resistance", "Useful values are usually between 0.0 and 1.0."))
                    .defineInRange("knockbackResistanceBonus", 0.0, -1.0, 1.0);
            builder.pop();
            builder.pop();

            builder.push("contexts");

            builder.push("desert");
            desertHealthMultiplier = builder
                    .comment(
                            "Health multiplier applied when the mob spawns in desert or badlands biomes.",
                            "1.0 = no change. Values above 1.0 make those mobs tougher.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            desertAttackMultiplier = builder
                    .comment(multiplierComment("Desert and badlands attack damage"))
                    .defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            desertSpeedMultiplier = builder
                    .comment(multiplierComment("Desert and badlands movement speed"))
                    .defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            desertArmorMultiplier = builder
                    .comment(multiplierComment("Desert and badlands armor"))
                    .defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            desertFollowRangeMultiplier = builder
                    .comment(multiplierComment("Desert and badlands follow range"))
                    .defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            desertKnockbackResistanceMultiplier = builder
                    .comment(multiplierComment("Desert and badlands knockback resistance"))
                    .defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            builder.pop();

            builder.push("water");
            waterHealthMultiplier = builder
                    .comment(
                            "Health multiplier applied when the mob spawns in ocean, river, or swamp biomes.",
                            "Use this to make water-heavy areas easier or harder than normal.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            waterAttackMultiplier = builder
                    .comment(multiplierComment("Water-biome attack damage"))
                    .defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            waterSpeedMultiplier = builder
                    .comment(multiplierComment("Water-biome movement speed"))
                    .defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            waterArmorMultiplier = builder
                    .comment(multiplierComment("Water-biome armor"))
                    .defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            waterFollowRangeMultiplier = builder
                    .comment(multiplierComment("Water-biome follow range"))
                    .defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            waterKnockbackResistanceMultiplier = builder
                    .comment(multiplierComment("Water-biome knockback resistance"))
                    .defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            builder.pop();

            builder.push("mushroom");
            mushroomHealthMultiplier = builder
                    .comment(
                            "Health multiplier applied when the mob spawns in mushroom fields.",
                            "Useful if you want mushroom areas to stay safer or become a trap instead.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            mushroomAttackMultiplier = builder
                    .comment(multiplierComment("Mushroom-biome attack damage"))
                    .defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            mushroomSpeedMultiplier = builder
                    .comment(multiplierComment("Mushroom-biome movement speed"))
                    .defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            mushroomArmorMultiplier = builder
                    .comment(multiplierComment("Mushroom-biome armor"))
                    .defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            mushroomFollowRangeMultiplier = builder
                    .comment(multiplierComment("Mushroom-biome follow range"))
                    .defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            mushroomKnockbackResistanceMultiplier = builder
                    .comment(multiplierComment("Mushroom-biome knockback resistance"))
                    .defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            builder.pop();

            builder.push("nether");
            netherHealthMultiplier = builder
                    .comment(
                            "Health multiplier applied when the mob spawns in the Nether.",
                            "Use this to give Nether zombie spawns their own difficulty profile.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            netherAttackMultiplier = builder
                    .comment(multiplierComment("Nether attack damage"))
                    .defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            netherSpeedMultiplier = builder
                    .comment(multiplierComment("Nether movement speed"))
                    .defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            netherArmorMultiplier = builder
                    .comment(multiplierComment("Nether armor"))
                    .defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            netherFollowRangeMultiplier = builder
                    .comment(multiplierComment("Nether follow range"))
                    .defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            netherKnockbackResistanceMultiplier = builder
                    .comment(multiplierComment("Nether knockback resistance"))
                    .defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            builder.pop();

            builder.push("end");
            endHealthMultiplier = builder
                    .comment(
                            "Health multiplier applied when the mob spawns in the End.",
                            "Use this to give End zombie spawns their own difficulty profile.")
                    .defineInRange("healthMultiplier", 1.0, 0.0, 10.0);
            endAttackMultiplier = builder
                    .comment(multiplierComment("End attack damage"))
                    .defineInRange("attackMultiplier", 1.0, 0.0, 10.0);
            endSpeedMultiplier = builder
                    .comment(multiplierComment("End movement speed"))
                    .defineInRange("speedMultiplier", 1.0, 0.0, 10.0);
            endArmorMultiplier = builder
                    .comment(multiplierComment("End armor"))
                    .defineInRange("armorMultiplier", 1.0, 0.0, 10.0);
            endFollowRangeMultiplier = builder
                    .comment(multiplierComment("End follow range"))
                    .defineInRange("followRangeMultiplier", 1.0, 0.0, 10.0);
            endKnockbackResistanceMultiplier = builder
                    .comment(multiplierComment("End knockback resistance"))
                    .defineInRange("knockbackResistanceMultiplier", 1.0, 0.0, 10.0);
            builder.pop();

            builder.pop();
            builder.pop();

            builder.push("nightspawning");
            enableNightBoost = builder
                    .comment(
                            "Adds an extra spawn chance boost at night.",
                            "This stacks on top of the normal spawn settings.")
                    .define("enableNightBoost", true);

            nightSpawnMultiplier = builder
                    .comment(
                            "Extra spawn chance multiplier used at night.",
                            "1.0 = no extra boost, 1.5 = 50% more chance at night.")
                    .defineInRange("nightSpawnMultiplier", 1.5, 1.0, 10.0);
            builder.pop();

            builder.push("biomes");
            enableBiomeModifiers = builder
                    .comment(
                            "Lets biome type influence which zombie variants spawn more often.",
                            "Example: more husks in deserts and more drowned in wet biomes.")
                    .define("enableBiomeModifiers", true);

            desertHuskBonus = builder
                    .comment(
                            "Extra husk chance added in desert and badlands biomes.",
                            "0.5 adds 50 percentage points to the normal husk chance in those biomes.")
                    .defineInRange("desertHuskBonus", 0.5, 0.0, 1.0);

            waterDrownedBonus = builder
                    .comment(
                            "Extra drowned chance added in ocean, river, and swamp biomes.",
                            "0.4 adds 40 percentage points to the normal drowned chance in those biomes.")
                    .defineInRange("waterDrownedBonus", 0.4, 0.0, 1.0);

            mushroomSafeZone = builder
                    .comment(
                            "If true, the mod will not do custom spawning in mushroom fields.",
                            "This lets mushroom biomes act like a safer refuge.")
                    .define("mushroomSafeZone", true);

            netherSpawning = builder
                    .comment(
                            "Allows the mod's custom spawning system to work in the Nether.",
                            "If false, Nether zombie pressure comes only from normal vanilla spawning.")
                    .define("netherSpawning", false);

            endSpawning = builder
                    .comment(
                            "Allows the mod's custom spawning system to work in the End.",
                            "If false, End zombie pressure comes only from normal vanilla spawning.")
                    .define("endSpawning", false);
            builder.pop();

            builder.push("deathcooldown");
            enableDeathCooldown = builder
                    .comment(
                            "Temporarily lowers custom spawn pressure around players after they die.",
                            "This helps prevent repeated instant-death loops.")
                    .define("enableDeathCooldown", true);

            deathCooldownSeconds = builder
                    .comment(
                            "How long the death cooldown lasts after a player dies.",
                            "30 means the reduced pressure lasts for 30 seconds.")
                    .defineInRange("deathCooldownSeconds", 30, 5, 600);

            cooldownSpawnReduction = builder
                    .comment(
                            "How much the custom spawn chance is reduced during death cooldown.",
                            "0.5 = cut the chance in half, 1.0 = remove all custom spawn chance during the cooldown.")
                    .defineInRange("cooldownSpawnReduction", 0.5, 0.0, 1.0);
            builder.pop();

            builder.push("effects");
            enableSpawnEffects = builder
                    .comment(
                            "Enables extra sound and particle feedback when custom spawns happen.",
                            "Purely cosmetic.")
                    .define("enableSpawnEffects", true);

            spawnSound = builder
                    .comment(
                            "Plays a zombie sound when the mod creates a custom spawn wave.",
                            "Only matters if enableSpawnEffects is true.")
                    .define("spawnSound", true);

            spawnParticles = builder
                    .comment(
                            "Shows smoke particles when the mod creates a custom spawn wave.",
                            "Only matters if enableSpawnEffects is true.")
                    .define("spawnParticles", true);
            builder.pop();

            builder.push("statistics");
            enableStatistics = builder
                    .comment(
                            "Tracks zombie kill totals shown by /zstats.",
                            "Milestone advancements keep their own progress so achievements can still work if this is off.")
                    .define("enableStatistics", true);
            builder.pop();

            builder.push("drops");
            boneChance = builder
                    .comment(
                            "Chance for an extra bone to drop from a zombie-class mob.",
                            "0.15 = 15% chance.")
                    .defineInRange("boneChance", 0.15, 0.0, 1.0);

            stringChance = builder
                    .comment(
                            "Chance for an extra string to drop from a zombie-class mob.",
                            "0.15 = 15% chance.")
                    .defineInRange("stringChance", 0.15, 0.0, 1.0);

            gunpowderChance = builder
                    .comment(
                            "Chance for an extra gunpowder to drop from a zombie-class mob.",
                            "0.10 = 10% chance.")
                    .defineInRange("gunpowderChance", 0.10, 0.0, 1.0);

            enderPearlChance = builder
                    .comment(
                            "Chance for an extra ender pearl to drop from a zombie-class mob.",
                            "0.02 = 2% chance.")
                    .defineInRange("enderPearlChance", 0.02, 0.0, 1.0);

            phantomMembraneChance = builder
                    .comment(
                            "Chance for an extra phantom membrane to drop from a zombie-class mob.",
                            "0.03 = 3% chance.")
                    .defineInRange("phantomMembraneChance", 0.03, 0.0, 1.0);
            builder.pop();
        }

        private static String[] multiplierComment(String label) {
            return new String[] {
                    label + " multiplier.",
                    "1.0 = no change. Higher values increase it and lower values reduce it."
            };
        }

        private static String[] bonusComment(String label) {
            return bonusComment(label, null);
        }

        private static String[] bonusComment(String label, String extraLine) {
            if (extraLine == null || extraLine.isBlank()) {
                return new String[] {
                        label + " flat bonus added after the multiplier.",
                        "0.0 = no extra bonus. Negative values reduce the final value."
                };
            }

            return new String[] {
                    label + " flat bonus added after the multiplier.",
                    "0.0 = no extra bonus. Negative values reduce the final value.",
                    extraLine
            };
        }
    }
}
