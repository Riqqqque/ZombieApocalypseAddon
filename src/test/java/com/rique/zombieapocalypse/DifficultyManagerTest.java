package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DifficultyManagerTest {

    @Test
    void spawnScalingRunsOnceWhenAnyScalingSystemIsEnabled() {
        assertTrue(DifficultyManager.shouldApplyScaling(false, true, false));
        assertTrue(DifficultyManager.shouldApplyScaling(false, false, true));
        assertTrue(DifficultyManager.shouldApplyScaling(false, true, true));

        assertFalse(DifficultyManager.shouldApplyScaling(true, true, true));
        assertFalse(DifficultyManager.shouldApplyScaling(false, false, false));
    }

    @Test
    void endlessRatesReadAsPlainPercents() {
        assertEquals("1%", DifficultyManager.formatPercentNumber(1.0));
        assertEquals("0.5%", DifficultyManager.formatPercentNumber(0.5));
        assertEquals("0.25%", DifficultyManager.formatPercentNumber(0.25));
        assertEquals("100%", DifficultyManager.formatPercentNumber(100.0));
    }
}
