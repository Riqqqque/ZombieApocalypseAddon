package com.rique.zombieapocalypse.commands;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.SpawnMath;
import com.rique.zombieapocalypse.ZombieBlockPlacer;

/**
 * What the feature "on" commands change.
 *
 * Turning a feature on flips its switch, plus any switch it depends on, and repairs only values
 * that would keep it from doing anything at all, such as a 0% chance. Everything else an admin
 * tuned, in game or in the config file, stays as it is. Each method returns a plain-language note
 * for every extra change so the command can report it. /za config reset restores whole sections.
 */
final class FeaturePresets {

    private FeaturePresets() {
    }

    static List<String> enableSpawning() {
        List<String> notes = new ArrayList<>();
        Config.edit(() -> {
            Config.set(Config.COMMON.enableDaySpawning, true);
            repairSpawning(notes);
        });
        return notes;
    }

    static List<String> enableHordes() {
        Config.Common config = Config.COMMON;
        List<String> notes = new ArrayList<>();
        Config.edit(() -> {
            Config.set(config.enableHordeEvents, true);
            requireCustomWaves(notes, "Hordes");
            if (config.hordeStartChance.get() <= 0.0) {
                Config.reset(config.hordeStartChance);
                notes.add(repaired("Horde start chance", "0%", CommandUtil.percent(config.hordeStartChance.getDefault()),
                        "so scheduled hordes could never start"));
            }
        });
        if (!config.enableDaytimeSpawning.get() && !config.hordeStartsAtDusk.get()) {
            notes.add("Night-only mode is on, so scheduled dawn hordes stay paused. /za events dusk on makes them arrive at nightfall instead.");
        }
        return notes;
    }

    static List<String> enableBloodMoons() {
        Config.Common config = Config.COMMON;
        List<String> notes = new ArrayList<>();
        Config.edit(() -> {
            Config.set(config.enableBloodMoon, true);
            requireCustomWaves(notes, "Blood moons");
            if (config.bloodMoonChance.get() <= 0.0) {
                Config.reset(config.bloodMoonChance);
                notes.add(repaired("Blood moon chance", "0%", CommandUtil.percent(config.bloodMoonChance.getDefault()),
                        "so blood moons could never happen"));
            }
        });
        return notes;
    }

    static List<String> enableVariants() {
        Config.Common config = Config.COMMON;
        List<String> notes = new ArrayList<>();
        Config.edit(() -> {
            Config.set(config.enableZombieVariants, true);
            if (config.huskChance.get() <= 0.0
                    && config.drownedChance.get() <= 0.0
                    && config.zombieVillagerChance.get() <= 0.0) {
                Config.reset(config.huskChance);
                Config.reset(config.drownedChance);
                Config.reset(config.zombieVillagerChance);
                notes.add("Husk, drowned, and zombie villager chances were all 0%, so variants could never spawn."
                        + " They are back to their defaults.");
            }
        });
        return notes;
    }

    static List<String> enableNightBoost() {
        Config.Common config = Config.COMMON;
        List<String> notes = new ArrayList<>();
        Config.edit(() -> {
            Config.set(config.enableNightBoost, true);
            if (config.nightSpawnMultiplier.get() <= 1.0) {
                Config.reset(config.nightSpawnMultiplier);
                notes.add(repaired("Night spawn multiplier", "1.00x", CommandUtil.multiplier(config.nightSpawnMultiplier.getDefault()),
                        "which adds nothing"));
            }
        });
        return notes;
    }

    static List<String> enableScaling() {
        Config.Common config = Config.COMMON;
        List<String> notes = new ArrayList<>();
        Config.edit(() -> {
            Config.set(config.enableDifficultyScaling, true);
            if (config.maxSpeedBoost.get() <= 0.0
                    && config.maxHealthBoost.get() <= 0
                    && config.maxArmorChance.get() <= 0.0
                    && config.maxWeaponChance.get() <= 0.0) {
                Config.reset(config.maxSpeedBoost);
                Config.reset(config.maxHealthBoost);
                Config.reset(config.maxArmorChance);
                Config.reset(config.maxWeaponChance);
                notes.add("Speed, health, armor, and weapon bonuses were all 0, so scaling could not change anything."
                        + " They are back to their defaults.");
            }
        });
        return notes;
    }

