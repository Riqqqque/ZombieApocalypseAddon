package com.rique.zombieapocalypse;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(ZombieApocalypseAddon.MODID)
public class ZombieApocalypseAddon {
    public static final String MODID = "zombieapocalypseaddon";

    @SuppressWarnings({"deprecation", "removal"})
    public ZombieApocalypseAddon() {
        // This is the config-registration API shared by Forge and NeoForge 1.20.1.
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
    }
}
