package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class ApocalypseWorldDataTest {

    @Test
    void resetEventScheduleStateClearsActiveAndPendingEventFlags() {
        ApocalypseWorldData data = new ApocalypseWorldData();

        data.setHordeActive(true);
        data.setHordeEndGameTime(1200L);
        data.setLastHordeRollDay(12L);
        data.setBloodMoonActive(true);
        data.setBloodMoonNightDay(12L);
        data.setForcedBloodMoonPending(true);
        data.setLastDayAnnouncementDay(12L);

        data.resetEventScheduleState();

        assertFalse(data.isHordeActive());
        assertEquals(0L, data.getHordeEndGameTime());
        assertEquals(-1L, data.getLastHordeRollDay());
        assertFalse(data.isBloodMoonActive());
        assertEquals(-1L, data.getBloodMoonNightDay());
        assertFalse(data.isForcedBloodMoonPending());
        assertEquals(-1L, data.getLastDayAnnouncementDay());
    }
}
