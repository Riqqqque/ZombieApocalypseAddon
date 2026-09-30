package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ZombieGearTest {

    @Test
    void equipmentCompatibilityPreservesOccupiedSlotsByDefault() {
        assertTrue(ZombieGear.shouldEquipSlot(true, true));
        assertFalse(ZombieGear.shouldEquipSlot(false, true));
        assertTrue(ZombieGear.shouldEquipSlot(false, false));
    }

    @Test
    void armorTiersKeepTheOriginalLeatherToDiamondThresholds() {
        assertEquals(1, ZombieGear.armorTier(0.49, 0.0));
        assertEquals(2, ZombieGear.armorTier(0.51, 0.0));
        assertEquals(2, ZombieGear.armorTier(0.69, 0.0));
        assertEquals(3, ZombieGear.armorTier(0.71, 0.0));
        assertEquals(3, ZombieGear.armorTier(0.89, 0.0));
        assertEquals(4, ZombieGear.armorTier(0.91, 0.0));

        assertEquals(1, ZombieGear.armorTier(0.19, 1.0));
        assertEquals(2, ZombieGear.armorTier(0.21, 1.0));
        assertEquals(3, ZombieGear.armorTier(0.41, 1.0));
        assertEquals(3, ZombieGear.armorTier(0.69, 1.0));
        assertEquals(4, ZombieGear.armorTier(0.71, 1.0));
    }

    @Test
    void weaponTiersKeepTheOriginalWoodenToDiamondThresholds() {
        assertEquals(1, ZombieGear.weaponTier(0.39, 0.0));
        assertEquals(2, ZombieGear.weaponTier(0.41, 0.0));
        assertEquals(3, ZombieGear.weaponTier(0.66, 0.0));
        assertEquals(4, ZombieGear.weaponTier(0.86, 0.0));

        assertEquals(1, ZombieGear.weaponTier(0.19, 1.0));
        assertEquals(2, ZombieGear.weaponTier(0.21, 1.0));
        assertEquals(3, ZombieGear.weaponTier(0.46, 1.0));
        assertEquals(4, ZombieGear.weaponTier(0.76, 1.0));
    }

    @Test
    void tierOddsMatchTheRollThresholdsAndSumToOne() {
        for (double factor : new double[] { 0.0, 0.25, 0.5, 1.0 }) {
            assertOdds(ZombieGear.armorTierOdds(factor));
            assertOdds(ZombieGear.weaponTierOdds(factor));
        }
        assertEquals("50%/20%/20%/10%", ZombieGear.describeOdds(ZombieGear.armorTierOdds(0.0)));
        assertEquals("20%/20%/30%/30%", ZombieGear.describeOdds(ZombieGear.armorTierOdds(1.0)));
        assertEquals("40%/25%/20%/15%", ZombieGear.describeOdds(ZombieGear.weaponTierOdds(0.0)));
        assertEquals("20%/25%/30%/25%", ZombieGear.describeOdds(ZombieGear.weaponTierOdds(1.0)));
    }

    @Test
    void chancesFormatWithoutNeedlessDecimals() {
        assertEquals("8.5%", ZombieGear.formatChance(0.085));
        assertEquals("25%", ZombieGear.formatChance(0.25));
        assertEquals("0%", ZombieGear.formatChance(0.0));
        assertEquals("100%", ZombieGear.formatChance(1.0));
    }

    @Test
    void listParserTrimsLowercasesAndKeepsDuplicatesAsWeights() {
        assertEquals(List.of("minecraft:iron_helmet", "magistuarmory:armet", "magistuarmory:armet"),
                ZombieGear.parseList(" minecraft:iron_helmet ,Magistuarmory:Armet,, magistuarmory:armet "));
        assertTrue(ZombieGear.parseList(null).isEmpty());
        assertTrue(ZombieGear.parseList("  ,  ").isEmpty());
    }

    @Test
    void idsNormalizeToTheirFullNamespace() {
        assertEquals("minecraft:iron_sword", ZombieGear.normalizeId(" iron_sword "));
        assertEquals("example:knight_helmet", ZombieGear.normalizeId("Example:Knight_Helmet"));
        assertNull(ZombieGear.normalizeId("not valid"));
        assertNull(ZombieGear.normalizeId(" "));
        assertNull(ZombieGear.normalizeId(null));
    }

    @Test
    void addingAndRemovingEntriesRewritesTheListCleanly() {
        String raw = "minecraft:iron_helmet, iron_sword,example:mace";

        String added = ZombieGear.withAdded(raw, "minecraft:iron_sword");
        assertEquals("minecraft:iron_helmet, iron_sword, example:mace, minecraft:iron_sword", added);
        assertEquals(2, ZombieGear.countEntries(added, "minecraft:iron_sword"));

        String removed = ZombieGear.withRemoved(added, "minecraft:iron_sword");
        assertEquals("minecraft:iron_helmet, example:mace", removed);
        assertEquals(0, ZombieGear.countEntries(removed, "minecraft:iron_sword"));

        assertEquals("example:mace", ZombieGear.withAdded("", "example:mace"));
        assertEquals("", ZombieGear.withRemoved("example:mace", "example:mace"));
    }

    @Test
    void defaultListsNameOneItemPerSlotForEveryTier() {
        for (int tier = 1; tier <= ZombieGear.TIER_COUNT; tier++) {
            List<String> entries = ZombieGear.parseList(ZombieGear.defaultList(tier));
            assertEquals(5, entries.size(), "tier " + tier);
            for (String entry : entries) {
                assertEquals(entry, ZombieGear.normalizeId(entry), "tier " + tier);
            }
        }
    }

    private static void assertOdds(double[] odds) {
        assertEquals(4, odds.length);
        double sum = 0.0;
        for (double chance : odds) {
            assertTrue(chance >= 0.0 && chance <= 1.0);
            sum += chance;
        }
        assertEquals(1.0, sum, 1.0e-9);
    }
}
