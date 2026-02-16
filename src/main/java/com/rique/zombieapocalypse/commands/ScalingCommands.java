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
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("status")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            CommandUtil.feedback(context.getSource(), DifficultyManager.getScalingStatus(level), false);
                            return 1;
                        })));
    }
}
