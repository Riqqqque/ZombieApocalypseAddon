package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;

import com.rique.zombieapocalypse.Config;
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
                        .executes(context -> showStatus(context.getSource()))));
    }

    private static int setEnabled(CommandSourceStack source, boolean enabled) {
        if (enabled) {
            FeaturePresets.enableScaling();
            CommandUtil.feedback(source,
                    "Difficulty scaling: ON\nBalanced day 3-50 progression preset loaded.",
                    true);
        } else {
            Config.set(Config.COMMON.enableDifficultyScaling, false);
            CommandUtil.feedback(source, "Difficulty scaling: OFF", true);
        }
        return 1;
    }

    private static int showStatus(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        CommandUtil.feedback(source, DifficultyManager.getScalingStatus(level), false);
        return 1;
    }
}
