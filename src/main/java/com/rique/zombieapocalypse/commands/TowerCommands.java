package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.DifficultyManager;

public final class TowerCommands {

    private TowerCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ztower")
                .executes(context -> showStatus(context.getSource()))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource())))
                .then(CommandUtil.admin(Commands.literal("on")
                        .executes(context -> setEnabled(context.getSource(), true))))
                .then(CommandUtil.admin(Commands.literal("off")
                        .executes(context -> setEnabled(context.getSource(), false))))
                .then(CommandUtil.admin(Commands.literal("dayone")
                        .executes(context -> {
                            Config.edit(() -> {
                                Config.set(Config.COMMON.enableZombieTowering, true);
                                Config.set(Config.COMMON.zombieToweringStartDay, 0);
                            });
                            CommandUtil.feedback(context.getSource(),
                                    "Zombie towering enabled and set to start immediately.", true);
                            return 1;
                        })))
                .then(CommandUtil.toggleSetting("enabled", Config.COMMON.enableZombieTowering::get,
                        value -> Config.set(Config.COMMON.enableZombieTowering, value), "Zombie towering"))
                .then(CommandUtil.intSetting("startday", "day", 0, 3650,
                        Config.COMMON.zombieToweringStartDay::get,
                        value -> Config.set(Config.COMMON.zombieToweringStartDay, value),
                        value -> value == 0
                                ? "Zombie towering can start immediately when enabled."
                                : "Zombie towering starts on day " + value + '.',
                        0, 1, 5, 10, 15, 30, 50, 100))
                .then(CommandUtil.intSetting("interval", "ticks", 5, 72000,
                        Config.COMMON.zombieToweringInterval::get,
                        value -> Config.set(Config.COMMON.zombieToweringInterval, value),
                        value -> "Zombie towering interval: " + CommandUtil.ticks(value),
                        5, 10, 20, 40, 100, 200, 600))
                .then(CommandUtil.doubleSetting("chance", "chance", 0.0, 1.0,
                        Config.COMMON.zombieToweringChance::get,
                        value -> Config.set(Config.COMMON.zombieToweringChance, value),
                        value -> "Zombie towering chance: " + CommandUtil.percent(value),
                        0.0, 0.1, 0.25, 0.5, 0.75, 1.0))
                .then(CommandUtil.intSetting("distance", "blocks", 4, 128,
                        Config.COMMON.zombieToweringMaxTargetDistance::get,
                        value -> Config.set(Config.COMMON.zombieToweringMaxTargetDistance, value),
                        value -> "Zombie towering target distance: " + value + " blocks",
                        4, 8, 16, 24, 32, 64, 128))
                .then(CommandUtil.intSetting("crowd", "zombies", 1, 16,
                        Config.COMMON.zombieToweringMinNearbyZombies::get,
                        value -> Config.set(Config.COMMON.zombieToweringMinNearbyZombies, value),
                        value -> "Nearby zombies required for towering: " + value,
                        1, 2, 3, 4, 6, 8, 12, 16))
                .then(CommandUtil.doubleSetting("radius", "blocks", 0.75, 6.0,
                        Config.COMMON.zombieToweringCrowdRadius::get,
                        value -> Config.set(Config.COMMON.zombieToweringCrowdRadius, value),
                        value -> "Zombie towering crowd radius: " + CommandUtil.number(value) + " blocks",
                        0.75, 1.0, 1.5, 2.0, 3.0, 4.0, 6.0))
                .then(CommandUtil.doubleSetting("vertical", "velocity", 0.1, 1.0,
                        Config.COMMON.zombieToweringVerticalBoost::get,
                        value -> Config.set(Config.COMMON.zombieToweringVerticalBoost, value),
                        value -> "Zombie towering vertical boost: " + CommandUtil.number(value),
                        0.1, 0.2, 0.3, 0.4, 0.6, 0.8, 1.0))
                .then(CommandUtil.doubleSetting("forward", "velocity", 0.0, 0.6,
                        Config.COMMON.zombieToweringForwardBoost::get,
                        value -> Config.set(Config.COMMON.zombieToweringForwardBoost, value),
                        value -> "Zombie towering forward boost: " + CommandUtil.number(value),
                        0.0, 0.1, 0.2, 0.3, 0.4, 0.6))
                .then(CommandUtil.intSetting("height", "blocks", 1, 32,
                        Config.COMMON.zombieToweringMaxHeightAboveTarget::get,
                        value -> Config.set(Config.COMMON.zombieToweringMaxHeightAboveTarget, value),
                        value -> "Zombie towering height above target: " + value + " blocks",
                        1, 2, 3, 4, 6, 8, 12, 16, 24, 32))
                .then(CommandUtil.toggleSetting("obstacle", Config.COMMON.zombieToweringRequireObstacle::get,
                        value -> Config.set(Config.COMMON.zombieToweringRequireObstacle, value),
                        "Require obstacle or raised/covered target")));
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
