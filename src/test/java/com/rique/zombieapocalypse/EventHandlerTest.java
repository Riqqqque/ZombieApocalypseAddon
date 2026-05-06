package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

class EventHandlerTest {

    @Test
    void computeSpawnQuotaRespectsRemainingCapacity() {
        assertEquals(5, EventHandler.computeSpawnQuota(10, 50, 5));
        assertEquals(1, EventHandler.computeSpawnQuota(49, 50, 5));
        assertEquals(0, EventHandler.computeSpawnQuota(50, 50, 5));
        assertEquals(0, EventHandler.computeSpawnQuota(55, 50, 5));
    }

    @Test
    void daylightSpawnBlockUsesConfiguredStartDay() {
        assertEquals(false, EventHandler.isDaylightSpawnBlocked(false, 0, 10, false));
        assertEquals(true, EventHandler.isDaylightSpawnBlocked(true, 0, 10, false));
        assertEquals(true, EventHandler.isDaylightSpawnBlocked(true, 9, 10, false));
        assertEquals(false, EventHandler.isDaylightSpawnBlocked(true, 10, 10, false));
        assertEquals(false, EventHandler.isDaylightSpawnBlocked(true, 0, 0, false));
        assertEquals(false, EventHandler.isDaylightSpawnBlocked(true, 5, 10, true));
    }

    @Test
    void spawnDistanceIsOnlyImpossibleOutsideTheSpawnSquare() {
        assertEquals(false, EventHandler.isSpawnDistanceImpossible(12, 30));
        assertEquals(false, EventHandler.isSpawnDistanceImpossible(30, 30));
        assertEquals(false, EventHandler.isSpawnDistanceImpossible(42, 30));
        assertEquals(true, EventHandler.isSpawnDistanceImpossible(43, 30));
    }

    @Test
    void minimumSpawnDistanceUsesHorizontalDistanceOnly() {
        assertEquals(25L, EventHandler.horizontalDistanceSquared(new BlockPos(3, 90, 4), new BlockPos(0, 20, 0)));
    }

    @Test
    void blockLightLimitCanDisableOrCapCustomSpawns() {
        assertTrue(EventHandler.isBlockLightSpawnAllowed(15, -1));
        assertTrue(EventHandler.isBlockLightSpawnAllowed(0, 0));
        assertFalse(EventHandler.isBlockLightSpawnAllowed(1, 0));
        assertTrue(EventHandler.isBlockLightSpawnAllowed(7, 7));
        assertFalse(EventHandler.isBlockLightSpawnAllowed(8, 7));
        assertTrue(EventHandler.isBlockLightSpawnAllowed(20, 15));
        assertTrue(EventHandler.isBlockLightSpawnAllowed(-5, 0));
    }

    @Test
    void babyZombieChanceZeroForcesAdultsOnly() {
        assertTrue(EventHandler.shouldForceAdultZombie(true, 0.0));
        assertTrue(EventHandler.shouldForceAdultZombie(true, -1.0));
        assertFalse(EventHandler.shouldForceAdultZombie(true, 0.01));
        assertFalse(EventHandler.shouldForceAdultZombie(false, 0.0));
    }

    @Test
    void sunFireCancelOnlyAppliesToVanillaOnFireTicks() {
        assertTrue(EventHandler.shouldCancelSunFireDamage(true, true, false));
        assertFalse(EventHandler.shouldCancelSunFireDamage(false, true, false));
        assertFalse(EventHandler.shouldCancelSunFireDamage(true, false, false));
        assertFalse(EventHandler.shouldCancelSunFireDamage(true, true, true));
    }
}
