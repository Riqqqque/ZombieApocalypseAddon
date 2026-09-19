package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HordeManagerTest {

    @Test
    void scheduledHordesRespectNightOnlyModeAndGracePeriod() {
        assertTrue(HordeManager.isScheduledHordeBlocked(false, 10, 0, false));
        assertTrue(HordeManager.isScheduledHordeBlocked(true, 0, 10, false));
        assertTrue(HordeManager.isScheduledHordeBlocked(true, 9, 10, false));
        assertFalse(HordeManager.isScheduledHordeBlocked(true, 10, 10, false));
        assertFalse(HordeManager.isScheduledHordeBlocked(true, 0, 0, false));
    }

    @Test
    void duskHordesIgnoreNightOnlyModeButKeepGracePeriod() {
        assertFalse(HordeManager.isScheduledHordeBlocked(false, 10, 0, true));
        assertFalse(HordeManager.isScheduledHordeBlocked(false, 10, 10, true));
        assertTrue(HordeManager.isScheduledHordeBlocked(false, 9, 10, true));
        assertTrue(HordeManager.isScheduledHordeBlocked(true, 5, 10, true));
    }

    @Test
    void manualHordesAreOnlyBlockedByNightOnlyModeDuringRealDaytime() {
        assertTrue(HordeManager.isManualHordeBlockedByDaytime(true, true, false));
        assertFalse(HordeManager.isManualHordeBlockedByDaytime(true, false, false));
        assertFalse(HordeManager.isManualHordeBlockedByDaytime(true, true, true));
        assertFalse(HordeManager.isManualHordeBlockedByDaytime(false, true, false));
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
    void hordeEndingAtDawnConsumesThatDaysScheduledRoll() {
        assertTrue(HordeManager.shouldConsumeScheduledHordeRollAfterEnd(0L, false));
        assertTrue(HordeManager.shouldConsumeScheduledHordeRollAfterEnd(99L, false));
        assertFalse(HordeManager.shouldConsumeScheduledHordeRollAfterEnd(100L, false));
    }

    @Test
    void hordeEndingAtDuskConsumesThatDaysScheduledRollInDuskMode() {
        assertFalse(HordeManager.shouldConsumeScheduledHordeRollAfterEnd(50L, true));
        assertFalse(HordeManager.shouldConsumeScheduledHordeRollAfterEnd(11999L, true));
        assertTrue(HordeManager.shouldConsumeScheduledHordeRollAfterEnd(12000L, true));
        assertTrue(HordeManager.shouldConsumeScheduledHordeRollAfterEnd(12499L, true));
        assertFalse(HordeManager.shouldConsumeScheduledHordeRollAfterEnd(12500L, true));
    }

    @Test
    void simultaneousDawnEventsUseOneCombinedSubtitle() {
        assertEquals(
                "Day 10 | The blood moon fades and the zombie horde has dispersed.",
                HordeManager.buildEndedEventsSubtitle(true, true, true, 10L));
        assertEquals(
                "The zombie horde has dispersed.",
                HordeManager.buildEndedEventsSubtitle(true, false, false, 10L));
    }

    @Test
    void activeHordeTimeRoundsUpUntilTheFinalTick() {
        assertEquals(5L, HordeManager.remainingSeconds(200L, 100L));
        assertEquals(1L, HordeManager.remainingSeconds(200L, 199L));
        assertEquals(0L, HordeManager.remainingSeconds(200L, 200L));
    }

    @Test
    void simultaneousEventsUseTheLargerConfiguredEventWave() {
        assertEquals(2, HordeManager.selectEventWaveSize(false, false, 2, 5, 4));
        assertEquals(3, HordeManager.selectEventWaveSize(true, false, 8, 3, 20));
        assertEquals(4, HordeManager.selectEventWaveSize(false, true, 8, 20, 4));
        assertEquals(20, HordeManager.selectEventWaveSize(true, true, 8, 3, 20));
    }

    @Test
    void disablingCustomWavesClearsActiveAndQueuedSpawnEvents() {
        ApocalypseWorldData state = new ApocalypseWorldData();
        state.setHordeActive(true);
        state.setHordeEndGameTime(1200L);
        state.setBloodMoonActive(true);
        state.setForcedBloodMoonPending(true);

        HordeManager.stopSpawnEvents(state, 10L, 50L, false);

        assertFalse(state.isHordeActive());
        assertEquals(0L, state.getHordeEndGameTime());
        assertEquals(10L, state.getLastHordeRollDay());
        assertFalse(state.isBloodMoonActive());
        assertFalse(state.isForcedBloodMoonPending());
    }
}
