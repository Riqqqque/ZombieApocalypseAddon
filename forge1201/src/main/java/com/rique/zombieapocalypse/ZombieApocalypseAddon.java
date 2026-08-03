package com.rique.zombieapocalypse;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ZombieApocalypseAddon.MODID)
public class ZombieApocalypseAddon {
    public static final String MODID = "zombieapocalypseaddon";

    @SuppressWarnings({"deprecation", "removal"})
    public ZombieApocalypseAddon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::onConfigLoading);
        modEventBus.addListener(this::onConfigReloading);
        // This is the config-registration API shared by Forge and NeoForge 1.20.1.
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
    }

    private void onConfigLoading(ModConfigEvent.Loading event) {
        Config.bind(event.getConfig());
    }

    private void onConfigReloading(ModConfigEvent.Reloading event) {
        Config.bind(event.getConfig());
    }
}
