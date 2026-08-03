package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DifficultyManagerTest {

    @Test
    void equipmentCompatibilityPreservesOccupiedSlotsByDefault() {
        assertTrue(DifficultyManager.shouldEquipSlot(true, true));
        assertFalse(DifficultyManager.shouldEquipSlot(false, true));
        assertTrue(DifficultyManager.shouldEquipSlot(false, false));
    }

    @Test
    void spawnScalingRunsOnceWhenAnyScalingSystemIsEnabled() {
        assertTrue(DifficultyManager.shouldApplyScaling(false, true, false));
        assertTrue(DifficultyManager.shouldApplyScaling(false, false, true));
        assertTrue(DifficultyManager.shouldApplyScaling(false, true, true));

        assertFalse(DifficultyManager.shouldApplyScaling(true, true, true));
        assertFalse(DifficultyManager.shouldApplyScaling(false, false, false));
    }
}
