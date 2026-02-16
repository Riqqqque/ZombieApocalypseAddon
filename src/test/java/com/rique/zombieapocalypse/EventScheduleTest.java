package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EventScheduleTest {

    @Test
    void shouldRollHordeRequiresScheduledDayAndWindow() {
        assertTrue(EventSchedule.shouldRollHorde(5, 10, 4, 5));
        assertFalse(EventSchedule.shouldRollHorde(6, 10, 5, 5));
        assertFalse(EventSchedule.shouldRollHorde(5, 250, 4, 5));
        assertFalse(EventSchedule.shouldRollHorde(5, 10, 5, 5));
        assertFalse(EventSchedule.shouldRollHorde(0, 10, -1, 5));
    }

    @Test
    void nightWindowMatchesExpectedRange() {
        assertFalse(EventSchedule.isNight(0));
        assertTrue(EventSchedule.isNight(13000));
        assertTrue(EventSchedule.isNight(22999));
        assertFalse(EventSchedule.isNight(23000));
    }
}
