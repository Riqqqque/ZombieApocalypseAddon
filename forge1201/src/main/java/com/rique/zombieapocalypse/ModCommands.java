package com.rique.zombieapocalypse;

import com.rique.zombieapocalypse.commands.CommandRegistrar;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ZombieApocalypseAddon.MODID)
public final class ModCommands {

    private ModCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandRegistrar.registerAll(event.getDispatcher());
    }
}
