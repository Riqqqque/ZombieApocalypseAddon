package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.ConfigLimits;
import com.rique.zombieapocalypse.DifficultyManager;

public final class BlockBreakCommands {

    private BlockBreakCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zblockbreak")
                .executes(context -> {
                    CommandUtil.feedback(context.getSource(), buildStatusMessage(context.getSource()), false);
                    return 1;
                })
                .then(Commands.literal("status")
                        .executes(context -> {
                            CommandUtil.feedback(context.getSource(), buildStatusMessage(context.getSource()), false);
                            return 1;
                        }))
                .then(CommandUtil.admin(Commands.literal("on")
                        .executes(context -> setEnabled(context.getSource(), true))))
                .then(CommandUtil.admin(Commands.literal("off")
                        .executes(context -> setEnabled(context.getSource(), false))))
                .then(CommandUtil.admin(Commands.literal("dayone")
                        .executes(context -> setEnabled(context.getSource(), true))))
                .then(CommandUtil.toggleSetting("enabled",
                        Config.COMMON.enableZombieBlockBreaking::get,
                        BlockBreakCommands::setEnabledValue,
                        "Zombie block breaking"))
                .then(CommandUtil.intSetting("startday", "day", 0, ConfigLimits.MAX_APOCALYPSE_DAY,
                        Config.COMMON.zombieBlockBreakingStartDay::get,
                        value -> Config.set(Config.COMMON.zombieBlockBreakingStartDay, value),
                        value -> value <= 0
                                ? "Zombie block breaking can start immediately when enabled."
                                : "Zombie block breaking starts on day " + value + '.',
                        0, 1, 5, 10, 15, 30, 50, 100))
                .then(CommandUtil.intSetting("interval", "ticks", 20, 72000,
                        Config.COMMON.zombieBlockBreakingInterval::get,
                        value -> Config.set(Config.COMMON.zombieBlockBreakingInterval, value),
                        value -> "Zombie block breaking interval: " + CommandUtil.ticks(value),
                        20, 40, 100, 200, 600, 1200))
                .then(CommandUtil.doubleSetting("chance", "chance", 0.0, 1.0,
                        Config.COMMON.zombieBlockBreakingChance::get,
                        value -> Config.set(Config.COMMON.zombieBlockBreakingChance, value),
                        value -> "Zombie block breaking chance: " + CommandUtil.percent(value),
                        0.0, 0.1, 0.25, 0.5, 0.75, 1.0))
                .then(CommandUtil.intSetting("range", "blocks", 1, 4,
                        Config.COMMON.zombieBlockBreakingRange::get,
                        value -> Config.set(Config.COMMON.zombieBlockBreakingRange, value),
                        value -> "Zombie block breaking range: " + CommandUtil.count(value, "block"),
                        1, 2, 3, 4))
                .then(CommandUtil.doubleSetting("hardness", "hardness", 0.0, 50.0,
                        Config.COMMON.zombieBlockBreakingMaxHardness::get,
                        value -> Config.set(Config.COMMON.zombieBlockBreakingMaxHardness, value),
                        value -> "Zombie block breaking max hardness: " + CommandUtil.number(value),
                        0.5, 1.5, 2.0, 3.0, 5.0, 10.0, 50.0))
                .then(CommandUtil.toggleSetting("drops", Config.COMMON.zombieBlockBreakingDropBlocks::get,
                        value -> Config.set(Config.COMMON.zombieBlockBreakingDropBlocks, value), "Block drops from zombie breaking"))
                .then(CommandUtil.toggleSetting("target", Config.COMMON.zombieBlockBreakingRequireTarget::get,
                        value -> Config.set(Config.COMMON.zombieBlockBreakingRequireTarget, value), "Require zombie target"))
                .then(CommandUtil.toggleSetting("obstacle", Config.COMMON.zombieBlockBreakingRequireObstacle::get,
                        value -> Config.set(Config.COMMON.zombieBlockBreakingRequireObstacle, value), "Require blocked path/covered target"))
                .then(CommandUtil.toggleSetting("mobgriefing", Config.COMMON.zombieBlockBreakingRespectMobGriefing::get,
                        value -> Config.set(Config.COMMON.zombieBlockBreakingRespectMobGriefing, value), "Respect mobGriefing"))
                .then(CommandUtil.toggleSetting("containers", Config.COMMON.zombieBlockBreakingAllowBlockEntities::get,
                        value -> Config.set(Config.COMMON.zombieBlockBreakingAllowBlockEntities, value), "Allow containers/block entities"))
                .then(CommandUtil.toggleSetting("toolblocks", Config.COMMON.zombieBlockBreakingAllowToolRequiredBlocks::get,
                        value -> Config.set(Config.COMMON.zombieBlockBreakingAllowToolRequiredBlocks, value), "Allow tool-required blocks"))
                .then(CommandUtil.toggleSetting("lights", Config.COMMON.zombieBlockBreakingAllowLightBlocks::get,
                        value -> Config.set(Config.COMMON.zombieBlockBreakingAllowLightBlocks, value), "Allow light-emitting blocks")));
    }

    private static int setEnabled(CommandSourceStack source, boolean enabled) {
        if (enabled) {
            FeaturePresets.enableBlockBreaking();
            CommandUtil.feedback(source,
                    "Zombie block breaking: ON\nBalanced preset loaded and active immediately. Containers, machines, lights, and tool-required blocks stay protected.",
                    true);
        } else {
            Config.set(Config.COMMON.enableZombieBlockBreaking, false);
            CommandUtil.feedback(source, "Zombie block breaking: OFF", true);
        }
        return 1;
    }

    private static void setEnabledValue(boolean enabled) {
        if (enabled) {
            FeaturePresets.enableBlockBreaking();
        } else {
            Config.set(Config.COMMON.enableZombieBlockBreaking, false);
        }
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
        status.append("Interval: ").append(CommandUtil.ticks(Config.COMMON.zombieBlockBreakingInterval.get())).append('\n');
        status.append("Chance: ").append(CommandUtil.percent(Config.COMMON.zombieBlockBreakingChance.get())).append('\n');
        status.append("Range: ")
                .append(CommandUtil.count(Config.COMMON.zombieBlockBreakingRange.get(), "block")).append('\n');
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
