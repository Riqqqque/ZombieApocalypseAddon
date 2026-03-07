package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HordeManagerTest {

    @Test
    void scheduledHordesRespectGracePeriod() {
        assertTrue(HordeManager.isScheduledHordeBlockedByGrace(0, 10));
        assertTrue(HordeManager.isScheduledHordeBlockedByGrace(9, 10));
        assertFalse(HordeManager.isScheduledHordeBlockedByGrace(10, 10));
        assertFalse(HordeManager.isScheduledHordeBlockedByGrace(0, 0));
    }
}
