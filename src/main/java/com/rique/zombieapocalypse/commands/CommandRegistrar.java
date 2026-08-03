package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;

public final class CommandRegistrar {

    private CommandRegistrar() {
    }

    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher) {
        MainCommands.register(dispatcher);
        HelpCommands.register(dispatcher);
        CompatibilityCommands.register(dispatcher);
        UtilityCommands.register(dispatcher);
        WorldCommands.register(dispatcher);
        HordeCommands.register(dispatcher);
        StatsCommands.register(dispatcher);
        ScalingCommands.register(dispatcher);
        DaySpawnCommands.register(dispatcher);
        BlockBreakCommands.register(dispatcher);
        BlockPlaceCommands.register(dispatcher);
        TowerCommands.register(dispatcher);
        AttributeCommands.register(dispatcher);
    }
}
