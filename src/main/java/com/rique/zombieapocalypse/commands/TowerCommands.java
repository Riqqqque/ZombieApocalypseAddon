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
                .then(Commands.literal("on")
                        .executes(context -> setEnabled(context.getSource(), true)))
                .then(Commands.literal("off")
                        .executes(context -> setEnabled(context.getSource(), false)))
                .then(Commands.literal("dayone")
                        .executes(context -> {
                            Config.edit(() -> {
                                Config.set(Config.COMMON.enableZombieTowering, true);
                                Config.set(Config.COMMON.zombieToweringStartDay, 0);
                            });
                            CommandUtil.feedback(context.getSource(),
                                    "Zombie towering enabled and set to start immediately.", true);
                            return 1;
                        }))
                .then(toggleBoolNode("enabled", value -> Config.set(Config.COMMON.enableZombieTowering, value),
                        "Zombie towering"))
                .then(Commands.literal("startday")
                        .then(Commands.argument("day", IntegerArgumentType.integer(0, 3650))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "day");
                                    Config.set(Config.COMMON.zombieToweringStartDay, value);
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
                                    Config.set(Config.COMMON.zombieToweringInterval, value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie towering interval: " + CommandUtil.ticks(value), true);
                                    return 1;
                                })))
                .then(Commands.literal("chance")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "value");
                                    Config.set(Config.COMMON.zombieToweringChance, value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie towering chance: " + CommandUtil.percent(value), true);
                                    return 1;
                                })))
                .then(Commands.literal("distance")
                        .then(Commands.argument("blocks", IntegerArgumentType.integer(4, 128))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "blocks");
                                    Config.set(Config.COMMON.zombieToweringMaxTargetDistance, value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie towering target distance: " + value + " blocks", true);
                                    return 1;
                                })))
                .then(Commands.literal("crowd")
                        .then(Commands.argument("zombies", IntegerArgumentType.integer(1, 16))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "zombies");
                                    Config.set(Config.COMMON.zombieToweringMinNearbyZombies, value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Nearby zombies required for towering: " + value, true);
                                    return 1;
                                })))
                .then(Commands.literal("radius")
                        .then(Commands.argument("blocks", DoubleArgumentType.doubleArg(0.75, 6.0))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "blocks");
                                    Config.set(Config.COMMON.zombieToweringCrowdRadius, value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie towering crowd radius: " + value + " blocks", true);
                                    return 1;
                                })))
                .then(Commands.literal("vertical")
                        .then(Commands.argument("velocity", DoubleArgumentType.doubleArg(0.1, 1.0))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "velocity");
                                    Config.set(Config.COMMON.zombieToweringVerticalBoost, value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie towering vertical boost: " + value, true);
                                    return 1;
                                })))
                .then(Commands.literal("forward")
                        .then(Commands.argument("velocity", DoubleArgumentType.doubleArg(0.0, 0.6))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "velocity");
                                    Config.set(Config.COMMON.zombieToweringForwardBoost, value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie towering forward boost: " + value, true);
                                    return 1;
                                })))
                .then(Commands.literal("height")
                        .then(Commands.argument("blocks", IntegerArgumentType.integer(1, 32))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "blocks");
                                    Config.set(Config.COMMON.zombieToweringMaxHeightAboveTarget, value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie towering height above target: " + value + " blocks", true);
                                    return 1;
                                })))
                .then(toggleBoolNode("obstacle", value -> Config.set(Config.COMMON.zombieToweringRequireObstacle, value),
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

    private static int setEnabled(CommandSourceStack source, boolean enabled) {
        Config.set(Config.COMMON.enableZombieTowering, enabled);
        CommandUtil.feedback(source, "Zombie towering: " + CommandUtil.onOff(enabled), true);
        return 1;
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
        status.append("Interval: ").append(CommandUtil.ticks(Config.COMMON.zombieToweringInterval.get())).append('\n');
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
