package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.ConfigLimits;
import com.rique.zombieapocalypse.DifficultyManager;
import com.rique.zombieapocalypse.ZombieTowering;

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
                        .executes(context -> setEnabled(context.getSource(), true))))
                .then(CommandUtil.admin(Commands.literal("unlimited")
                        .executes(context -> enableUnlimited(context.getSource()))))
                .then(CommandUtil.toggleSetting("enabled", Config.COMMON.enableZombieTowering::get,
                        TowerCommands::setEnabledValue, "Zombie towering"))
                .then(CommandUtil.intSetting("startday", "day", 0, ConfigLimits.MAX_APOCALYPSE_DAY,
                        Config.COMMON.zombieToweringStartDay::get,
                        value -> Config.set(Config.COMMON.zombieToweringStartDay, value),
                        value -> value == 0
                                ? "Zombie towering can start immediately when enabled."
                                : "Zombie towering starts on day " + value + '.',
                        0, 1, 5, 10, 15, 30, 50, 100))
                .then(CommandUtil.intSetting("interval", "ticks", 1, 72000,
                        Config.COMMON.zombieToweringInterval::get,
                        value -> Config.set(Config.COMMON.zombieToweringInterval, value),
                        value -> "Zombie towering interval: " + CommandUtil.ticks(value),
                        1, 5, 10, 20, 40, 100, 200, 600))
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
                .then(CommandUtil.intSetting("stacksize", "zombies", 0, ConfigLimits.MAX_TOWER_STACK_SIZE,
                        Config.COMMON.zombieToweringMaxStackSize::get,
                        TowerCommands::setMaximumStackSize,
                        value -> "Maximum zombie tower size: " + (value == 0 ? "unlimited" : value),
                        0, 1, 2, 3, 4, 5, 8, 16, 32, 64, 128, 256, 1000))
                .then(CommandUtil.intSetting("maxperplayer", "towers", 0, ConfigLimits.MAX_TOWERS_PER_PLAYER,
                        Config.COMMON.zombieToweringMaxTowersPerPlayer::get,
                        TowerCommands::setMaximumTowersPerPlayer,
                        value -> "Maximum towers targeting each player: " + (value == 0 ? "unlimited" : value),
                        0, 1, 2, 3, 4, 5, 8, 10, 16, 32))
                .then(CommandUtil.toggleSetting("jumping", Config.COMMON.zombieToweringJumpingEnabled::get,
                        value -> Config.set(Config.COMMON.zombieToweringJumpingEnabled, value),
                        "Top-zombie jump attacks"))
                .then(CommandUtil.toggleSetting("dynamic", Config.COMMON.zombieToweringDynamicHeightEnabled::get,
                        value -> Config.set(Config.COMMON.zombieToweringDynamicHeightEnabled, value),
                        "Dynamic target-height limit"))
                .then(CommandUtil.intSetting("offset", "blocks", 0, ConfigLimits.MAX_TOWER_HEIGHT_OFFSET,
                        Config.COMMON.zombieToweringTargetHeightOffset::get,
                        value -> Config.set(Config.COMMON.zombieToweringTargetHeightOffset, value),
                        value -> "Dynamic tower height: target block Y + " + value,
                        0, 1, 2, 3, 4, 8, 16, 32, 64, 128))
                .then(CommandUtil.toggleSetting("smartdismount", Config.COMMON.zombieToweringSmartDismountEnabled::get,
                        value -> Config.set(Config.COMMON.zombieToweringSmartDismountEnabled, value),
                        "Smart gradual tower dismounting"))
                .then(CommandUtil.intSetting("jumpcooldown", "ticks", 1, 1200,
                        Config.COMMON.zombieToweringJumpCooldownTicks::get,
                        value -> Config.set(Config.COMMON.zombieToweringJumpCooldownTicks, value),
                        value -> "Delay between jumps from one tower: " + CommandUtil.ticks(value),
                        1, 5, 10, 20, 40, 100, 200, 600, 1200))
                .then(CommandUtil.doubleSetting("dismount", "blocks", 1.0, 8.0,
                        Config.COMMON.zombieToweringDismountDistance::get,
                        value -> Config.set(Config.COMMON.zombieToweringDismountDistance, value),
                        value -> "Tower dismount distance: " + CommandUtil.number(value) + " blocks",
                        1.0, 1.5, 2.0, 2.75, 3.0, 4.0, 6.0, 8.0))
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
                .then(CommandUtil.intSetting("height", "blocks", 0, ConfigLimits.MAX_TOWER_HEIGHT_LIMIT,
                        Config.COMMON.zombieToweringMaxHeightAboveTarget::get,
                        value -> Config.set(Config.COMMON.zombieToweringMaxHeightAboveTarget, value),
                        value -> value == 0
                                ? "Fallback tower height: unlimited"
                                : "Fallback tower height above target: " + value + " blocks",
                        0, 1, 2, 3, 4, 6, 8, 12, 16, 24, 32, 64, 128, 256, 1000))
                .then(CommandUtil.toggleSetting("obstacle", Config.COMMON.zombieToweringRequireObstacle::get,
                        value -> Config.set(Config.COMMON.zombieToweringRequireObstacle, value),
                        "Require obstacle or raised/covered target")));
    }

    private static int setEnabled(CommandSourceStack source, boolean enabled) {
        if (enabled) {
            FeaturePresets.enableTowering();
            CommandUtil.feedback(source,
                    "Zombie towering: ON\nSmart preset loaded: towers grow to the target's block Y + 1, then dismount safely when the target returns to reachable ground.",
                    true);
            return 1;
        }

        Config.set(Config.COMMON.enableZombieTowering, false);
        int released = ZombieTowering.releaseAll(source.getServer());
        CommandUtil.feedback(source,
                "Zombie towering: OFF\nReleased " + released + " loaded stack "
                        + (released == 1 ? "rider." : "riders."),
                true);
        return 1;
    }

    private static void setEnabledValue(CommandSourceStack source, boolean enabled) {
        if (enabled) {
            FeaturePresets.enableTowering();
        } else {
            Config.set(Config.COMMON.enableZombieTowering, false);
            ZombieTowering.releaseAll(source.getServer());
        }
    }

    private static int enableUnlimited(CommandSourceStack source) {
        FeaturePresets.enableUnlimitedTowering();
        CommandUtil.feedback(source,
                "Zombie towering: UNLIMITED\nCount and height caps are off. Smart dismounting and jump attacks are off. Very tall towers can affect server performance.",
                true);
        return 1;
    }

    private static void setMaximumStackSize(CommandSourceStack source, int value) {
        Config.set(Config.COMMON.zombieToweringMaxStackSize, value);
        int released = ZombieTowering.trimLoadedTowers(source.getServer(), value);
        if (released > 0) {
            CommandUtil.feedback(source,
                    "Trimmed " + released + " loaded tower " + (released == 1 ? "rider" : "riders")
                            + " to enforce the new stack size.",
                    true);
        }
    }

    private static void setMaximumTowersPerPlayer(CommandSourceStack source, int value) {
        Config.set(Config.COMMON.zombieToweringMaxTowersPerPlayer, value);
        int released = ZombieTowering.enforcePerPlayerLimit(source.getServer(), value);
        if (released > 0) {
            CommandUtil.feedback(source,
                    "Released " + released + " loaded tower " + (released == 1 ? "rider" : "riders")
                            + " to enforce the new per-player limit.",
                    true);
        }
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
        int maximumStackSize = Config.COMMON.zombieToweringMaxStackSize.get();
        status.append("Maximum stack size: ")
                .append(maximumStackSize == 0 ? "unlimited" : maximumStackSize + " zombies").append('\n');
        int maximumTowers = Config.COMMON.zombieToweringMaxTowersPerPlayer.get();
        status.append("Maximum towers per player: ")
                .append(maximumTowers == 0 ? "unlimited" : maximumTowers).append('\n');
        status.append("Loaded active towers: ").append(ZombieTowering.countLoadedTowers(source.getServer())).append('\n');
        status.append("Dynamic target height: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieToweringDynamicHeightEnabled.get())).append('\n');
        status.append("Dynamic height target: player block Y + ")
                .append(Config.COMMON.zombieToweringTargetHeightOffset.get()).append('\n');
        status.append("Smart gradual dismount: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieToweringSmartDismountEnabled.get())).append('\n');
        status.append("Top zombies jump toward target: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieToweringJumpingEnabled.get())).append('\n');
        status.append("Delay between tower jumps: ")
                .append(CommandUtil.ticks(Config.COMMON.zombieToweringJumpCooldownTicks.get())).append('\n');
        status.append("Dismount distance: ").append(Config.COMMON.zombieToweringDismountDistance.get()).append(" blocks\n");
        status.append("Dismount vertical boost: ").append(Config.COMMON.zombieToweringVerticalBoost.get()).append('\n');
        status.append("Dismount forward boost: ").append(Config.COMMON.zombieToweringForwardBoost.get()).append('\n');
        int fallbackHeight = Config.COMMON.zombieToweringMaxHeightAboveTarget.get();
        status.append("Fallback height above target: ")
                .append(fallbackHeight == 0 ? "unlimited" : fallbackHeight + " blocks").append('\n');
        status.append("Require obstacle/raised target: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieToweringRequireObstacle.get()));
        CommandUtil.feedback(source, status.toString(), false);
        return 1;
    }
}
