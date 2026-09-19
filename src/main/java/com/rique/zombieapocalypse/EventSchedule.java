package com.rique.zombieapocalypse;

/**
 * Pure scheduling rules for horde and blood moon timing.
 */
public final class EventSchedule {

    private static final long HORDE_ROLL_WINDOW_TICKS = 100L;
    private static final long DUSK_ROLL_START_TICKS = 12000L;
    private static final long DUSK_ROLL_WINDOW_TICKS = 500L;

    private EventSchedule() {
    }

    public static boolean isNight(long dayTime) {
        return dayTime >= 13000L && dayTime < 23000L;
    }

    public static boolean shouldRollHorde(long currentDay, long dayTime, long lastRolledDay, int intervalDays, boolean duskMode) {
        if (currentDay <= 0 || intervalDays <= 0) {
            return false;
        }

        if (lastRolledDay == currentDay) {
            return false;
        }

        if (duskMode ? !isHordeDuskRollWindow(dayTime) : dayTime >= HORDE_ROLL_WINDOW_TICKS) {
            return false;
        }

        return currentDay % intervalDays == 0;
    }

    public static boolean isHordeRollWindow(long dayTime) {
        return dayTime < HORDE_ROLL_WINDOW_TICKS;
    }

    public static boolean isHordeDuskRollWindow(long dayTime) {
        return dayTime >= DUSK_ROLL_START_TICKS && dayTime < DUSK_ROLL_START_TICKS + DUSK_ROLL_WINDOW_TICKS;
    }
}
