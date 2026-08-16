package com.rique.zombieapocalypse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public final class Config {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;
    private static final ThreadLocal<Map<List<String>, Object>> PENDING_CHANGES = new ThreadLocal<>();
    private static volatile ModConfig loadedConfig;

    static {
        Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    private Config() {
    }

    public static void bind(ModConfig config) {
        if (config.getSpec() == COMMON_SPEC) {
            loadedConfig = config;
        }
    }

    public static <T> void set(ForgeConfigSpec.ConfigValue<T> setting, T value) {
        ForgeConfigSpec.ValueSpec valueSpec = COMMON_SPEC.getSpec().get(setting.getPath());
        if (valueSpec != null && !valueSpec.test(value)) {
            throw new IllegalArgumentException("Value is outside the allowed range.");
        }

        Map<List<String>, Object> pending = PENDING_CHANGES.get();
        if (pending == null) {
            edit(() -> set(setting, value));
            return;
        }
        pending.put(List.copyOf(setting.getPath()), value);
    }

    public static void edit(Runnable changes) {
        if (PENDING_CHANGES.get() != null) {
            changes.run();
            return;
        }

        Map<List<String>, Object> pending = new LinkedHashMap<>();
        PENDING_CHANGES.set(pending);
        try {
            changes.run();
            applyChanges(pending);
        } finally {
            PENDING_CHANGES.remove();
        }
    }

    private static synchronized void applyChanges(Map<List<String>, Object> changes) {
        if (changes.isEmpty()) {
            return;
        }

        ModConfig config = loadedConfig;
        if (config == null || config.getConfigData() == null) {
            throw new IllegalStateException("Zombie Apocalypse config is not loaded yet.");
        }

        try (CommentedFileConfig file = CommentedFileConfig.builder(config.getFullPath())
                .sync()
                .preserveInsertionOrder()
                .writingMode(WritingMode.REPLACE)
                .build()) {
            file.load();
            changes.forEach(file::set);
            file.save();
        }

        CommentedFileConfig loaded = (CommentedFileConfig) config.getConfigData();
        loaded.load();
        COMMON_SPEC.setConfig(loaded);
        COMMON_SPEC.afterReload();
    }

    public static final class Common {

        // General
        public final ForgeConfigSpec.BooleanValue preventSunBurn;
        public final ForgeConfigSpec.BooleanValue enableExtraDrops;
        public final ForgeConfigSpec.BooleanValue enableDebugLogging;

        // Mod compatibility
        public final ForgeConfigSpec.BooleanValue enableModdedZombieCompatibility;
        public final ForgeConfigSpec.BooleanValue applyDifficultyToModdedZombies;
        public final ForgeConfigSpec.BooleanValue applyAiFeaturesToModdedZombies;
        public final ForgeConfigSpec.BooleanValue respectExternalSpawnRules;
        public final ForgeConfigSpec.BooleanValue respectExternalZombieAi;
        public final ForgeConfigSpec.BooleanValue respectExternalDifficulty;
        public final ForgeConfigSpec.BooleanValue respectZombieDoorBreakingAbility;
        public final ForgeConfigSpec.BooleanValue preserveExistingZombieEquipment;
        public final ForgeConfigSpec.ConfigValue<String> additionalZombieEntityTypes;
        public final ForgeConfigSpec.ConfigValue<String> excludedZombieEntityTypes;

        // Day spawning core
        public final ForgeConfigSpec.BooleanValue enableDaySpawning;
        public final ForgeConfigSpec.BooleanValue enableDaytimeSpawning;
        public final ForgeConfigSpec.IntValue daySpawnInterval;
        public final ForgeConfigSpec.DoubleValue daySpawnChance;
        public final ForgeConfigSpec.IntValue maxDayZombiesPerPlayer;
        public final ForgeConfigSpec.IntValue zombiesPerSpawn;
        public final ForgeConfigSpec.IntValue spawnRange;
        public final ForgeConfigSpec.IntValue minSpawnDistance;
        public final ForgeConfigSpec.IntValue spawnAttemptsPerZombie;
        public final ForgeConfigSpec.BooleanValue requireOpenSkyForOverworldSpawns;
        public final ForgeConfigSpec.IntValue maxBlockLightForSpawning;
        public final ForgeConfigSpec.IntValue daylightSpawnStartDay;

        // Zombie variants
        public final ForgeConfigSpec.BooleanValue enableZombieVariants;
        public final ForgeConfigSpec.DoubleValue huskChance;
        public final ForgeConfigSpec.DoubleValue drownedChance;
        public final ForgeConfigSpec.DoubleValue babyZombieChance;
        public final ForgeConfigSpec.DoubleValue zombieVillagerChance;

        // Zombie block breaking
        public final ForgeConfigSpec.BooleanValue enableZombieBlockBreaking;
        public final ForgeConfigSpec.IntValue zombieBlockBreakingStartDay;
        public final ForgeConfigSpec.IntValue zombieBlockBreakingInterval;
        public final ForgeConfigSpec.DoubleValue zombieBlockBreakingChance;
        public final ForgeConfigSpec.IntValue zombieBlockBreakingRange;
        public final ForgeConfigSpec.DoubleValue zombieBlockBreakingMaxHardness;
        public final ForgeConfigSpec.BooleanValue zombieBlockBreakingDropBlocks;
        public final ForgeConfigSpec.BooleanValue zombieBlockBreakingRequireTarget;
        public final ForgeConfigSpec.BooleanValue zombieBlockBreakingRequireObstacle;
        public final ForgeConfigSpec.BooleanValue zombieBlockBreakingRespectMobGriefing;
        public final ForgeConfigSpec.BooleanValue zombieBlockBreakingAllowBlockEntities;
        public final ForgeConfigSpec.BooleanValue zombieBlockBreakingAllowToolRequiredBlocks;
        public final ForgeConfigSpec.BooleanValue zombieBlockBreakingAllowLightBlocks;

        // Zombie block placing
        public final ForgeConfigSpec.BooleanValue enableZombieBlockPlacing;
        public final ForgeConfigSpec.IntValue zombieBlockPlacingStartDay;
        public final ForgeConfigSpec.IntValue zombieBlockPlacingInterval;
        public final ForgeConfigSpec.DoubleValue zombieBlockPlacingChance;
        public final ForgeConfigSpec.ConfigValue<String> zombieBlockPlacingBlock;
        public final ForgeConfigSpec.IntValue zombieBlockPlacingMaxPerZombie;
        public final ForgeConfigSpec.IntValue zombieBlockPlacingMaxTargetDistance;
        public final ForgeConfigSpec.BooleanValue zombieBlockPlacingRequireTarget;
        public final ForgeConfigSpec.BooleanValue zombieBlockPlacingRequireObstacle;
        public final ForgeConfigSpec.BooleanValue zombieBlockPlacingRespectMobGriefing;
        public final ForgeConfigSpec.BooleanValue zombieBlockPlacingAllowBridges;
        public final ForgeConfigSpec.BooleanValue zombieBlockPlacingAllowSteps;
        public final ForgeConfigSpec.BooleanValue zombieBlockPlacingReplaceFluids;
        public final ForgeConfigSpec.BooleanValue zombieBlockPlacingReplaceReplaceableBlocks;

        // Zombie towering
        public final ForgeConfigSpec.BooleanValue enableZombieTowering;
        public final ForgeConfigSpec.IntValue zombieToweringStartDay;
        public final ForgeConfigSpec.IntValue zombieToweringInterval;
        public final ForgeConfigSpec.DoubleValue zombieToweringChance;
        public final ForgeConfigSpec.IntValue zombieToweringMaxTargetDistance;
        public final ForgeConfigSpec.IntValue zombieToweringMinNearbyZombies;
        public final ForgeConfigSpec.DoubleValue zombieToweringCrowdRadius;
        public final ForgeConfigSpec.IntValue zombieToweringMaxStackSize;
        public final ForgeConfigSpec.IntValue zombieToweringMaxTowersPerPlayer;
        public final ForgeConfigSpec.BooleanValue zombieToweringDynamicHeightEnabled;
        public final ForgeConfigSpec.IntValue zombieToweringTargetHeightOffset;
        public final ForgeConfigSpec.BooleanValue zombieToweringSmartDismountEnabled;
        public final ForgeConfigSpec.BooleanValue zombieToweringJumpingEnabled;
        public final ForgeConfigSpec.IntValue zombieToweringJumpCooldownTicks;
        public final ForgeConfigSpec.DoubleValue zombieToweringDismountDistance;
        public final ForgeConfigSpec.DoubleValue zombieToweringVerticalBoost;
        public final ForgeConfigSpec.DoubleValue zombieToweringForwardBoost;
        public final ForgeConfigSpec.IntValue zombieToweringMaxHeightAboveTarget;
        public final ForgeConfigSpec.BooleanValue zombieToweringRequireObstacle;

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
            builder.comment(sectionComment(
                    "START HERE",
                    "Most servers only need the [dayspawning], [variants], [horde], [bloodmoon], and [scaling] sections.",
                    "Fast setup in game: /za preset casual, /za preset standard, or /za preset hardcore.",
                    "Check the important live settings with /za status. Use /za help for short command topics.",
                    "20 ticks = 1 second. Chances use decimals: 0.25 = 25%, 0.50 = 50%, and 1.0 = 100%.",
                    "Stop the server before editing this file manually. Advanced settings are safe to leave at their defaults."))
                    .push("general");
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

            builder.comment(sectionComment(
                    "MOD COMPATIBILITY",
                    "Controls how this addon cooperates with other zombie, difficulty, AI, and spawn-control mods.",
                    "Beginner advice: skip this section and keep every default.",
                    "Quick reset: /za compatibility on restores the recommended safeguards.",
                    "No compatibility option creates a hard dependency. Missing mods and missing optional entity IDs are ignored."))
                    .push("compatibility");
            enableModdedZombieCompatibility = builder
                    .comment(
                            "Recognizes modded entities that extend Minecraft's normal Zombie class.",
                            "This lets them count toward caps and cleanup and receive configured drops, kill credit, and sunlight handling.",
                            "false = only vanilla zombie, husk, drowned, zombie villager, and explicitly listed/tagged entities are recognized.")
                    .define("enableModdedZombieCompatibility", true);

            applyDifficultyToModdedZombies = builder
                    .comment(
                            "Allows this addon's attribute and day-scaling pipeline to affect recognized modded zombies.",
                            "Disable this if another mod should have complete control over the stats of its own zombie variants.",
                            "The addon still marks each entity after one application, so its own scaling is never applied twice.")
                    .define("applyDifficultyToModdedZombies", true);

            applyAiFeaturesToModdedZombies = builder
                    .comment(
                            "Allows recognized modded Zombie subclasses to use enabled block breaking, block placing, and towering features.",
                            "This does nothing while those individual systems are disabled.",
                            "Disable it if a custom zombie has movement or destruction behavior that should remain untouched.")
                    .define("applyAiFeaturesToModdedZombies", true);

            respectExternalSpawnRules = builder
                    .comment(
                            "Lets other mods veto or adjust this addon's custom spawns through the loader's normal mob-spawn events.",
                            "Keep this true for spawn-control mods such as In Control! and Bad Mobs.",
                            "The addon's daylight and block-light rules still decide the normal default result.")
                    .define("respectExternalSpawnRules", true);

            respectExternalZombieAi = builder
                    .comment(
                            "Avoids running this addon's optional destructive/movement AI on mobs already managed by a known AI overhaul.",
                            "This prevents duplicate block breaking, leaps, or towering with Zombies Reworked, Improved Mobs, and Undead Nights.",
                            "Set false only when you intentionally want both mods' AI systems to stack.")
                    .define("respectExternalZombieAi", true);

            respectExternalDifficulty = builder
                    .comment(
                            "Avoids stacking this addon's attribute scaling on mobs controlled by a known external difficulty system.",
                            "Improved Mobs and specially tagged event zombies keep their own intended stats when this is true.",
                            "Set false if you intentionally want both difficulty systems to multiply together.")
                    .define("respectExternalDifficulty", true);

            respectZombieDoorBreakingAbility = builder
                    .comment(
                            "Stops the addon's block breaker from destroying doors when that zombie is not allowed to break doors.",
                            "Keep this true so Zombie Proof Doors and vanilla door-breaking rules remain authoritative.",
                            "Set false only if the addon's block-breaking feature should ignore that protection.")
                    .define("respectZombieDoorBreakingAbility", true);

            preserveExistingZombieEquipment = builder
                    .comment(
                            "Prevents difficulty scaling from replacing weapons or armor a zombie already has.",
                            "Keep this true for specialized mobs such as archers, sword zombies, bosses, and modded variants.",
                            "The addon may still fill an empty equipment slot. false restores the old overwrite behavior.")
                    .define("preserveExistingZombieEquipment", true);

            additionalZombieEntityTypes = builder
                    .comment(
                            "Optional comma-separated entity IDs to treat as zombie-class even if they do not extend Zombie.",
                            "Example: minecraft:giant,examplemod:infected_mob",
                            "Use exact namespace:path IDs. Invalid or missing IDs are ignored and reported when debug logging is enabled.",
                            "Most zombie mods do not need this because Zombie subclasses are detected automatically.")
                    .define("additionalZombieEntityTypes", "");

            excludedZombieEntityTypes = builder
                    .comment(
                            "Optional comma-separated entity IDs that this addon must never treat as zombie-class.",
                            "Exclusions win over automatic subclass detection, tags, and the additional list.",
                            "Example: examplemod:zombie_boss,examplemod:friendly_zombie")
                    .define("excludedZombieEntityTypes", "");
            builder.pop();

            builder.comment(sectionComment(
                    "CUSTOM DAY/NIGHT SPAWNING",
                    "This is the main pressure system. It creates extra zombie waves around survival players.",
                    "Safe beginner setup: keep the defaults, then lower daySpawnChance if the world feels too packed.",
                    "Quick setup: /za spawn on loads the standard day/night spawning preset.",
                    "Use enableDaytimeSpawning = false for permanent night-only custom waves.",
                    "Use maxBlockLightForSpawning if you want player-placed lights to protect bases from custom waves.",
                    "Performance warning: low intervals, high wave size, high caps, and high attempts can lag weak servers.",
                    "Hardcore setup: increase chance/amount slowly, then test with multiple players before publishing a pack."))
                    .push("dayspawning");
            enableDaySpawning = builder
                    .comment(
                            "Main on/off switch for all custom zombie spawning added by this mod.",
                            "If false, the mod stops creating its own zombie waves around players.",
                            "Active or queued horde and blood moon pressure is canceled because those events use custom waves.",
                            "Difficulty, sunlight, drops, stats, optional siege AI, and the morning day counter stay independent.")
                    .define("enableDaySpawning", true);

            enableDaytimeSpawning = builder
                    .comment(
                            "Controls custom waves during daytime in dimensions with a normal day/night cycle.",
                            "true = custom waves can spawn during both day and night.",
                            "false = night-only mode. Night waves and blood moons still work.",
                            "Daytime horde waves are blocked, scheduled dawn hordes are paused, and /zhorde start is rejected during the day.",
                            "The Nether, End, and other fixed-time dimensions still use their own dimension toggles.",
                            "daylightSpawnStartDay is only used while this setting is true.",
                            "Applying a gameplay preset turns daytime custom spawning back on.")
                    .define("enableDaytimeSpawning", true);

            daySpawnInterval = builder
                    .comment(
                            "How often the mod checks whether to spawn a custom wave.",
                            "20 ticks = 1 second.",
                            "Default 120 = one check every 6 seconds.",
                            "Lower numbers mean more checks and more CPU use. 20 or lower is very aggressive.")
                    .defineInRange("daySpawnInterval", 120, 1, 72000);

            daySpawnChance = builder
                    .comment(
                            "Chance that each spawn check turns into a real wave.",
                            "0.0 = never spawn, 1.0 = every check spawns a wave.",
                            "High chance plus a low interval can flood an area quickly.")
                    .defineInRange("daySpawnChance", 0.5, 0.0, 1.0);

            maxDayZombiesPerPlayer = builder
                    .comment(
                            "Maximum nearby zombie-class mobs allowed around each player before new custom spawns are skipped.",
                            "This helps stop overcrowding and keeps performance under control.",
                            "Beginner: 30-60 is usually sane. Raising this above 100 is for stronger servers.")
                    .defineInRange("maxDayZombiesPerPlayer", 50, 1, 500);

            zombiesPerSpawn = builder
                    .comment(
                            "How many mobs the mod tries to place in each successful wave.",
                            "The real number can be lower if it cannot find enough valid spawn spots.",
                            "High values feel like mini-hordes and can spike lag if many players are online.")
                    .defineInRange("zombiesPerSpawn", 2, 1, 50);

            spawnRange = builder
                    .comment(
                            "How far from each player custom spawns are allowed to appear horizontally.",
                            "Higher values spread mobs farther out and can make them feel less concentrated.",
                            "Very high values make the mod check a wider area around every player.")
                    .defineInRange("spawnRange", 30, 16, 128);

            minSpawnDistance = builder
                    .comment(
                            "Closest distance a custom spawn is allowed to appear from a player.",
                            "Raise this if mobs feel like they are popping in too close.",
                            "This must still fit inside the square made by spawnRange.",
                            "Example: range 30 supports at most about 42 blocks diagonally. A larger minimum pauses custom spawning.")
                    .defineInRange("minSpawnDistance", 12, 8, 64);

            spawnAttemptsPerZombie = builder
                    .comment(
                            "How many location tries the mod makes for each mob in a wave.",
                            "Higher values help waves fill more reliably, but they also use more CPU time.",
                            "Beginner: 5-15 is normal. Do not max this out unless spawn placement is failing badly.")
                    .defineInRange("spawnAttemptsPerZombie", 10, 1, 40);

            requireOpenSkyForOverworldSpawns = builder
                    .comment(
                            "If true, overworld custom spawns only happen where the sky is open.",
                            "Turn this off to search near the player's height for caves, interiors, and covered areas before using the surface.",
                            "Block-light protection still prevents covered spawns that are brighter than maxBlockLightForSpawning.")
                    .define("requireOpenSkyForOverworldSpawns", true);

            maxBlockLightForSpawning = builder
                    .comment(
                            "Maximum block light level that still allows custom zombie spawning.",
                            "This only affects the mod's custom spawn waves. It does not change vanilla natural spawning.",
                            "Block light means light from blocks like torches, lanterns, glowstone, campfires, and similar sources.",
                            "Light never makes custom spawns more likely. This setting can only block spawns when the area is too bright.",
                            "-1 = ignore block light and keep the old behavior, so lit bases are not protected from custom waves.",
                            "0 = custom waves only spawn in complete block darkness.",
                            "7 = classic hostile-mob style limit and a good choice if players expect torches to protect bases.",
                            "15 = any block light is allowed, which is almost the same as ignoring this check.",
                            "Sunlight is ignored on purpose so daytime apocalypse spawning still works.")
                    .defineInRange("maxBlockLightForSpawning", -1, -1, 15);

            daylightSpawnStartDay = builder
                    .comment(
                            "Temporary grace period: blocks daytime custom spawning until the world reaches this day number.",
                            "0 = daytime spawning starts immediately.",
                            "Nighttime custom spawning still works before this day.",
                            "This value is kept but ignored while enableDaytimeSpawning is false.",
                            "Use enableDaytimeSpawning = false for permanent night-only spawning.")
                    .defineInRange("daylightSpawnStartDay", 0, 0, ConfigLimits.MAX_APOCALYPSE_DAY);
            builder.pop();

            builder.comment(sectionComment(
                    "ZOMBIE VARIANTS",
                    "Controls which zombie-type mobs can appear in custom waves.",
                    "Safe beginner setup: defaults add variety without making every spawn a special mob.",
                    "If the numbers add up high, the mod normalizes them so the total chance stays safe."))
                    .push("variants");
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
                            "Chance for a custom spawned zombie-type mob to be a baby when that mob type supports it.",
                            "Set this to 0.0 to force new zombie-class spawns to stay adults.",
                            "The 0.0 adult-only rule also catches new vanilla and modded Zombie subclasses spawned outside custom waves.",
                            "1.0 = every supported custom spawn is a baby.")
                    .defineInRange("babyZombieChance", 0.05, 0.0, 1.0);

            zombieVillagerChance = builder
                    .comment(
                            "Base chance for a custom spawn to become a zombie villager.",
                            "Any remaining chance after the variant rolls becomes a normal zombie.")
                    .defineInRange("zombieVillagerChance", 0.05, 0.0, 1.0);
            builder.pop();

            builder.comment(sectionComment(
                    "ZOMBIE BLOCK BREAKING",
                    "Optional destructive behavior for zombie-class mobs.",
                    "Safe beginner setup: leave this disabled unless your server wants base pressure.",
                    "Quick setup: /za breaking on enables a protected, balanced preset immediately.",
                    "When enabled, zombies only try on a timer and chance roll, so this avoids every-zombie every-tick scans.",
                    "The defaults protect chests, furnaces, modded machines, light sources, tool-required blocks, and unbreakable blocks.",
                    "Use start day 0 if you want it active immediately after enabling."))
                    .push("blockbreaking");
            enableZombieBlockBreaking = builder
                    .comment(
                            "Main on/off switch for zombie block breaking.",
                            "false = zombies never break blocks from this mod.",
                            "true = zombie-class mobs can break blocks after zombieBlockBreakingStartDay if the other rules allow it.")
                    .define("enableZombieBlockBreaking", false);

            zombieBlockBreakingStartDay = builder
                    .comment(
                            "Apocalypse day when block breaking is allowed to start after the feature is enabled.",
                            "0 = active immediately on day 0/day 1 style worlds.",
                            "10 = zombies cannot break blocks until the day counter reaches 10.")
                    .defineInRange("zombieBlockBreakingStartDay", 10, 0, ConfigLimits.MAX_APOCALYPSE_DAY);

            zombieBlockBreakingInterval = builder
                    .comment(
                            "How often each zombie can attempt block breaking.",
                            "20 ticks = 1 second.",
                            "Default 100 = each zombie checks at most once every 5 seconds.",
                            "Lower values feel more aggressive but cost more CPU on crowded servers.")
                    .defineInRange("zombieBlockBreakingInterval", 100, 20, 72000);

            zombieBlockBreakingChance = builder
                    .comment(
                            "Chance that a scheduled block-breaking check actually tries to break a block.",
                            "0.0 = never break, 1.0 = every scheduled check tries.",
                            "Default 0.20 means 20% per scheduled check.")
                    .defineInRange("zombieBlockBreakingChance", 0.20, 0.0, 1.0);

            zombieBlockBreakingRange = builder
                    .comment(
                            "How far in front of the zombie it can look for a block to break.",
                            "1 = only the immediate wall/door in front of it.",
                            "2-4 lets it reach slightly farther but increases checks per attempt.")
                    .defineInRange("zombieBlockBreakingRange", 1, 1, 4);

            zombieBlockBreakingMaxHardness = builder
                    .comment(
                            "Maximum block hardness zombies are allowed to break.",
                            "Low values make them break soft blocks only. Higher values allow stronger building blocks.",
                            "Examples: dirt is about 0.5, glass is about 0.3, planks are about 2.0, wooden doors are about 3.0, obsidian is 50.",
                            "Unbreakable blocks are always protected.")
                    .defineInRange("zombieBlockBreakingMaxHardness", 3.0, 0.0, 50.0);

            zombieBlockBreakingDropBlocks = builder
                    .comment(
                            "Whether blocks broken by zombies should drop items.",
                            "false avoids item spam and farming exploits.",
                            "true makes broken blocks drop like normal block destruction.")
                    .define("zombieBlockBreakingDropBlocks", false);

            zombieBlockBreakingRequireTarget = builder
                    .comment(
                            "If true, zombies only break blocks while they have a valid target.",
                            "This stops random terrain griefing when zombies are wandering.")
                    .define("zombieBlockBreakingRequireTarget", true);

            zombieBlockBreakingRequireObstacle = builder
                    .comment(
                            "If true, zombies only try to break blocks when blocked or when their target is behind cover.",
                            "This helps focus breaking on walls, doors, and barriers instead of random nearby blocks.")
                    .define("zombieBlockBreakingRequireObstacle", true);

            zombieBlockBreakingRespectMobGriefing = builder
                    .comment(
                            "If true, the vanilla mobGriefing gamerule and loader mob-griefing events can stop zombie block breaking.",
                            "If false, this feature can work even when mobGriefing is false, but block protection and destroy events still apply.")
                    .define("zombieBlockBreakingRespectMobGriefing", true);

            zombieBlockBreakingAllowBlockEntities = builder
                    .comment(
                            "If true, zombies may break blocks with block entities, like chests, furnaces, and many modded machines.",
                            "Strong warning: leave this false unless you really want zombies to destroy storage or machinery.")
                    .define("zombieBlockBreakingAllowBlockEntities", false);

            zombieBlockBreakingAllowToolRequiredBlocks = builder
                    .comment(
                            "If true, zombies may break blocks that normally require the correct tool for drops.",
                            "false protects many stone, ore, metal, and stronger building blocks even if max hardness would allow them.")
                    .define("zombieBlockBreakingAllowToolRequiredBlocks", false);

            zombieBlockBreakingAllowLightBlocks = builder
                    .comment(
                            "If true, zombies may break light-emitting blocks like torches, lanterns, glowstone, and similar blocks.",
                            "false keeps light-based base protection useful when maxBlockLightForSpawning is configured.")
                    .define("zombieBlockBreakingAllowLightBlocks", false);
            builder.pop();

            builder.comment(sectionComment(
                    "ZOMBIE BLOCK PLACING",
                    "Optional siege behavior that lets zombie-class mobs place simple building blocks to reach targets.",
                    "Safe beginner setup: leave this disabled. It is separate from zombie block breaking.",
                    "Quick setup: /za placing on enables limited cobblestone steps and bridges immediately.",
                    "Placement honors mobGriefing and loader block-place events by default, so protection mods can cancel it.",
                    "Each zombie has a configurable placement limit to prevent unlimited terrain clutter.",
                    "Use start day 0 if you want it active immediately after enabling."))
                    .push("blockplacing");
            enableZombieBlockPlacing = builder
                    .comment(
                            "Main on/off switch for zombie block placing.",
                            "false = zombies never place blocks from this mod.",
                            "true = zombie-class mobs can place blocks after zombieBlockPlacingStartDay if the other rules allow it.")
                    .define("enableZombieBlockPlacing", false);

            zombieBlockPlacingStartDay = builder
                    .comment(
                            "Apocalypse day when block placing is allowed to start after the feature is enabled.",
                            "0 = active immediately on day 0/day 1 style worlds.",
                            "15 = zombies cannot place blocks until the day counter reaches 15.")
                    .defineInRange("zombieBlockPlacingStartDay", 15, 0, ConfigLimits.MAX_APOCALYPSE_DAY);

            zombieBlockPlacingInterval = builder
                    .comment(
                            "How often each zombie can attempt block placing.",
                            "20 ticks = 1 second.",
                            "Default 100 = each zombie checks at most once every 5 seconds.",
                            "Lower values feel more aggressive but cost more CPU on crowded servers.")
                    .defineInRange("zombieBlockPlacingInterval", 100, 20, 72000);

            zombieBlockPlacingChance = builder
                    .comment(
                            "Chance that a scheduled block-placing check actually tries to place a block.",
                            "0.0 = never place, 1.0 = every scheduled check tries.",
                            "Default 0.15 means 15% per scheduled check.")
                    .defineInRange("zombieBlockPlacingChance", 0.15, 0.0, 1.0);

            zombieBlockPlacingBlock = builder
                    .comment(
                            "Block zombies place, written as a namespaced block ID.",
                            "Examples: minecraft:cobblestone, minecraft:dirt, minecraft:oak_planks.",
                            "Air, falling blocks, block-entity blocks, unbreakable blocks, fluids, and non-solid blocks are rejected for safety.",
                            "Invalid IDs pause block placing and produce one clear server warning.")
                    .define("zombieBlockPlacingBlock", "minecraft:cobblestone");

            zombieBlockPlacingMaxPerZombie = builder
                    .comment(
                            "Maximum number of blocks one zombie may place during its lifetime.",
                            "0 = unlimited. Use unlimited only if terrain clutter is intentional.",
                            "Default 8 limits griefing even when many placement attempts succeed.")
                    .defineInRange("zombieBlockPlacingMaxPerZombie", 8, 0, 256);

            zombieBlockPlacingMaxTargetDistance = builder
                    .comment(
                            "Farthest target distance where a zombie may place blocks.",
                            "This stops distant targets from causing unnecessary building.",
                            "Default 32 blocks is close enough for active pursuit without broad terrain changes.")
                    .defineInRange("zombieBlockPlacingMaxTargetDistance", 32, 4, 128);

            zombieBlockPlacingRequireTarget = builder
                    .comment(
                            "If true, zombies only place blocks while pursuing a valid living target.",
                            "Creative and spectator players do not count as valid targets.",
                            "Leave this true to prevent wandering zombies from building randomly.")
                    .define("zombieBlockPlacingRequireTarget", true);

            zombieBlockPlacingRequireObstacle = builder
                    .comment(
                            "If true, zombies only place when blocked, when the target is above/behind cover, or when bridging a gap.",
                            "If false, zombies may place valid step blocks while pursuing a target even on open terrain.")
                    .define("zombieBlockPlacingRequireObstacle", true);

            zombieBlockPlacingRespectMobGriefing = builder
                    .comment(
                            "If true, the vanilla mobGriefing gamerule and loader mob-griefing events can stop block placing.",
                            "Block-place events are always fired so claim and protection mods can cancel placement.",
                            "Set false only if you intentionally want this feature to ignore the mobGriefing gamerule.")
                    .define("zombieBlockPlacingRespectMobGriefing", true);

            zombieBlockPlacingAllowBridges = builder
                    .comment(
                            "Allows zombies to fill a one-block-deep gap directly in front of them.",
                            "The new block must attach to solid ground behind it, so zombies cannot build floating bridges.")
                    .define("zombieBlockPlacingAllowBridges", true);

            zombieBlockPlacingAllowSteps = builder
                    .comment(
                            "Allows zombies to place a one-block step directly in front of them.",
                            "Steps require solid support below and enough empty space for entities.")
                    .define("zombieBlockPlacingAllowSteps", true);

            zombieBlockPlacingReplaceFluids = builder
                    .comment(
                            "Allows placement to replace water or other replaceable fluid blocks.",
                            "false protects water builds and prevents zombies from filling fluids.",
                            "This never replaces solid blocks.")
                    .define("zombieBlockPlacingReplaceFluids", false);

            zombieBlockPlacingReplaceReplaceableBlocks = builder
                    .comment(
                            "Allows placement to replace non-fluid replaceable blocks such as grass, flowers, or snow layers.",
                            "false means zombies only place into air, plus fluids when zombieBlockPlacingReplaceFluids is enabled.",
                            "Block entities and solid blocks are always protected.")
                    .define("zombieBlockPlacingReplaceReplaceableBlocks", false);
            builder.pop();

            builder.comment(sectionComment(
                    "ZOMBIE TOWERING",
                    "Optional World War Z-style swarm movement that lets zombie-class mobs form real moving stacks.",
                    "Safe beginner setup: leave this disabled. It never places or breaks blocks.",
                    "Towering requires a valid target and a nearby crowd, and is day-gated to prevent early-world pressure.",
                    "Normal ground combat and ordinary player jumps do not trigger a tower with the default obstacle rule.",
                    "Quick setup: /za towering on enables a balanced stack preset immediately.",
                    "Checks are staggered by entity ID so large hordes do not all scan on the same tick."))
                    .push("towering");
            enableZombieTowering = builder
                    .comment(
                            "Main on/off switch for zombie towering.",
                            "false = zombies never form passenger stacks from this mod.",
                            "true = recognized Zombie subclasses can form moving stacks after zombieToweringStartDay.")
                    .define("enableZombieTowering", false);

            zombieToweringStartDay = builder
                    .comment(
                            "Apocalypse day when towering is allowed to start after the feature is enabled.",
                            "0 = active immediately on day 0/day 1 style worlds.",
                            "20 = zombies cannot tower until the day counter reaches 20.")
                    .defineInRange("zombieToweringStartDay", 20, 0, ConfigLimits.MAX_APOCALYPSE_DAY);

            zombieToweringInterval = builder
                    .comment(
                            "How often each zombie can check for a towering opportunity.",
                            "20 ticks = 1 second. Checks are spread across ticks using the entity ID.",
                            "Lower values react faster but increase nearby-entity scans in crowded areas.")
                    .defineInRange("zombieToweringInterval", 20, 1, 72000);

            zombieToweringChance = builder
                    .comment(
                            "Chance that a scheduled towering check continues to the nearby-crowd scan.",
                            "0.0 = never tower, 1.0 = every eligible scheduled check tries.",
                            "The chance roll happens before the entity scan to keep failed attempts cheap.")
                    .defineInRange("zombieToweringChance", 0.45, 0.0, 1.0);

            zombieToweringMaxTargetDistance = builder
                    .comment(
                            "Farthest target distance where towering is allowed.",
                            "This prevents a distant or unloaded fight from making nearby zombies pile up.")
                    .defineInRange("zombieToweringMaxTargetDistance", 32, 4, 128);

            zombieToweringMinNearbyZombies = builder
                    .comment(
                            "Minimum number of other nearby Zombie subclasses required before a stack can form.",
                            "Default 2 means towering needs a crowd of at least three including the climbing zombie.")
                    .defineInRange("zombieToweringMinNearbyZombies", 2, 1, 16);

            zombieToweringCrowdRadius = builder
                    .comment(
                            "Horizontal radius used to find nearby zombie-class mobs.",
                            "Keep this small because every eligible attempt performs one bounded entity lookup.")
                    .defineInRange("zombieToweringCrowdRadius", 2.25, 0.75, 6.0);

            zombieToweringMaxStackSize = builder
                    .comment(
                            "Maximum number of zombies allowed in one vertical stack, including the bottom zombie.",
                            "Default 0 means no configured zombie-count limit; dynamic target height stops normal towers.",
                            "1 prevents a stack from forming. Values 2 and higher set an exact per-tower cap.",
                            "Unlimited mode still needs enough zombies, a useful raised target, and collision-free rider space.")
                    .defineInRange("zombieToweringMaxStackSize", 0, 0, ConfigLimits.MAX_TOWER_STACK_SIZE);

            zombieToweringMaxTowersPerPlayer = builder
                    .comment(
                            "Maximum number of separate zombie towers that may target one player at the same time.",
                            "Default 3 allows pressure from several directions without turning every crowd into a tower.",
                            "0 = unlimited. Lowering this live releases extra loaded towers cleanly.")
                    .defineInRange("zombieToweringMaxTowersPerPlayer", 3, 0, ConfigLimits.MAX_TOWERS_PER_PLAYER);

            zombieToweringDynamicHeightEnabled = builder
                    .comment(
                            "Smart height limit for normal zombie towers.",
                            "true = grow only through the target's block Y level plus zombieToweringTargetHeightOffset.",
                            "false = use zombieToweringMaxHeightAboveTarget instead.",
                            "Default true keeps stacks useful without letting them grow above a grounded player.")
                    .define("zombieToweringDynamicHeightEnabled", true);

            zombieToweringTargetHeightOffset = builder
                    .comment(
                            "Extra block levels allowed above the target while dynamic height is enabled.",
                            "Default 1 means the top zombie may occupy the block level at player Y + 1.",
                            "This uses block Y levels, so normal passenger spacing does not stop one level too early.")
                    .defineInRange("zombieToweringTargetHeightOffset", 1, 0,
                            ConfigLimits.MAX_TOWER_HEIGHT_OFFSET);

            zombieToweringSmartDismountEnabled = builder
                    .comment(
                            "Let towers return to normal zombies when stacking is no longer useful.",
                            "When a target stays on reachable ground, riders move one at a time to nearby safe floor positions.",
                            "The short stability delay prevents a single jump or pathfinding hiccup from collapsing the tower.",
                            "Blocked or unsafe dismount positions are rejected instead of putting zombies inside the floor.",
                            "Disable this only for intentionally permanent or unlimited towers.")
                    .define("zombieToweringSmartDismountEnabled", true);

            zombieToweringJumpingEnabled = builder
                    .comment(
                            "Controls whether the top zombie can jump off a tall-enough tower toward its target.",
                            "Default false keeps normal towers stable and lets smart dismounting handle grounded targets.",
                            "When enabled, inherited hit/knockback velocity is discarded so riders cannot be launched.")
                    .define("zombieToweringJumpingEnabled", false);

            zombieToweringJumpCooldownTicks = builder
                    .comment(
                            "Minimum delay between changes to the same tower: growth, safe dismounts, and jump attacks.",
                            "Default 10 ticks makes stacks build and separate gradually instead of snapping together.",
                            "20 ticks = 1 second. Lower values react faster; higher values look calmer and cost less CPU.",
                            "The config key keeps its old jumpCooldown name so existing server configs remain compatible.")
                    .defineInRange("zombieToweringJumpCooldownTicks", 10, 1, 1200);

            zombieToweringDismountDistance = builder
                    .comment(
                            "Horizontal distance from the target where a high-enough stacked zombie jumps off to attack.",
                            "Default 2.75 blocks lets the upper zombie clear a wall edge without launching from far away.")
                    .defineInRange("zombieToweringDismountDistance", 2.75, 1.0, 8.0);

            zombieToweringVerticalBoost = builder
                    .comment(
                            "Upward velocity used when the upper zombie jumps off its stack toward the target.",
                            "Default 0.48 clears a normal block edge without acting like flight.")
                    .defineInRange("zombieToweringVerticalBoost", 0.48, 0.1, 1.0);

            zombieToweringForwardBoost = builder
                    .comment(
                            "Horizontal velocity toward the target when a stacked zombie jumps off.",
                            "The result is capped so repeated attempts cannot create runaway horizontal speed.")
                    .defineInRange("zombieToweringForwardBoost", 0.18, 0.0, 0.6);

            zombieToweringMaxHeightAboveTarget = builder
                    .comment(
                            "Fallback height above the target when dynamic height is disabled.",
                            "0 = no configured height cap. Values 1 and higher set the maximum blocks above the target.",
                            "Use 0 with max stack size 0 only when intentionally allowing unrestricted towers.")
                    .defineInRange("zombieToweringMaxHeightAboveTarget", 8, 0,
                            ConfigLimits.MAX_TOWER_HEIGHT_LIMIT);

            zombieToweringRequireObstacle = builder
                    .comment(
                            "If true, towering only starts for a meaningfully raised target or a genuinely blocked route.",
                            "Ordinary ground combat, crowd collisions, and normal player jumps are ignored.",
                            "Leave this true for smooth gameplay. false allows towers to form anywhere, including flat ground.")
                    .define("zombieToweringRequireObstacle", true);
            builder.pop();

            builder.comment(sectionComment(
                    "HORDE EVENTS",
                    "Scheduled high-pressure events that temporarily increase custom spawning.",
                    "Safe beginner setup: keep interval and duration at defaults until you know your server can handle more.",
                    "Quick setup: /za events on enables a balanced scheduled-horde preset.",
                    "Performance warning: hordeZombiesPerSpawn, hordeSpawnMultiplier, and eventSpawnInterval are the big lag knobs.",
                    "Hardcore setup: increase one setting at a time, not all at once."))
                    .push("horde");
            enableHordeEvents = builder
                    .comment(
                            "Enables scheduled horde days that can trigger large pressure spikes.",
                            "Admins can still use horde commands even if this automatic system is disabled.",
                            "Hordes require the main custom spawning switch because they strengthen custom waves.")
                    .define("enableHordeEvents", true);

            hordeIntervalDays = builder
                    .comment(
                            "How often the mod reaches a scheduled horde day.",
                            "Example: 5 means every 5th day is checked for a horde.")
                    .defineInRange("hordeIntervalDays", 5, 1, ConfigLimits.MAX_APOCALYPSE_DAY);

            hordeStartChance = builder
                    .comment(
                            "Chance that a scheduled horde day actually starts a horde at dawn.",
                            "0.0 = scheduled days never trigger, 1.0 = every scheduled day triggers.")
                    .defineInRange("hordeStartChance", 0.5, 0.0, 1.0);

            hordeDurationMinutes = builder
                    .comment(
                            "How long a horde lasts in real-world minutes.",
                            "Longer hordes keep the stronger event settings active for more time.")
                    .defineInRange("hordeDurationMinutes", 5, 1, 10080);

            hordeSpawnMultiplier = builder
                    .comment(
                            "Extra spawn chance multiplier used while a horde is active.",
                            "1.0 = no extra boost, 3.0 = three times the normal chance.",
                            "High values can overwhelm players and servers very fast.")
                    .defineInRange("hordeSpawnMultiplier", 3.0, 1.0, 20.0);

            hordeZombiesPerSpawn = builder
                    .comment(
                            "How many mobs each custom wave tries to spawn during a horde.",
                            "This overrides the normal wave size while the event is active.",
                            "Do not raise this too high unless the server is strong and the pack is meant to be brutal.")
                    .defineInRange("hordeZombiesPerSpawn", 5, 1, 100);

            eventSpawnInterval = builder
                    .comment(
                            "Spawn check interval used during hordes and blood moons.",
                            "Lower values mean more frequent checks. 20 ticks = 1 second.",
                            "Performance warning: this runs for every eligible player during events.")
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

            builder.comment(sectionComment(
                    "BLOOD MOON EVENTS",
                    "Random night events that raise zombie pressure until dawn.",
                    "Blood moon settings stack with other spawn systems, so small changes can hit hard.",
                    "Safe beginner setup: keep chance low and avoid huge multipliers.",
                    "Quick setup: /za bloodmoon on enables a balanced random-event preset."))
                    .push("bloodmoon");
            enableBloodMoon = builder
                    .comment(
                            "Enables random blood moon nights.",
                            "Admins can still force a blood moon with the command even if this is off.",
                            "Blood moons require the main custom spawning switch because they strengthen custom waves.")
                    .define("enableBloodMoon", true);

            bloodMoonChance = builder
                    .comment(
                            "Chance each night becomes a blood moon.",
                            "0.0 = never, 1.0 = every night.")
                    .defineInRange("bloodMoonChance", 0.15, 0.0, 1.0);

            bloodMoonSpawnMultiplier = builder
                    .comment(
                            "Extra spawn chance multiplier used during a blood moon.",
                            "1.0 = no extra boost, 5.0 = five times the normal chance.",
                            "High values can turn one night into a server-wide stress test.")
                    .defineInRange("bloodMoonSpawnMultiplier", 5.0, 1.0, 50.0);

            bloodMoonZombiesPerSpawn = builder
                    .comment(
                            "How many mobs each custom wave tries to spawn during a blood moon.",
                            "This overrides the normal wave size while the blood moon is active.",
                            "Raise slowly if you want harder nights without runaway mob counts.")
                    .defineInRange("bloodMoonZombiesPerSpawn", 4, 1, 100);
            builder.pop();

            builder.comment(sectionComment(
                    "DAY-BASED DIFFICULTY SCALING",
                    "Makes the apocalypse get harder as the world day counter rises.",
                    "Safe beginner setup: keep scalingStartDay above 0 so new worlds get a short grace period.",
                    "Quick setup: /za scaling on loads balanced day 3-50 progression.",
                    "Hardcore setup: lower scalingStartDay or maxScalingDay, but test before combining with strong hordes."))
                    .push("scaling");
            enableDifficultyScaling = builder
                    .comment(
                            "Enables day-based scaling so zombies get tougher as the world gets older.",
                            "This affects the legacy stat and gear progression system.")
                    .define("enableDifficultyScaling", true);

            scalingStartDay = builder
                    .comment(
                            "The first day where scaling starts to increase.",
                            "Before this day, the scaling factor stays at 0%.")
                    .defineInRange("scalingStartDay", 3, 0, ConfigLimits.MAX_APOCALYPSE_DAY);

            maxScalingDay = builder
                    .comment(
                            "The day where scaling reaches full strength.",
                            "After this day, the legacy scaling factor stays at 100%.")
                    .defineInRange("maxScalingDay", 50, 1, ConfigLimits.MAX_APOCALYPSE_DAY);

            maxSpeedBoost = builder
                    .comment(
                            "Maximum extra movement speed from the legacy scaling system at 100% scaling.",
                            "0.2 means up to 20% more movement speed.",
                            "Be careful: speed changes are very noticeable in combat.")
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

            builder.comment(sectionComment(
                    "ADVANCED ATTRIBUTE SYSTEM",
                    "Deep stat tuning for zombie-class mobs when they enter the world.",
                    "Beginner advice: leave this on with default numbers unless you are building a custom difficulty profile.",
                    "Quick setup: /za attributes on loads conservative scaling values and required dependencies.",
                    "Hardcore setup: use small multiplier changes first. 1.20 is already 20% stronger.",
                    "Warning: extreme health, speed, follow range, or knockback values can feel broken or hurt performance."))
                    .push("attributes");
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

            builder.comment(sectionComment(
                    "BASE ATTRIBUTE PROFILE",
                    "Global stat layer applied to all zombie-class mobs.",
                    "This is the easiest place to make every zombie a little stronger or weaker.",
                    "Safe beginner setup: keep multipliers at 1.0 and bonuses at 0.0."))
                    .push("base");
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

            builder.comment(sectionComment(
                    "ATTRIBUTE SCALING PROFILE",
                    "Extra stat growth added over time when scaleAttributesWithDifficulty is enabled.",
                    "These values are multiplied by the day-based difficulty factor.",
                    "Beginner advice: start with health or attack only. Avoid stacking speed boosts everywhere."))
                    .push("scaling");
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

            builder.comment(sectionComment(
                    "VARIANT ATTRIBUTE PROFILES",
                    "Per-mob stat layers for zombies, husks, drowned, and zombie villagers.",
                    "Use this when you want one variant to feel different from the others."))
                    .push("variants");

            builder.comment(subsectionComment(
                    "Normal zombie stat overrides.",
                    "Good beginner use: keep normal zombies close to vanilla and make special variants scarier."))
                    .push("zombie");
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

            builder.comment(subsectionComment(
                    "Husk stat overrides.",
                    "Good hardcore use: make husks hit harder in deserts without changing all zombies."))
                    .push("husk");
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

            builder.comment(subsectionComment(
                    "Drowned stat overrides.",
                    "Good hardcore use: make drowned tankier or faster in water-heavy worlds."))
                    .push("drowned");
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

            builder.comment(subsectionComment(
                    "Zombie villager stat overrides.",
                    "Good beginner use: keep these near normal zombies unless you want them to be rare threats."))
                    .push("zombieVillager");
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

            builder.comment(sectionComment(
                    "BIOME AND DIMENSION ATTRIBUTE CONTEXTS",
                    "Context multipliers apply based on where the mob enters the world.",
                    "Multiple contexts can stack, so avoid huge values here.",
                    "Beginner advice: use small changes like 1.10 or 0.90 first."))
                    .push("contexts");

            builder.comment(subsectionComment(
                    "Desert and badlands context multipliers.",
                    "Useful for dry-biome husk pressure."))
                    .push("desert");
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

            builder.comment(subsectionComment(
                    "Ocean, river, swamp, and mangrove swamp context multipliers.",
                    "Useful for making drowned or wet-biome zombies feel different."))
                    .push("water");
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

            builder.comment(subsectionComment(
                    "Mushroom fields context multipliers.",
                    "If mushroomSafeZone is true, custom spawns are blocked there, but natural zombies can still use these stats."))
                    .push("mushroom");
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

            builder.comment(subsectionComment(
                    "Nether context multipliers.",
                    "Only affects zombie-class mobs in the Nether. Keep values modest because Nether combat is already risky."))
                    .push("nether");
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

            builder.comment(subsectionComment(
                    "End context multipliers.",
                    "Only affects zombie-class mobs in the End. Useful for custom packs that add zombie pressure there."))
                    .push("end");
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

            builder.comment(sectionComment(
                    "NIGHT SPAWN BOOST",
                    "Raises custom spawn chance at night. This does not directly change zombie speed or health.",
                    "Safe beginner setup: 1.5 keeps nights scarier without making every check guaranteed."))
                    .push("nightspawning");
            enableNightBoost = builder
                    .comment(
                            "Adds an extra spawn chance boost at night.",
                            "This stacks on top of normal spawn settings in dimensions with a real day/night cycle.",
                            "Nether, End, and other fixed-time dimensions do not receive a permanent night boost.")
                    .define("enableNightBoost", true);

            nightSpawnMultiplier = builder
                    .comment(
                            "Extra spawn chance multiplier used at night.",
                            "1.0 = no extra boost, 1.5 = 50% more chance at night.")
                    .defineInRange("nightSpawnMultiplier", 1.5, 1.0, 10.0);
            builder.pop();

            builder.comment(sectionComment(
                    "BIOME SPAWN RULES",
                    "Controls biome-based variant chances and dimension custom spawning.",
                    "Safe beginner setup: leave Nether and End spawning off unless the pack is designed around it.",
                    "Performance warning: enabling extra dimensions means more places can run custom spawn checks."))
                    .push("biomes");
            enableBiomeModifiers = builder
                    .comment(
                            "Lets biome type influence which zombie variants spawn more often.",
                            "Example: more husks in deserts and more drowned in wet biomes.",
                            "This does not disable mushroomSafeZone; the safe-zone switch remains independent.")
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
                            "This lets mushroom biomes act like a safer refuge.",
                            "It works even when enableBiomeModifiers is false.")
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

            builder.comment(sectionComment(
                    "DEATH COOLDOWN",
                    "A mercy system that reduces custom spawn pressure after a player dies.",
                    "Recommended for public servers so players do not get spawn-camped by fresh waves."))
                    .push("deathcooldown");
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

            builder.comment(sectionComment(
                    "SPAWN FEEDBACK EFFECTS",
                    "Cosmetic sound and particle feedback when custom waves spawn.",
                    "Safe to disable if players find it noisy or if you want slightly less client-side clutter."))
                    .push("effects");
            enableSpawnEffects = builder
                    .comment(
                            "Enables extra sound and particle feedback when custom spawns happen.",
                            "One effect is produced per successful wave instead of once per zombie.",
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

            builder.comment(sectionComment(
                    "STATISTICS",
                    "Controls kill totals shown by /zstats.",
                    "Milestone advancements use their own progress so achievements can still work if this is disabled.",
                    "Quick control: /za stats on or /za stats off."))
                    .push("statistics");
            enableStatistics = builder
                    .comment(
                            "Tracks zombie kill totals shown by /zstats.",
                            "Milestone advancements keep their own progress so achievements can still work if this is off.")
                    .define("enableStatistics", true);
            builder.pop();

            builder.comment(sectionComment(
                    "EXTRA DROPS",
                    "Bonus loot chances for zombie-class mobs.",
                    "Safe beginner setup: keep rare items low so zombies do not replace normal farming.",
                    "0.10 means 10% chance per killed zombie-class mob.",
                    "The vanilla doMobLoot gamerule still controls whether any bonus loot can drop."))
                    .push("drops");
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

        private static String[] sectionComment(String title, String... details) {
            String[] lines = new String[details.length + 5];
            lines[0] = " ";
            lines[1] = "============================================================";
            lines[2] = title;
            lines[3] = "============================================================";
            lines[4] = " ";
            System.arraycopy(details, 0, lines, 5, details.length);
            return lines;
        }

        private static String[] subsectionComment(String title, String... details) {
            String[] lines = new String[details.length + 3];
            lines[0] = " ";
            lines[1] = "-------------------- " + title + " --------------------";
            lines[2] = " ";
            System.arraycopy(details, 0, lines, 3, details.length);
            return lines;
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
