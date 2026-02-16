package com.rique.zombieapocalypse;

import com.rique.zombieapocalypse.commands.CommandRegistrar;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = ZombieApocalypseAddon.MODID)
public final class ModCommands {

    private ModCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandRegistrar.registerAll(event.getDispatcher());
    }
}
