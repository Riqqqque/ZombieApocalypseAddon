package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.SpawnMath;

public final class DaySpawnCommands {

    private DaySpawnCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zdayspawn")
                .executes(context -> showStatus(context.getSource(), false))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource(), false))
                        .then(Commands.literal("all")
                                .executes(context -> showStatus(context.getSource(), true))))
                .then(CommandUtil.admin(Commands.literal("on")
                        .executes(context -> setSpawning(context.getSource(), true))))
                .then(CommandUtil.admin(Commands.literal("off")
                        .executes(context -> setSpawning(context.getSource(), false))))
                .then(CommandUtil.toggleSetting(
                        "enabled",
                        Config.COMMON.enableDaySpawning::get,
                        value -> Config.set(Config.COMMON.enableDaySpawning, value),
                        "Custom zombie waves"))
                .then(Commands.literal("daytime")
                        .executes(context -> {
                            CommandUtil.feedback(context.getSource(), "Daytime custom waves: " + formatDaytimeSpawning(), false);
                            return 1;
                        })
                        .then(CommandUtil.admin(CommandUtil.toggleArgument("state")
                                .executes(context -> setDaytimeSpawning(
                                        context.getSource(),
                                        CommandUtil.getToggle(context, "state"))))))
                .then(CommandUtil.doubleSetting("chance", "chance", 0.0, 1.0,
                        Config.COMMON.daySpawnChance::get,
                        value -> Config.set(Config.COMMON.daySpawnChance, value),
                        value -> "Spawn chance: " + CommandUtil.percent(value),
                        0.0, 0.1, 0.25, 0.5, 0.75, 1.0))
                .then(CommandUtil.doubleSetting("babychance", "chance", 0.0, 1.0,
                        Config.COMMON.babyZombieChance::get,
                        value -> Config.set(Config.COMMON.babyZombieChance, value),
                        value -> value <= 0.0
                                ? "Baby zombie spawns disabled."
                                : "Baby zombie chance: " + CommandUtil.percent(value),
                        0.0, 0.02, 0.05, 0.1))
                .then(CommandUtil.intSetting("interval", "ticks", 1, 72000,
                        Config.COMMON.daySpawnInterval::get,
                        value -> Config.set(Config.COMMON.daySpawnInterval, value),
                        value -> "Spawn interval: " + CommandUtil.ticks(value),
                        20, 60, 120, 200, 400, 1200))
                .then(CommandUtil.intSetting("eventinterval", "ticks", 1, 200,
                        Config.COMMON.eventSpawnInterval::get,
                        value -> Config.set(Config.COMMON.eventSpawnInterval, value),
                        value -> "Event spawn interval: " + CommandUtil.ticks(value),
                        5, 10, 20, 40, 100, 200))
                .then(CommandUtil.intSetting("amount", "zombies", 1, 50,
                        Config.COMMON.zombiesPerSpawn::get,
                        value -> Config.set(Config.COMMON.zombiesPerSpawn, value),
                        value -> "Zombies per wave: " + value,
                        1, 2, 4, 8, 16, 32, 50))
                .then(CommandUtil.intSetting("attempts", "attempts", 1, 40,
                        Config.COMMON.spawnAttemptsPerZombie::get,
                        value -> Config.set(Config.COMMON.spawnAttemptsPerZombie, value),
                        value -> "Spawn position attempts per zombie: " + value,
                        5, 10, 20, 40))
                .then(CommandUtil.intSetting("max", "zombies", 1, 500,
                        Config.COMMON.maxDayZombiesPerPlayer::get,
                        value -> Config.set(Config.COMMON.maxDayZombiesPerPlayer, value),
                        value -> "Max nearby zombies per player: " + value,
                        25, 50, 100, 200, 500))
                .then(CommandUtil.intSetting("range", "blocks", 16, 128,
                        Config.COMMON.spawnRange::get,
                        value -> Config.set(Config.COMMON.spawnRange, value),
                        value -> "Spawn range: " + value + " blocks" + buildSpawnDistanceWarning(),
                        16, 24, 32, 48, 64, 96, 128))
                .then(CommandUtil.intSetting("mindist", "blocks", 8, 64,
                        Config.COMMON.minSpawnDistance::get,
                        value -> Config.set(Config.COMMON.minSpawnDistance, value),
                        value -> "Minimum spawn distance: " + value + " blocks" + buildSpawnDistanceWarning(),
                        8, 12, 16, 24, 32, 48, 64))
                .then(CommandUtil.intSetting("daylightstart", "day", 0, 3650,
                        Config.COMMON.daylightSpawnStartDay::get,
                        value -> Config.set(Config.COMMON.daylightSpawnStartDay, value),
                        DaySpawnCommands::formatDaylightStart,
                        0, 1, 5, 10, 15, 30, 50, 100))
                .then(CommandUtil.intSetting("maxlight", "level", -1, 15,
                        Config.COMMON.maxBlockLightForSpawning::get,
                        value -> Config.set(Config.COMMON.maxBlockLightForSpawning, value),
                        value -> value < 0
                                ? "Custom spawning ignores block light."
                                : "Custom spawning max block light: " + value,
                        -1, 0, 7, 15))
                .then(CommandUtil.toggleSetting("sky", Config.COMMON.requireOpenSkyForOverworldSpawns::get,
                        value -> Config.set(Config.COMMON.requireOpenSkyForOverworldSpawns, value), "Require open sky in overworld"))
                .then(CommandUtil.toggleSetting("variants", Config.COMMON.enableZombieVariants::get,
                        value -> Config.set(Config.COMMON.enableZombieVariants, value), "Zombie variants"))
                .then(CommandUtil.toggleSetting("nightboost", Config.COMMON.enableNightBoost::get,
                        value -> Config.set(Config.COMMON.enableNightBoost, value), "Night boost"))
                .then(CommandUtil.toggleSetting("horde", Config.COMMON.enableHordeEvents::get,
                        value -> Config.set(Config.COMMON.enableHordeEvents, value), "Scheduled hordes"))
                .then(CommandUtil.toggleSetting("daycounter", Config.COMMON.enableDayCounterAnnouncements::get,
                        value -> Config.set(Config.COMMON.enableDayCounterAnnouncements, value), "Morning day counter"))
                .then(CommandUtil.doubleSetting("hordechance", "chance", 0.0, 1.0,
                        Config.COMMON.hordeStartChance::get,
                        value -> Config.set(Config.COMMON.hordeStartChance, value),
                        value -> "Horde start chance: " + CommandUtil.percent(value),
                        0.0, 0.25, 0.5, 0.75, 1.0))
                .then(CommandUtil.toggleSetting("bloodmoon", Config.COMMON.enableBloodMoon::get,
                        value -> Config.set(Config.COMMON.enableBloodMoon, value), "Random blood moons"))
                .then(CommandUtil.toggleSetting("scaling", Config.COMMON.enableDifficultyScaling::get,
                        value -> Config.set(Config.COMMON.enableDifficultyScaling, value), "Difficulty scaling"))
                .then(CommandUtil.toggleSetting("attributes", Config.COMMON.enableAttributeModifiers::get,
                        value -> Config.set(Config.COMMON.enableAttributeModifiers, value), "Attribute modifiers"))
                .then(CommandUtil.toggleSetting("attributescaling", Config.COMMON.scaleAttributesWithDifficulty::get,
                        value -> Config.set(Config.COMMON.scaleAttributesWithDifficulty, value), "Attribute scaling with difficulty"))
                .then(CommandUtil.toggleSetting("variantprofiles", Config.COMMON.enableVariantAttributeProfiles::get,
                        value -> Config.set(Config.COMMON.enableVariantAttributeProfiles, value), "Variant attribute profiles"))
                .then(CommandUtil.toggleSetting("contextprofiles", Config.COMMON.enableBiomeDimensionAttributeMultipliers::get,
                        value -> Config.set(Config.COMMON.enableBiomeDimensionAttributeMultipliers, value), "Biome/dimension context profiles"))
                .then(CommandUtil.toggleSetting("biomes", Config.COMMON.enableBiomeModifiers::get,
                        value -> Config.set(Config.COMMON.enableBiomeModifiers, value), "Biome modifiers"))
                .then(CommandUtil.toggleSetting("nether", Config.COMMON.netherSpawning::get,
                        value -> Config.set(Config.COMMON.netherSpawning, value), "Nether spawning"))
                .then(CommandUtil.toggleSetting("end", Config.COMMON.endSpawning::get,
                        value -> Config.set(Config.COMMON.endSpawning, value), "End spawning"))
                .then(CommandUtil.toggleSetting("cooldown", Config.COMMON.enableDeathCooldown::get,
                        value -> Config.set(Config.COMMON.enableDeathCooldown, value), "Death cooldown"))
                .then(CommandUtil.toggleSetting("effects", Config.COMMON.enableSpawnEffects::get,
                        value -> Config.set(Config.COMMON.enableSpawnEffects, value), "Spawn effects"))
                .then(CommandUtil.toggleSetting("debug", Config.COMMON.enableDebugLogging::get,
                        value -> Config.set(Config.COMMON.enableDebugLogging, value), "Debug logging")));
    }

    private static String formatDaylightStart(int value) {
        String message = value <= 0
                ? "Daytime custom spawning starts immediately."
                : "Daytime custom spawning starts on day " + value + '.';
        if (!Config.COMMON.enableDaytimeSpawning.get()) {
            message += " This grace setting is saved but ignored while night-only mode is on.";
        }
        return message;
    }

    private static int setSpawning(CommandSourceStack source, boolean enabled) {
        Config.set(Config.COMMON.enableDaySpawning, enabled);
        CommandUtil.feedback(source, "Custom zombie waves: " + CommandUtil.onOff(enabled), true);
        return 1;
    }

    private static int setDaytimeSpawning(CommandSourceStack source, boolean enabled) {
        Config.set(Config.COMMON.enableDaytimeSpawning, enabled);
        String message = enabled
                ? "Daytime custom waves: ON. Temporary start day: "
                        + Config.COMMON.daylightSpawnStartDay.get() + "."
                : "Daytime custom waves: OFF (night-only). Night waves and blood moons still work; scheduled dawn hordes are paused.";
        CommandUtil.feedback(source, message, true);
        return 1;
    }

    private static int showStatus(CommandSourceStack source, boolean detailed) {
        CommandUtil.feedback(source, detailed ? buildDetailedStatusMessage() : buildStatusMessage(), false);
        return 1;
    }

    private static String buildStatusMessage() {
        int maxLight = Config.COMMON.maxBlockLightForSpawning.get();
        StringBuilder status = new StringBuilder("Custom spawning:\n");
        status.append("State: ").append(CommandUtil.onOff(Config.COMMON.enableDaySpawning.get())).append('\n');
        status.append("Wave: ").append(CommandUtil.percent(Config.COMMON.daySpawnChance.get()))
                .append(" every ").append(CommandUtil.ticks(Config.COMMON.daySpawnInterval.get()))
                .append(" | ").append(CommandUtil.count(Config.COMMON.zombiesPerSpawn.get(), "zombie")).append('\n');
        status.append("Nearby cap: ").append(Config.COMMON.maxDayZombiesPerPlayer.get())
                .append(" per player | Spawn distance: ")
                .append(Config.COMMON.minSpawnDistance.get()).append('-')
                .append(Config.COMMON.spawnRange.get()).append(" blocks\n");
        status.append("Daytime spawning: ").append(formatDaytimeSpawning())
                .append(" | Open sky: ")
                .append(CommandUtil.onOff(Config.COMMON.requireOpenSkyForOverworldSpawns.get())).append('\n');
        status.append("Base light protection: ")
                .append(maxLight < 0 ? "OFF (block light ignored)" : "ON (spawns need light " + maxLight + " or lower)")
                .append('\n');
        status.append("Variants: ").append(CommandUtil.onOff(Config.COMMON.enableZombieVariants.get()))
                .append(" | Custom baby chance: ").append(CommandUtil.percent(Config.COMMON.babyZombieChance.get()))
                .append(" | Night boost: ").append(CommandUtil.onOff(Config.COMMON.enableNightBoost.get())).append('\n');
        status.append("Use /za spawn status all for every related toggle.");
        status.append(buildSpawnDistanceWarning());
        return status.toString();
    }

    private static String buildDetailedStatusMessage() {
        StringBuilder status = new StringBuilder();
        status.append("All spawn-related settings:\n");
        status.append("Enabled: ").append(CommandUtil.onOff(Config.COMMON.enableDaySpawning.get())).append('\n');
        status.append("Daytime custom waves: ").append(formatDaytimeSpawning()).append('\n');
        status.append("Interval: ").append(CommandUtil.ticks(Config.COMMON.daySpawnInterval.get())).append('\n');
        status.append("Chance: ").append(CommandUtil.percent(Config.COMMON.daySpawnChance.get())).append('\n');
        status.append("Max zombies/player: ").append(Config.COMMON.maxDayZombiesPerPlayer.get()).append('\n');
        status.append("Per spawn: ").append(Config.COMMON.zombiesPerSpawn.get()).append('\n');
        status.append("Attempts per zombie: ").append(Config.COMMON.spawnAttemptsPerZombie.get()).append('\n');
        status.append("Range: ").append(Config.COMMON.spawnRange.get()).append(" blocks\n");
        status.append("Min distance: ").append(Config.COMMON.minSpawnDistance.get()).append(" blocks\n");
        status.append("Daylight spawn start day: ").append(Config.COMMON.daylightSpawnStartDay.get()).append('\n');
        status.append("Max block light: ").append(formatMaxBlockLight(Config.COMMON.maxBlockLightForSpawning.get()))
                .append('\n');
        status.append("Require overworld sky: ").append(CommandUtil.onOff(Config.COMMON.requireOpenSkyForOverworldSpawns.get()))
                .append('\n');
        status.append("Variants: ").append(CommandUtil.onOff(Config.COMMON.enableZombieVariants.get())).append('\n');
        status.append("Custom baby zombie chance: ").append(CommandUtil.percent(Config.COMMON.babyZombieChance.get()));
        if (Config.COMMON.babyZombieChance.get() <= 0.0) {
            status.append(" (all new Zombie subclasses forced adult)");
        }
        status.append('\n');
        status.append("Night boost: ").append(CommandUtil.onOff(Config.COMMON.enableNightBoost.get())).append('\n');
        status.append("Horde events: ").append(CommandUtil.onOff(Config.COMMON.enableHordeEvents.get())).append('\n');
        status.append("Morning day counter: ").append(CommandUtil.onOff(Config.COMMON.enableDayCounterAnnouncements.get()))
                .append('\n');
        status.append("Horde start chance: ").append(CommandUtil.percent(Config.COMMON.hordeStartChance.get())).append('\n');
        status.append("Event interval: ").append(CommandUtil.ticks(Config.COMMON.eventSpawnInterval.get())).append('\n');
        status.append("Blood moon: ").append(CommandUtil.onOff(Config.COMMON.enableBloodMoon.get())).append('\n');
        status.append("Scaling: ").append(CommandUtil.onOff(Config.COMMON.enableDifficultyScaling.get())).append('\n');
        status.append("Attributes: ").append(CommandUtil.onOff(Config.COMMON.enableAttributeModifiers.get())).append('\n');
        status.append("Attribute scaling: ").append(CommandUtil.onOff(Config.COMMON.scaleAttributesWithDifficulty.get()))
                .append('\n');
        status.append("Variant profiles: ").append(CommandUtil.onOff(Config.COMMON.enableVariantAttributeProfiles.get()))
                .append('\n');
        status.append("Context profiles: ")
                .append(CommandUtil.onOff(Config.COMMON.enableBiomeDimensionAttributeMultipliers.get()))
                .append('\n');
        status.append("Base multipliers H/ATK/SPD: ")
                .append(CommandUtil.multiplier(Config.COMMON.baseHealthMultiplier.get()))
                .append(" / ")
                .append(CommandUtil.multiplier(Config.COMMON.baseAttackMultiplier.get()))
                .append(" / ")
                .append(CommandUtil.multiplier(Config.COMMON.baseSpeedMultiplier.get()))
                .append('\n');
        status.append("Use /za attributes for live attribute tuning.\n");
        status.append("Biome modifiers: ").append(CommandUtil.onOff(Config.COMMON.enableBiomeModifiers.get())).append('\n');
        status.append("Nether spawning: ").append(CommandUtil.onOff(Config.COMMON.netherSpawning.get())).append('\n');
        status.append("End spawning: ").append(CommandUtil.onOff(Config.COMMON.endSpawning.get())).append('\n');
        status.append("Death cooldown: ").append(CommandUtil.onOff(Config.COMMON.enableDeathCooldown.get())).append('\n');
        status.append("Spawn effects: ").append(CommandUtil.onOff(Config.COMMON.enableSpawnEffects.get())).append('\n');
        status.append("Debug logging: ").append(CommandUtil.onOff(Config.COMMON.enableDebugLogging.get()));
        status.append(buildSpawnDistanceWarning());
        return status.toString();
    }

    private static String buildSpawnDistanceWarning() {
        int minDistance = Config.COMMON.minSpawnDistance.get();
        int spawnRange = Config.COMMON.spawnRange.get();
        if (!SpawnMath.isSpawnDistanceImpossible(minDistance, spawnRange)) {
            return "";
        }

        return "\nWARNING: Custom spawning is paused because min distance " + minDistance
                + " cannot fit inside range " + spawnRange
                + ". Lower min distance to " + SpawnMath.maxHorizontalDistance(spawnRange)
                + " or less, or raise the range.";
    }

    private static String formatMaxBlockLight(int value) {
        return value < 0 ? "ignored" : Integer.toString(value);
    }

    private static String formatDaytimeSpawning() {
        if (!Config.COMMON.enableDaytimeSpawning.get()) {
            return "OFF (night-only; saved start day " + Config.COMMON.daylightSpawnStartDay.get() + ')';
        }
        return "ON (starts day " + Config.COMMON.daylightSpawnStartDay.get() + ')';
    }
}
