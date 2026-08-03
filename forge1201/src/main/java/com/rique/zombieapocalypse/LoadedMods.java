package com.rique.zombieapocalypse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraftforge.fml.ModList;

public final class LoadedMods {

    private static final Map<String, Boolean> CACHE = new ConcurrentHashMap<>();

    private LoadedMods() {
    }

    public static boolean isLoaded(String modId) {
        return CACHE.computeIfAbsent(modId, id -> ModList.get().isLoaded(id));
    }
}
