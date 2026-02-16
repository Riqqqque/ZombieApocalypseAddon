package com.rique.zombieapocalypse;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(ZombieApocalypseAddon.MODID)
public class ZombieApocalypseAddon {
    public static final String MODID = "zombieapocalypseaddon";

    public ZombieApocalypseAddon(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
    }
}
