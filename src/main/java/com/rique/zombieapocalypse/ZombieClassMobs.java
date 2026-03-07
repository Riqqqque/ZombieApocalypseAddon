package com.rique.zombieapocalypse;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;

public final class ZombieClassMobs {

    private ZombieClassMobs() {
    }

    public static boolean isZombieClass(Entity entity) {
        return isZombieClass(entity.getType());
    }

    public static boolean isZombieClass(EntityType<?> type) {
        return type == EntityType.ZOMBIE
                || type == EntityType.HUSK
                || type == EntityType.DROWNED
                || type == EntityType.ZOMBIE_VILLAGER;
    }

    static boolean isZombieClass(ResourceLocation typeId) {
        String namespace = typeId.getNamespace();
        String path = typeId.getPath();
        return "minecraft".equals(namespace)
                && ("zombie".equals(path)
                || "husk".equals(path)
                || "drowned".equals(path)
                || "zombie_villager".equals(path));
    }
}
