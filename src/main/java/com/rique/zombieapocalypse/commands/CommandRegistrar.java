package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;

public final class CommandRegistrar {

    private CommandRegistrar() {
    }

    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher) {
        HelpCommands.register(dispatcher);
        UtilityCommands.register(dispatcher);
        HordeCommands.register(dispatcher);
        StatsCommands.register(dispatcher);
        ScalingCommands.register(dispatcher);
        DaySpawnCommands.register(dispatcher);
    }
}
