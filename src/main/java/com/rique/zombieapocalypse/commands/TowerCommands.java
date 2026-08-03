package com.rique.zombieapocalypse.commands;

import java.util.function.Consumer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.DifficultyManager;

public final class TowerCommands {

    private TowerCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ztower")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showStatus(context.getSource()))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource())))
                .then(Commands.literal("dayone")
                        .executes(context -> {
                            Config.COMMON.enableZombieTowering.set(true);
                            Config.COMMON.zombieToweringStartDay.set(0);
                            CommandUtil.feedback(context.getSource(),
                                    "Zombie towering enabled and set to start immediately.", true);
                            return 1;
                        }))
                .then(toggleBoolNode("enabled", Config.COMMON.enableZombieTowering::set,
                        "Zombie towering"))
                .then(Commands.literal("startday")
                        .then(Commands.argument("day", IntegerArgumentType.integer(0, 3650))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "day");
                                    Config.COMMON.zombieToweringStartDay.set(value);
                                    String message = value == 0
                                            ? "Zombie towering can start immediately when enabled."
                                            : "Zombie towering starts on day " + value + '.';
                                    CommandUtil.feedback(context.getSource(), message, true);
                                    return 1;
                                })))
                .then(Commands.literal("interval")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(5, 72000))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "ticks");
                                    Config.COMMON.zombieToweringInterval.set(value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie towering interval: " + value + " ticks", true);
                                    return 1;
                                })))
                .then(Commands.literal("chance")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "value");
                                    Config.COMMON.zombieToweringChance.set(value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie towering chance: " + CommandUtil.percent(value), true);
                                    return 1;
                                })))
                .then(Commands.literal("distance")
                        .then(Commands.argument("blocks", IntegerArgumentType.integer(4, 128))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "blocks");
                                    Config.COMMON.zombieToweringMaxTargetDistance.set(value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie towering target distance: " + value + " blocks", true);
                                    return 1;
                                })))
                .then(Commands.literal("crowd")
                        .then(Commands.argument("zombies", IntegerArgumentType.integer(1, 16))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "zombies");
                                    Config.COMMON.zombieToweringMinNearbyZombies.set(value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Nearby zombies required for towering: " + value, true);
                                    return 1;
                                })))
                .then(Commands.literal("radius")
                        .then(Commands.argument("blocks", DoubleArgumentType.doubleArg(0.75, 6.0))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "blocks");
                                    Config.COMMON.zombieToweringCrowdRadius.set(value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie towering crowd radius: " + value + " blocks", true);
                                    return 1;
                                })))
                .then(Commands.literal("vertical")
                        .then(Commands.argument("velocity", DoubleArgumentType.doubleArg(0.1, 1.0))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "velocity");
                                    Config.COMMON.zombieToweringVerticalBoost.set(value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie towering vertical boost: " + value, true);
                                    return 1;
                                })))
                .then(Commands.literal("forward")
                        .then(Commands.argument("velocity", DoubleArgumentType.doubleArg(0.0, 0.6))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "velocity");
                                    Config.COMMON.zombieToweringForwardBoost.set(value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie towering forward boost: " + value, true);
                                    return 1;
                                })))
                .then(Commands.literal("height")
                        .then(Commands.argument("blocks", IntegerArgumentType.integer(1, 32))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "blocks");
                                    Config.COMMON.zombieToweringMaxHeightAboveTarget.set(value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie towering height above target: " + value + " blocks", true);
                                    return 1;
                                })))
                .then(toggleBoolNode("obstacle", Config.COMMON.zombieToweringRequireObstacle::set,
                        "Require obstacle or raised/covered target")));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> toggleBoolNode(
            String literal,
            Consumer<Boolean> setter,
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

    private static int showStatus(CommandSourceStack source) {
        long currentDay = DifficultyManager.getCurrentDay(source.getLevel());
        int startDay = Config.COMMON.zombieToweringStartDay.get();
        boolean enabled = Config.COMMON.enableZombieTowering.get();
        boolean active = enabled && currentDay >= startDay;

        StringBuilder status = new StringBuilder();
        status.append("Zombie towering settings:\n");
        status.append("Enabled: ").append(CommandUtil.onOff(enabled)).append('\n');
        status.append("Active today: ").append(CommandUtil.onOff(active))
                .append(" (current day ").append(currentDay)
                .append(", start day ").append(startDay).append(")\n");
        status.append("Interval: ").append(Config.COMMON.zombieToweringInterval.get()).append(" ticks\n");
        status.append("Chance: ").append(CommandUtil.percent(Config.COMMON.zombieToweringChance.get())).append('\n');
        status.append("Max target distance: ")
                .append(Config.COMMON.zombieToweringMaxTargetDistance.get()).append(" blocks\n");
        status.append("Nearby zombies required: ")
                .append(Config.COMMON.zombieToweringMinNearbyZombies.get()).append('\n');
        status.append("Crowd radius: ").append(Config.COMMON.zombieToweringCrowdRadius.get()).append(" blocks\n");
        status.append("Vertical boost: ").append(Config.COMMON.zombieToweringVerticalBoost.get()).append('\n');
        status.append("Forward boost: ").append(Config.COMMON.zombieToweringForwardBoost.get()).append('\n');
        status.append("Max height above target: ")
                .append(Config.COMMON.zombieToweringMaxHeightAboveTarget.get()).append(" blocks\n");
        status.append("Require obstacle/raised target: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieToweringRequireObstacle.get()));
        CommandUtil.feedback(source, status.toString(), false);
        return 1;
    }
}
