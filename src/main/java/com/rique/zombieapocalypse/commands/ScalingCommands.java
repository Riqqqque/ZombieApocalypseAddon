package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.ConfigLimits;
import com.rique.zombieapocalypse.DifficultyManager;

public final class ScalingCommands {

    private ScalingCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zscaling")
                .executes(context -> showStatus(context.getSource()))
                .then(CommandUtil.admin(Commands.literal("on")
                        .executes(context -> setEnabled(context.getSource(), true))))
                .then(CommandUtil.admin(Commands.literal("off")
                        .executes(context -> setEnabled(context.getSource(), false))))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource())))
                .then(CommandUtil.switchSetting("enabled", Config.COMMON.enableDifficultyScaling::get,
                        ScalingCommands::setEnabled, "Difficulty scaling"))
                .then(CommandUtil.intSetting("startday", "day", 0, ConfigLimits.MAX_APOCALYPSE_DAY,
                        Config.COMMON.scalingStartDay::get,
                        value -> Config.set(Config.COMMON.scalingStartDay, value),
                        value -> "Difficulty scaling starts on day " + value + '.',
                        0, 1, 3, 5, 10, 15, 30, 50, 100, 365, 1000))
                .then(CommandUtil.intSetting("maxday", "day", 1, ConfigLimits.MAX_APOCALYPSE_DAY,
                        Config.COMMON.maxScalingDay::get,
                        value -> Config.set(Config.COMMON.maxScalingDay, value),
                        value -> "Difficulty scaling reaches full strength on day " + value + '.',
                        1, 10, 25, 50, 100, 365, 1000))
                .then(CommandUtil.intSetting("steps", "days", 0, ConfigLimits.MAX_APOCALYPSE_DAY,
                        Config.COMMON.scalingStepDays::get,
                        value -> Config.set(Config.COMMON.scalingStepDays, value),
                        value -> value <= 0
                                ? "Difficulty scaling rises a little every day."
                                : "Difficulty scaling rises once every " + value
                                        + " days, on the morning after each step (day " + (value + 1) + ", "
                                        + (2 * value + 1) + ", ...).",
                        0, 5, 7, 10, 15, 30))
                .then(CommandUtil.intSetting("extrazombies", "count", 0, 50,
                        Config.COMMON.maxExtraZombiesPerWave::get,
                        value -> Config.set(Config.COMMON.maxExtraZombiesPerWave, value),
                        value -> value <= 0
                                ? "Difficulty scaling no longer changes wave size."
                                : "Every custom wave gets up to " + value + " extra zombies at full strength.",
                        0, 1, 2, 3, 5, 10))
                .then(CommandUtil.doubleSetting("speed", "boost", 0.0, 1.0,
                        Config.COMMON.maxSpeedBoost::get,
                        value -> Config.set(Config.COMMON.maxSpeedBoost, value),
                        value -> "Maximum legacy speed boost: " + CommandUtil.percent(value),
                        0.0, 0.1, 0.2, 0.3, 0.5, 0.75, 1.0))
                .then(CommandUtil.intSetting("health", "points", 0, 40,
                        Config.COMMON.maxHealthBoost::get,
                        value -> Config.set(Config.COMMON.maxHealthBoost, value),
                        value -> "Maximum legacy health bonus: " + value + " health points",
                        0, 2, 4, 10, 20, 30, 40))
                .then(CommandUtil.doubleSetting("armorchance", "chance", 0.0, 1.0,
                        Config.COMMON.maxArmorChance::get,
                        value -> Config.set(Config.COMMON.maxArmorChance, value),
                        value -> "Maximum armor chance: " + CommandUtil.percent(value),
                        0.0, 0.1, 0.25, 0.3, 0.5, 0.75, 1.0))
                .then(CommandUtil.doubleSetting("weaponchance", "chance", 0.0, 1.0,
                        Config.COMMON.maxWeaponChance::get,
                        value -> Config.set(Config.COMMON.maxWeaponChance, value),
                        value -> "Maximum weapon chance: " + CommandUtil.percent(value),
                        0.0, 0.1, 0.2, 0.25, 0.5, 0.75, 1.0)));
    }

    private static int setEnabled(CommandSourceStack source, boolean enabled) {
        if (!enabled) {
            return CommandUtil.turnOff(source, "Difficulty scaling",
                    () -> Config.set(Config.COMMON.enableDifficultyScaling, false), null);
        }
        return CommandUtil.turnOn(source, "Difficulty scaling", "scaling",
                FeaturePresets::enableScaling,
                () -> "Progression: day " + Config.COMMON.scalingStartDay.get() + " to day "
                        + Config.COMMON.maxScalingDay.get() + " (today is day "
                        + DifficultyManager.getCurrentDay(source.getLevel()) + ").");
    }

    private static int showStatus(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        CommandUtil.feedback(source, DifficultyManager.getScalingStatus(level), false);
        return 1;
    }
}