    static List<String> enableAttributes() {
        List<String> notes = new ArrayList<>();
        Config.set(Config.COMMON.enableAttributeModifiers, true);
        if (Config.COMMON.scaleAttributesWithDifficulty.get() && !Config.COMMON.enableDifficultyScaling.get()) {
            notes.add("Day scaling is off, so attribute scaling stays at day-0 strength. /za scaling on turns it on.");
        }
        return notes;
    }

    /** Turns on one advanced-attribute option, plus the attribute system it belongs to. */
    static List<String> enableAttributeOption(Runnable setOption) {
        List<String> notes = new ArrayList<>();
        Config.edit(() -> {
            setOption.run();
            if (!Config.COMMON.enableAttributeModifiers.get()) {
                Config.set(Config.COMMON.enableAttributeModifiers, true);
                notes.add("Advanced attributes were off, and this option needs them, so they are on now.");
            }
        });
        return notes;
    }

    static List<String> enableCompatibility() {
        Config.set(Config.COMMON.enableModdedZombieCompatibility, true);
        return List.of();
    }

    static List<String> enableBiomeModifiers() {
        Config.Common config = Config.COMMON;
        List<String> notes = new ArrayList<>();
        Config.edit(() -> {
            Config.set(config.enableBiomeModifiers, true);
            if (config.desertHuskBonus.get() <= 0.0
                    && config.waterDrownedBonus.get() <= 0.0
                    && !config.mushroomSafeZone.get()) {
                Config.reset(config.desertHuskBonus);
                Config.reset(config.waterDrownedBonus);
                Config.reset(config.mushroomSafeZone);
                notes.add("Every biome modifier was off or 0%, so biomes could not change anything. They are back to their defaults.");
            }
        });
        return notes;
    }

    static List<String> enableDeathCooldown() {
        Config.Common config = Config.COMMON;
        List<String> notes = new ArrayList<>();
        Config.edit(() -> {
            Config.set(config.enableDeathCooldown, true);
            if (config.cooldownSpawnReduction.get() <= 0.0) {
                Config.reset(config.cooldownSpawnReduction);
                notes.add(repaired("Cooldown spawn reduction", "0%", CommandUtil.percent(config.cooldownSpawnReduction.getDefault()),
                        "so the cooldown changed nothing"));
            }
        });
        return notes;
    }

    static List<String> enableSpawnEffects() {
        Config.Common config = Config.COMMON;
        List<String> notes = new ArrayList<>();
        Config.edit(() -> {
            Config.set(config.enableSpawnEffects, true);
            if (!config.spawnSound.get() && !config.spawnParticles.get()) {
                Config.reset(config.spawnSound);
                Config.reset(config.spawnParticles);
                notes.add("Spawn sound and particles were both off, so effects could not show. Both are on again.");
            }
        });
        return notes;
    }

    static List<String> enableBlockBreaking(boolean startNow) {
        Config.Common config = Config.COMMON;
        List<String> notes = new ArrayList<>();
        Config.edit(() -> {
            Config.set(config.enableZombieBlockBreaking, true);
            if (startNow) {
                Config.set(config.zombieBlockBreakingStartDay, 0);
            }
            if (config.zombieBlockBreakingChance.get() <= 0.0) {
                Config.reset(config.zombieBlockBreakingChance);
                notes.add(repaired("Block breaking chance", "0%", CommandUtil.percent(config.zombieBlockBreakingChance.getDefault()),
                        "so zombies could never break anything"));
            }
            if (config.zombieBlockBreakingMaxHardness.get() <= 0.0) {
                Config.reset(config.zombieBlockBreakingMaxHardness);
                notes.add(repaired("Max hardness", "0", CommandUtil.number(config.zombieBlockBreakingMaxHardness.getDefault()),
                        "so no real block could be broken"));
            }
        });
        return notes;
    }

