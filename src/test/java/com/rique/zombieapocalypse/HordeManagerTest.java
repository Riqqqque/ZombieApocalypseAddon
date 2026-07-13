package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void dayAnnouncementsOnlyFireOnceDuringMorningWindow() {
        assertTrue(HordeManager.shouldAnnounceDay(10, 0, -1, true));
        assertTrue(HordeManager.shouldAnnounceDay(10, 99, 9, true));
        assertFalse(HordeManager.shouldAnnounceDay(10, 100, -1, true));
        assertFalse(HordeManager.shouldAnnounceDay(10, 50, 10, true));
        assertFalse(HordeManager.shouldAnnounceDay(10, 50, -1, false));
    }

    @Test
    void hordeIncomingSubtitleOnlyIncludesDayWhenItConsumesMorningAnnouncement() {
        assertEquals(
                "Day 10 | Zombie waves for 5 minutes.",
                HordeManager.buildHordeIncomingSubtitle(5, 10, true));
        assertEquals(
                "Zombie waves for 5 minutes.",
                HordeManager.buildHordeIncomingSubtitle(5, 10, false));
    }

    @Test
    void bloodMoonEndNotifiesAtNormalSunriseButCanCombineAfterSleep() {
        assertTrue(HordeManager.shouldNotifyBloodMoonEndImmediately(23000L));
        assertFalse(HordeManager.shouldNotifyBloodMoonEndImmediately(0L));
    }

    @Test
    void activeHordeTimeRoundsUpUntilTheFinalTick() {
        assertEquals(5L, HordeManager.remainingSeconds(200L, 100L));
        assertEquals(1L, HordeManager.remainingSeconds(200L, 199L));
        assertEquals(0L, HordeManager.remainingSeconds(200L, 200L));
    }
}
