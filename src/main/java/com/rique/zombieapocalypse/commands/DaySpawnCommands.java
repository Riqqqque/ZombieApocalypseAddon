package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.SpawnMath;

public final class DaySpawnCommands {

    private DaySpawnCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zdayspawn")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showStatus(context.getSource(), false))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource(), false))
                        .then(Commands.literal("all")
                                .executes(context -> showStatus(context.getSource(), true))))
                .then(Commands.literal("on")
                        .executes(context -> setSpawning(context.getSource(), true)))
                .then(Commands.literal("off")
                        .executes(context -> setSpawning(context.getSource(), false)))
                .then(Commands.literal("enabled")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean enabled = BoolArgumentType.getBool(context, "value");
                                    Config.set(Config.COMMON.enableDaySpawning, enabled);
                                    CommandUtil.feedback(context.getSource(), "Day spawning: " + CommandUtil.onOff(enabled),
                                            true);
                                    return 1;
                                })))
                .then(Commands.literal("chance")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "value");
                                    Config.set(Config.COMMON.daySpawnChance, value);
                                    CommandUtil.feedback(context.getSource(), "Spawn chance: " + CommandUtil.percent(value),
                                            true);
                                    return 1;
                                })))
                .then(Commands.literal("babychance")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "value");
                                    Config.set(Config.COMMON.babyZombieChance, value);
                                    String message = value <= 0.0
                                            ? "Baby zombie spawns disabled."
                                            : "Baby zombie chance: " + CommandUtil.percent(value);
                                    CommandUtil.feedback(context.getSource(), message, true);
                                    return 1;
                                })))
                .then(Commands.literal("interval")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(1, 72000))
                                .executes(context -> {
                                    int ticks = IntegerArgumentType.getInteger(context, "ticks");
                                    Config.set(Config.COMMON.daySpawnInterval, ticks);
                                    CommandUtil.feedback(context.getSource(),
                                            "Spawn interval: " + CommandUtil.ticks(ticks), true);
                                    return 1;
                                })))
                .then(Commands.literal("eventinterval")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(1, 200))
                                .executes(context -> {
                                    int ticks = IntegerArgumentType.getInteger(context, "ticks");
                                    Config.set(Config.COMMON.eventSpawnInterval, ticks);
                                    CommandUtil.feedback(context.getSource(),
                                            "Event spawn interval: " + CommandUtil.ticks(ticks),
                                            true);
                                    return 1;
                                })))
                .then(Commands.literal("amount")
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 50))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "value");
                                    Config.set(Config.COMMON.zombiesPerSpawn, value);
                                    CommandUtil.feedback(context.getSource(), "Zombies per spawn: " + value, true);
                                    return 1;
                                })))
                .then(Commands.literal("attempts")
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 40))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "value");
                                    Config.set(Config.COMMON.spawnAttemptsPerZombie, value);
                                    CommandUtil.feedback(context.getSource(), "Spawn attempts per zombie: " + value, true);
                                    return 1;
                                })))
                .then(Commands.literal("max")
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 500))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "value");
                                    Config.set(Config.COMMON.maxDayZombiesPerPlayer, value);
                                    CommandUtil.feedback(context.getSource(), "Max nearby zombies per player: " + value, true);
                                    return 1;
                                })))
                .then(Commands.literal("range")
                        .then(Commands.argument("blocks", IntegerArgumentType.integer(16, 128))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "blocks");
                                    Config.set(Config.COMMON.spawnRange, value);
                                    CommandUtil.feedback(
                                            context.getSource(),
                                            "Spawn range: " + value + " blocks" + buildSpawnDistanceWarning(),
                                            true);
                                    return 1;
                                })))
                .then(Commands.literal("mindist")
                        .then(Commands.argument("blocks", IntegerArgumentType.integer(8, 64))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "blocks");
                                    Config.set(Config.COMMON.minSpawnDistance, value);
                                    CommandUtil.feedback(
                                            context.getSource(),
                                            "Minimum spawn distance: " + value + " blocks" + buildSpawnDistanceWarning(),
                                            true);
                                    return 1;
                                })))
                .then(Commands.literal("daylightstart")
                        .then(Commands.argument("day", IntegerArgumentType.integer(0, 3650))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "day");
                                    Config.set(Config.COMMON.daylightSpawnStartDay, value);
                                    String message = value <= 0
                                            ? "Daytime custom spawning starts immediately."
                                            : "Daytime custom spawning starts on day " + value + '.';
                                    CommandUtil.feedback(context.getSource(), message, true);
                                    return 1;
                                })))
                .then(Commands.literal("maxlight")
                        .then(Commands.argument("level", IntegerArgumentType.integer(-1, 15))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "level");
                                    Config.set(Config.COMMON.maxBlockLightForSpawning, value);
                                    String message = value < 0
                                            ? "Custom spawning ignores block light."
                                            : "Custom spawning max block light: " + value;
                                    CommandUtil.feedback(context.getSource(), message, true);
                                    return 1;
                                })))
                .then(toggleBoolNode("sky", value -> Config.set(Config.COMMON.requireOpenSkyForOverworldSpawns, value),
                        "Require open sky in overworld"))
                .then(toggleBoolNode("variants", value -> Config.set(Config.COMMON.enableZombieVariants, value), "Zombie variants"))
                .then(toggleBoolNode("nightboost", value -> Config.set(Config.COMMON.enableNightBoost, value), "Night boost"))
                .then(toggleBoolNode("horde", value -> Config.set(Config.COMMON.enableHordeEvents, value), "Horde events"))
                .then(toggleBoolNode("daycounter", value -> Config.set(Config.COMMON.enableDayCounterAnnouncements, value),
                        "Morning day counter"))
                .then(Commands.literal("hordechance")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "value");
                                    Config.set(Config.COMMON.hordeStartChance, value);
                                    CommandUtil.feedback(context.getSource(), "Horde start chance: " + CommandUtil.percent(value),
                                            true);
                                    return 1;
                                })))
                .then(toggleBoolNode("bloodmoon", value -> Config.set(Config.COMMON.enableBloodMoon, value), "Blood moon"))
                .then(toggleBoolNode("scaling", value -> Config.set(Config.COMMON.enableDifficultyScaling, value), "Difficulty scaling"))
                .then(toggleBoolNode("attributes", value -> Config.set(Config.COMMON.enableAttributeModifiers, value), "Attribute modifiers"))
                .then(toggleBoolNode("attributescaling", value -> Config.set(Config.COMMON.scaleAttributesWithDifficulty, value),
                        "Attribute scaling with difficulty"))
                .then(toggleBoolNode("variantprofiles", value -> Config.set(Config.COMMON.enableVariantAttributeProfiles, value),
                        "Variant attribute profiles"))
                .then(toggleBoolNode("contextprofiles", value -> Config.set(Config.COMMON.enableBiomeDimensionAttributeMultipliers, value),
                        "Biome/dimension context profiles"))
                .then(toggleBoolNode("biomes", value -> Config.set(Config.COMMON.enableBiomeModifiers, value), "Biome modifiers"))
                .then(toggleBoolNode("nether", value -> Config.set(Config.COMMON.netherSpawning, value), "Nether spawning"))
                .then(toggleBoolNode("end", value -> Config.set(Config.COMMON.endSpawning, value), "End spawning"))
                .then(toggleBoolNode("cooldown", value -> Config.set(Config.COMMON.enableDeathCooldown, value), "Death cooldown"))
                .then(toggleBoolNode("effects", value -> Config.set(Config.COMMON.enableSpawnEffects, value), "Spawn effects"))
                .then(toggleBoolNode("debug", value -> Config.set(Config.COMMON.enableDebugLogging, value), "Debug logging")));
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<CommandSourceStack, ?> toggleBoolNode(
            String literal,
            java.util.function.Consumer<Boolean> setter,
            String label) {
        return Commands.literal(literal)
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                            setter.accept(enabled);
                            CommandUtil.feedback(context.getSource(), label + ": " + CommandUtil.onOff(enabled), true);
                            return 1;
                        }));
    }

    private static int setSpawning(CommandSourceStack source, boolean enabled) {
        Config.set(Config.COMMON.enableDaySpawning, enabled);
        CommandUtil.feedback(source, "Custom zombie waves: " + CommandUtil.onOff(enabled), true);
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
        status.append("Daytime spawning: starts day ").append(Config.COMMON.daylightSpawnStartDay.get())
                .append(" | Open sky: ")
                .append(CommandUtil.onOff(Config.COMMON.requireOpenSkyForOverworldSpawns.get())).append('\n');
        status.append("Base light protection: ")
                .append(maxLight < 0 ? "OFF (block light ignored)" : "ON (spawns need light " + maxLight + " or lower)")
                .append('\n');
        status.append("Variants: ").append(CommandUtil.onOff(Config.COMMON.enableZombieVariants.get()))
                .append(" | Baby chance: ").append(CommandUtil.percent(Config.COMMON.babyZombieChance.get()))
                .append(" | Night boost: ").append(CommandUtil.onOff(Config.COMMON.enableNightBoost.get())).append('\n');
        status.append("Use /zdayspawn status all for every related toggle.");
        status.append(buildSpawnDistanceWarning());
        return status.toString();
    }

    private static String buildDetailedStatusMessage() {
        StringBuilder status = new StringBuilder();
        status.append("All spawn-related settings:\n");
        status.append("Enabled: ").append(CommandUtil.onOff(Config.COMMON.enableDaySpawning.get())).append('\n');
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
        status.append("Baby zombie chance: ").append(CommandUtil.percent(Config.COMMON.babyZombieChance.get())).append('\n');
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
        status.append("Use /zattr for live numeric attribute tuning.\n");
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
}
