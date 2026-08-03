package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

class ZombieCompatibilityTest {

    @Test
    void entityIdListParserTrimsDeduplicatesAndRejectsInvalidIds() {
        Set<ResourceLocation> ids = ZombieCompatibility.parseEntityTypeIds(
                " example:runner, minecraft:zombie,example:runner,not valid, ,example:brute ");

        assertEquals(Set.of(
                ResourceLocation.fromNamespaceAndPath("example", "runner"),
                ResourceLocation.fromNamespaceAndPath("minecraft", "zombie"),
                ResourceLocation.fromNamespaceAndPath("example", "brute")), ids);
    }

    @Test
    void entityIdListParserHandlesEmptyValues() {
        assertTrue(ZombieCompatibility.parseEntityTypeIds(null).isEmpty());
        assertTrue(ZombieCompatibility.parseEntityTypeIds("").isEmpty());
        assertTrue(ZombieCompatibility.parseEntityTypeIds(" , ").isEmpty());
    }

    @Test
    void addonAiHonorsModdedAndExternalOwnershipToggles() {
        assertTrue(ZombieCompatibility.shouldUseAddonAi(true, true, false, true, false));
        assertTrue(ZombieCompatibility.shouldUseAddonAi(true, false, true, true, false));
        assertFalse(ZombieCompatibility.shouldUseAddonAi(true, false, false, true, false));
        assertFalse(ZombieCompatibility.shouldUseAddonAi(true, false, true, true, true));
        assertTrue(ZombieCompatibility.shouldUseAddonAi(true, false, true, false, true));
        assertFalse(ZombieCompatibility.shouldUseAddonAi(false, true, true, false, false));
    }
}
