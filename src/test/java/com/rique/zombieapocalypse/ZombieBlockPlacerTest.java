package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ZombieBlockPlacerTest {

    @Test
    void blockPlacingStartsOnlyWhenEnabledAndDayReached() {
        assertFalse(ZombieBlockPlacer.isBlockPlacingActive(false, 20, 15));
        assertFalse(ZombieBlockPlacer.isBlockPlacingActive(true, 14, 15));
        assertTrue(ZombieBlockPlacer.isBlockPlacingActive(true, 15, 15));
        assertTrue(ZombieBlockPlacer.isBlockPlacingActive(true, 0, -5));
    }

    @Test
    void placementBudgetSupportsLimitsAndIntentionalUnlimitedMode() {
        assertTrue(ZombieBlockPlacer.hasPlacementBudget(0, 8));
        assertTrue(ZombieBlockPlacer.hasPlacementBudget(7, 8));
        assertFalse(ZombieBlockPlacer.hasPlacementBudget(8, 8));
        assertTrue(ZombieBlockPlacer.hasPlacementBudget(1000, 0));
        assertFalse(ZombieBlockPlacer.hasPlacementBudget(Integer.MAX_VALUE, 0));
    }

    @Test
    void targetDistanceUsesSquaredRangeWithoutSquareRoots() {
        assertTrue(ZombieBlockPlacer.isTargetDistanceAllowed(1024.0, 32));
        assertFalse(ZombieBlockPlacer.isTargetDistanceAllowed(1024.01, 32));
        assertTrue(ZombieBlockPlacer.isTargetDistanceAllowed(1.0, 0));
    }

    @Test
    void destinationReplacementRulesKeepSolidBlocksProtected() {
        assertTrue(ZombieBlockPlacer.isDestinationReplaceable(true, false, false, false, false));
        assertFalse(ZombieBlockPlacer.isDestinationReplaceable(false, false, false, true, true));
        assertFalse(ZombieBlockPlacer.isDestinationReplaceable(false, true, true, false, true));
        assertTrue(ZombieBlockPlacer.isDestinationReplaceable(false, true, true, true, false));
        assertFalse(ZombieBlockPlacer.isDestinationReplaceable(false, false, true, true, false));
        assertTrue(ZombieBlockPlacer.isDestinationReplaceable(false, false, true, false, true));
    }

    @Test
    void obstacleRuleAllowsCollisionsCoveredTargetsAndHigherTargets() {
        assertTrue(ZombieBlockPlacer.shouldAttemptStep(false, false, false, false, false));
        assertTrue(ZombieBlockPlacer.shouldAttemptStep(true, true, false, false, false));
        assertTrue(ZombieBlockPlacer.shouldAttemptStep(true, false, true, false, false));
        assertTrue(ZombieBlockPlacer.shouldAttemptStep(true, false, true, true, true));
        assertFalse(ZombieBlockPlacer.shouldAttemptStep(true, false, true, true, false));
        assertFalse(ZombieBlockPlacer.shouldAttemptStep(true, false, false, false, true));
    }
}
