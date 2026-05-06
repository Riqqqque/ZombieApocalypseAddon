package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.rique.zombieapocalypse.Config;

public final class DaySpawnCommands {

    private DaySpawnCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zdayspawn")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("status")
                        .executes(context -> {
                            CommandUtil.feedback(context.getSource(), buildStatusMessage(), false);
                            return 1;
                        }))
                .then(Commands.literal("enabled")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean enabled = BoolArgumentType.getBool(context, "value");
                                    Config.COMMON.enableDaySpawning.set(enabled);
                                    CommandUtil.feedback(context.getSource(), "Day spawning: " + CommandUtil.onOff(enabled),
                                            true);
                                    return 1;
                                })))
                .then(Commands.literal("chance")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "value");
                                    Config.COMMON.daySpawnChance.set(value);
                                    CommandUtil.feedback(context.getSource(), "Spawn chance: " + CommandUtil.percent(value),
                                            true);
                                    return 1;
                                })))
                .then(Commands.literal("babychance")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "value");
                                    Config.COMMON.babyZombieChance.set(value);
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
                                    Config.COMMON.daySpawnInterval.set(ticks);
                                    CommandUtil.feedback(context.getSource(), "Spawn interval: " + ticks + " ticks", true);
                                    return 1;
                                })))
                .then(Commands.literal("eventinterval")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(1, 200))
                                .executes(context -> {
                                    int ticks = IntegerArgumentType.getInteger(context, "ticks");
                                    Config.COMMON.eventSpawnInterval.set(ticks);
                                    CommandUtil.feedback(context.getSource(), "Event spawn interval: " + ticks + " ticks",
                                            true);
                                    return 1;
                                })))
                .then(Commands.literal("amount")
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 50))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "value");
                                    Config.COMMON.zombiesPerSpawn.set(value);
                                    CommandUtil.feedback(context.getSource(), "Zombies per spawn: " + value, true);
                                    return 1;
                                })))
                .then(Commands.literal("attempts")
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 40))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "value");
                                    Config.COMMON.spawnAttemptsPerZombie.set(value);
                                    CommandUtil.feedback(context.getSource(), "Spawn attempts per zombie: " + value, true);
                                    return 1;
                                })))
                .then(Commands.literal("max")
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 500))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "value");
                                    Config.COMMON.maxDayZombiesPerPlayer.set(value);
                                    CommandUtil.feedback(context.getSource(), "Max nearby zombies per player: " + value, true);
                                    return 1;
                                })))
                .then(Commands.literal("range")
                        .then(Commands.argument("blocks", IntegerArgumentType.integer(16, 128))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "blocks");
                                    Config.COMMON.spawnRange.set(value);
                                    CommandUtil.feedback(context.getSource(), "Spawn range: " + value + " blocks", true);
                                    return 1;
                                })))
                .then(Commands.literal("mindist")
                        .then(Commands.argument("blocks", IntegerArgumentType.integer(8, 64))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "blocks");
                                    Config.COMMON.minSpawnDistance.set(value);
                                    CommandUtil.feedback(context.getSource(), "Minimum spawn distance: " + value + " blocks", true);
                                    return 1;
                                })))
                .then(Commands.literal("daylightstart")
                        .then(Commands.argument("day", IntegerArgumentType.integer(0, 3650))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "day");
                                    Config.COMMON.daylightSpawnStartDay.set(value);
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
                                    Config.COMMON.maxBlockLightForSpawning.set(value);
                                    String message = value < 0
                                            ? "Custom spawning ignores block light."
                                            : "Custom spawning max block light: " + value;
                                    CommandUtil.feedback(context.getSource(), message, true);
                                    return 1;
                                })))
                .then(toggleBoolNode("sky", Config.COMMON.requireOpenSkyForOverworldSpawns::set,
                        "Require open sky in overworld"))
                .then(toggleBoolNode("variants", Config.COMMON.enableZombieVariants::set, "Zombie variants"))
                .then(toggleBoolNode("nightboost", Config.COMMON.enableNightBoost::set, "Night boost"))
                .then(toggleBoolNode("horde", Config.COMMON.enableHordeEvents::set, "Horde events"))
                .then(toggleBoolNode("daycounter", Config.COMMON.enableDayCounterAnnouncements::set,
                        "Morning day counter"))
                .then(Commands.literal("hordechance")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "value");
                                    Config.COMMON.hordeStartChance.set(value);
                                    CommandUtil.feedback(context.getSource(), "Horde start chance: " + CommandUtil.percent(value),
                                            true);
                                    return 1;
                                })))
                .then(toggleBoolNode("bloodmoon", Config.COMMON.enableBloodMoon::set, "Blood moon"))
                .then(toggleBoolNode("scaling", Config.COMMON.enableDifficultyScaling::set, "Difficulty scaling"))
                .then(toggleBoolNode("attributes", Config.COMMON.enableAttributeModifiers::set, "Attribute modifiers"))
                .then(toggleBoolNode("attributescaling", Config.COMMON.scaleAttributesWithDifficulty::set,
                        "Attribute scaling with difficulty"))
                .then(toggleBoolNode("variantprofiles", Config.COMMON.enableVariantAttributeProfiles::set,
                        "Variant attribute profiles"))
                .then(toggleBoolNode("contextprofiles", Config.COMMON.enableBiomeDimensionAttributeMultipliers::set,
                        "Biome/dimension context profiles"))
                .then(toggleBoolNode("biomes", Config.COMMON.enableBiomeModifiers::set, "Biome modifiers"))
                .then(toggleBoolNode("nether", Config.COMMON.netherSpawning::set, "Nether spawning"))
                .then(toggleBoolNode("end", Config.COMMON.endSpawning::set, "End spawning"))
                .then(toggleBoolNode("cooldown", Config.COMMON.enableDeathCooldown::set, "Death cooldown"))
                .then(toggleBoolNode("effects", Config.COMMON.enableSpawnEffects::set, "Spawn effects"))
                .then(toggleBoolNode("debug", Config.COMMON.enableDebugLogging::set, "Debug logging")));
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

    private static String buildStatusMessage() {
        StringBuilder status = new StringBuilder();
        status.append("Zombie apocalypse settings:\n");
        status.append("Enabled: ").append(CommandUtil.onOff(Config.COMMON.enableDaySpawning.get())).append('\n');
        status.append("Interval: ").append(Config.COMMON.daySpawnInterval.get()).append(" ticks\n");
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
        status.append("Event interval: ").append(Config.COMMON.eventSpawnInterval.get()).append(" ticks\n");
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
        return status.toString();
    }

    private static String formatMaxBlockLight(int value) {
        return value < 0 ? "ignored" : Integer.toString(value);
    }
}
