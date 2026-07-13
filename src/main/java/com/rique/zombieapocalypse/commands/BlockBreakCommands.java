package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.DifficultyManager;

public final class BlockBreakCommands {

    private BlockBreakCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zblockbreak")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    CommandUtil.feedback(context.getSource(), buildStatusMessage(context.getSource()), false);
                    return 1;
                })
                .then(Commands.literal("status")
                        .executes(context -> {
                            CommandUtil.feedback(context.getSource(), buildStatusMessage(context.getSource()), false);
                            return 1;
                        }))
                .then(Commands.literal("dayone")
                        .executes(context -> {
                            Config.COMMON.enableZombieBlockBreaking.set(true);
                            Config.COMMON.zombieBlockBreakingStartDay.set(0);
                            CommandUtil.feedback(context.getSource(),
                                    "Zombie block breaking enabled and set to start immediately.", true);
                            return 1;
                        }))
                .then(toggleBoolNode("enabled", Config.COMMON.enableZombieBlockBreaking::set, "Zombie block breaking"))
                .then(Commands.literal("startday")
                        .then(Commands.argument("day", IntegerArgumentType.integer(0, 3650))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "day");
                                    Config.COMMON.zombieBlockBreakingStartDay.set(value);
                                    String message = value <= 0
                                            ? "Zombie block breaking can start immediately when enabled."
                                            : "Zombie block breaking starts on day " + value + '.';
                                    CommandUtil.feedback(context.getSource(), message, true);
                                    return 1;
                                })))
                .then(Commands.literal("interval")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(20, 72000))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "ticks");
                                    Config.COMMON.zombieBlockBreakingInterval.set(value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie block breaking interval: " + value + " ticks", true);
                                    return 1;
                                })))
                .then(Commands.literal("chance")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "value");
                                    Config.COMMON.zombieBlockBreakingChance.set(value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie block breaking chance: " + CommandUtil.percent(value), true);
                                    return 1;
                                })))
                .then(Commands.literal("range")
                        .then(Commands.argument("blocks", IntegerArgumentType.integer(1, 4))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "blocks");
                                    Config.COMMON.zombieBlockBreakingRange.set(value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie block breaking range: " + value + " blocks", true);
                                    return 1;
                                })))
                .then(Commands.literal("hardness")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 50.0))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "value");
                                    Config.COMMON.zombieBlockBreakingMaxHardness.set(value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie block breaking max hardness: " + CommandUtil.number(value), true);
                                    return 1;
                                })))
                .then(toggleBoolNode("drops", Config.COMMON.zombieBlockBreakingDropBlocks::set,
                        "Block drops from zombie breaking"))
                .then(toggleBoolNode("target", Config.COMMON.zombieBlockBreakingRequireTarget::set,
                        "Require zombie target"))
                .then(toggleBoolNode("obstacle", Config.COMMON.zombieBlockBreakingRequireObstacle::set,
                        "Require blocked path/covered target"))
                .then(toggleBoolNode("mobgriefing", Config.COMMON.zombieBlockBreakingRespectMobGriefing::set,
                        "Respect mobGriefing"))
                .then(toggleBoolNode("containers", Config.COMMON.zombieBlockBreakingAllowBlockEntities::set,
                        "Allow containers/block entities"))
                .then(toggleBoolNode("toolblocks", Config.COMMON.zombieBlockBreakingAllowToolRequiredBlocks::set,
                        "Allow tool-required blocks"))
                .then(toggleBoolNode("lights", Config.COMMON.zombieBlockBreakingAllowLightBlocks::set,
                        "Allow light-emitting blocks")));
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

    private static String buildStatusMessage(CommandSourceStack source) {
        long currentDay = DifficultyManager.getCurrentDay(source.getLevel());
        int startDay = Config.COMMON.zombieBlockBreakingStartDay.get();
        boolean enabled = Config.COMMON.enableZombieBlockBreaking.get();
        boolean active = enabled && currentDay >= startDay;

        StringBuilder status = new StringBuilder();
        status.append("Zombie block breaking settings:\n");
        status.append("Enabled: ").append(CommandUtil.onOff(enabled)).append('\n');
        status.append("Active today: ").append(CommandUtil.onOff(active))
                .append(" (current day ").append(currentDay)
                .append(", start day ").append(startDay).append(")\n");
        status.append("Interval: ").append(Config.COMMON.zombieBlockBreakingInterval.get()).append(" ticks\n");
        status.append("Chance: ").append(CommandUtil.percent(Config.COMMON.zombieBlockBreakingChance.get())).append('\n');
        status.append("Range: ").append(Config.COMMON.zombieBlockBreakingRange.get()).append(" blocks\n");
        status.append("Max hardness: ")
                .append(CommandUtil.number(Config.COMMON.zombieBlockBreakingMaxHardness.get())).append('\n');
        status.append("Drop broken blocks: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieBlockBreakingDropBlocks.get())).append('\n');
        status.append("Require target: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieBlockBreakingRequireTarget.get())).append('\n');
        status.append("Require obstacle: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieBlockBreakingRequireObstacle.get())).append('\n');
        status.append("Respect mobGriefing: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieBlockBreakingRespectMobGriefing.get())).append('\n');
        status.append("Allow containers/block entities: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieBlockBreakingAllowBlockEntities.get())).append('\n');
        status.append("Allow tool-required blocks: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieBlockBreakingAllowToolRequiredBlocks.get())).append('\n');
        status.append("Allow light-emitting blocks: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieBlockBreakingAllowLightBlocks.get()));
        return status.toString();
    }
}