    static List<String> enableBlockPlacing(ServerLevel level, BlockPos validationPos, boolean startNow) {
        Config.Common config = Config.COMMON;
        List<String> notes = new ArrayList<>();
        Config.edit(() -> {
            Config.set(config.enableZombieBlockPlacing, true);
            if (startNow) {
                Config.set(config.zombieBlockPlacingStartDay, 0);
            }
            if (config.zombieBlockPlacingChance.get() <= 0.0) {
                Config.reset(config.zombieBlockPlacingChance);
                notes.add(repaired("Block placing chance", "0%", CommandUtil.percent(config.zombieBlockPlacingChance.getDefault()),
                        "so zombies could never place anything"));
            }
            if (!config.zombieBlockPlacingAllowBridges.get() && !config.zombieBlockPlacingAllowSteps.get()) {
                Config.reset(config.zombieBlockPlacingAllowBridges);
                Config.reset(config.zombieBlockPlacingAllowSteps);
                notes.add("Bridges and steps were both off, so zombies had nothing to build. Both are on again.");
            }
            String block = config.zombieBlockPlacingBlock.get();
            if (level != null && ZombieBlockPlacer.validatePlacementBlock(level, validationPos, block) != null) {
                Config.reset(config.zombieBlockPlacingBlock);
                notes.add("The placing block " + block + " is missing or unsafe, so placing was paused. It is "
                        + config.zombieBlockPlacingBlock.getDefault() + " again.");
            }
        });
        return notes;
    }

    static List<String> enableTowering(boolean startNow) {
        Config.Common config = Config.COMMON;
        List<String> notes = new ArrayList<>();
        Config.edit(() -> {
            Config.set(config.enableZombieTowering, true);
            if (startNow) {
                Config.set(config.zombieToweringStartDay, 0);
            }
            repairTowering(notes);
        });
        return notes;
    }

    /** Unlimited mode: towering on with no count or height caps and no automatic collapse. */
    static List<String> enableUnlimitedTowering() {
        Config.Common config = Config.COMMON;
        List<String> notes = new ArrayList<>();
        Config.edit(() -> {
            Config.set(config.enableZombieTowering, true);
            Config.set(config.zombieToweringMaxStackSize, 0);
            Config.set(config.zombieToweringMaxHeightAboveTarget, 0);
            Config.set(config.zombieToweringDynamicHeightEnabled, false);
            Config.set(config.zombieToweringSmartDismountEnabled, false);
            Config.set(config.zombieToweringJumpingEnabled, false);
            repairTowering(notes);
        });
        return notes;
    }

    private static void repairTowering(List<String> notes) {
        Config.Common config = Config.COMMON;
        if (config.zombieToweringChance.get() <= 0.0) {
            Config.reset(config.zombieToweringChance);
            notes.add(repaired("Towering chance", "0%", CommandUtil.percent(config.zombieToweringChance.getDefault()),
                    "so towers could never form"));
        }
    }

    /** Hordes and blood moons strengthen custom waves, so they need the main spawning switch. */
    private static void requireCustomWaves(List<String> notes, String feature) {
        if (!Config.COMMON.enableDaySpawning.get()) {
            Config.set(Config.COMMON.enableDaySpawning, true);
            notes.add("Custom zombie waves were off. " + feature + " need them, so they are on again.");
        }
        repairSpawning(notes);
    }

    private static void repairSpawning(List<String> notes) {
        Config.Common config = Config.COMMON;
        if (config.daySpawnChance.get() <= 0.0) {
            Config.reset(config.daySpawnChance);
            notes.add(repaired("Spawn chance", "0%", CommandUtil.percent(config.daySpawnChance.getDefault()),
                    "so no waves could spawn"));
        }
        if (SpawnMath.isSpawnDistanceImpossible(config.minSpawnDistance.get(), config.spawnRange.get())) {
            int before = config.minSpawnDistance.get();
            Config.reset(config.minSpawnDistance);
            notes.add(repaired("Minimum spawn distance", before + " blocks",
                    config.minSpawnDistance.getDefault() + " blocks",
                    "which is farther than spawn range " + config.spawnRange.get() + " allows, so spawning was paused"));
        }
    }

    private static String repaired(String setting, String from, String to, String reason) {
        return setting + " was " + from + ", " + reason + ". It is now " + to + '.';
    }
}
