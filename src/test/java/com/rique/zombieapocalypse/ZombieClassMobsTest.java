package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class ZombieClassMobsTest {

    @Test
    void includesConfiguredZombieVariantsOnly() {
        assertTrue(ZombieClassMobs.isZombieClass(ResourceLocation.fromNamespaceAndPath("minecraft", "zombie")));
        assertTrue(ZombieClassMobs.isZombieClass(ResourceLocation.fromNamespaceAndPath("minecraft", "husk")));
        assertTrue(ZombieClassMobs.isZombieClass(ResourceLocation.fromNamespaceAndPath("minecraft", "drowned")));
        assertTrue(ZombieClassMobs.isZombieClass(ResourceLocation.fromNamespaceAndPath("minecraft", "zombie_villager")));

        assertFalse(ZombieClassMobs.isZombieClass(ResourceLocation.fromNamespaceAndPath("minecraft", "zombified_piglin")));
        assertFalse(ZombieClassMobs.isZombieClass(ResourceLocation.fromNamespaceAndPath("minecraft", "skeleton")));
        assertFalse(ZombieClassMobs.isZombieClass(ResourceLocation.fromNamespaceAndPath("example", "zombie")));
    }
}
