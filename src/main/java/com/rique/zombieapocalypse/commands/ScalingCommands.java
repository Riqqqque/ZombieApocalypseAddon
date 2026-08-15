package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;

import com.rique.zombieapocalypse.DifficultyManager;

public final class ScalingCommands {

    private ScalingCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zscaling")
                .executes(context -> showStatus(context.getSource()))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource()))));
    }

    private static int showStatus(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        CommandUtil.feedback(source, DifficultyManager.getScalingStatus(level), false);
        return 1;
    }
}
