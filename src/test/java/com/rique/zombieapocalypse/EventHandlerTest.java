package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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
}
