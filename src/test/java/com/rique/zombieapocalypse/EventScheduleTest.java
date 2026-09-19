package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EventScheduleTest {

    @Test
    void shouldRollHordeRequiresScheduledDayAndWindow() {
        assertTrue(EventSchedule.shouldRollHorde(5, 10, 4, 5, false));
        assertFalse(EventSchedule.shouldRollHorde(6, 10, 5, 5, false));
        assertFalse(EventSchedule.shouldRollHorde(5, 250, 4, 5, false));
        assertFalse(EventSchedule.shouldRollHorde(5, 10, 5, 5, false));
        assertFalse(EventSchedule.shouldRollHorde(0, 10, -1, 5, false));
    }

    @Test
    void shouldRollHordeUsesDuskWindowInDuskMode() {
        assertTrue(EventSchedule.shouldRollHorde(5, 12000, 4, 5, true));
        assertTrue(EventSchedule.shouldRollHorde(5, 12499, 4, 5, true));
        assertFalse(EventSchedule.shouldRollHorde(5, 10, 4, 5, true));
        assertFalse(EventSchedule.shouldRollHorde(5, 11999, 4, 5, true));
        assertFalse(EventSchedule.shouldRollHorde(5, 12500, 4, 5, true));
        assertFalse(EventSchedule.shouldRollHorde(5, 12000, 5, 5, true));
        assertFalse(EventSchedule.shouldRollHorde(6, 12000, 5, 5, true));
    }

    @Test
    void nightWindowMatchesExpectedRange() {
        assertFalse(EventSchedule.isNight(0));
        assertTrue(EventSchedule.isNight(13000));
        assertTrue(EventSchedule.isNight(22999));
        assertFalse(EventSchedule.isNight(23000));
    }

    @Test
    void duskRollWindowEndsBeforeNightfall() {
        assertFalse(EventSchedule.isHordeDuskRollWindow(0));
        assertFalse(EventSchedule.isHordeDuskRollWindow(11999));
        assertTrue(EventSchedule.isHordeDuskRollWindow(12000));
        assertTrue(EventSchedule.isHordeDuskRollWindow(12499));
        assertFalse(EventSchedule.isHordeDuskRollWindow(12500));
        assertFalse(EventSchedule.isHordeDuskRollWindow(13000));
    }
}
