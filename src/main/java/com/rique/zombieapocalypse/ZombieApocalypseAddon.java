package com.rique.zombieapocalypse;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

@Mod(ZombieApocalypseAddon.MODID)
public class ZombieApocalypseAddon {
    public static final String MODID = "zombieapocalypseaddon";

    public ZombieApocalypseAddon(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::onConfigLoading);
        modEventBus.addListener(this::onConfigReloading);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
    }

    private void onConfigLoading(ModConfigEvent.Loading event) {
        Config.bind(event.getConfig());
        if (event.getConfig().getSpec() == Config.COMMON_SPEC) {
            ConfigGuard.loaded(event.getConfig().getFullPath());
        }
    }

    private void onConfigReloading(ModConfigEvent.Reloading event) {
        Config.reloaded(event.getConfig());
        if (event.getConfig().getSpec() == Config.COMMON_SPEC) {
            ConfigGuard.reloaded(event.getConfig().getFullPath());
        }
    }
}
