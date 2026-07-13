package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ZombieBlockBreakerTest {

    @Test
    void blockBreakingStartsOnlyWhenEnabledAndDayReached() {
        assertFalse(ZombieBlockBreaker.isBlockBreakingActive(false, 20, 10));
        assertFalse(ZombieBlockBreaker.isBlockBreakingActive(true, 9, 10));
        assertTrue(ZombieBlockBreaker.isBlockBreakingActive(true, 10, 10));
        assertTrue(ZombieBlockBreaker.isBlockBreakingActive(true, 0, -5));
    }

    @Test
    void scheduledTickSpreadsChecksByEntityId() {
        assertTrue(ZombieBlockBreaker.isScheduledTick(100, 0, 100));
        assertFalse(ZombieBlockBreaker.isScheduledTick(100, 1, 100));
        assertTrue(ZombieBlockBreaker.isScheduledTick(99, 1, 100));
        assertTrue(ZombieBlockBreaker.isScheduledTick(42, 0, 0));
    }

    @Test
    void obstacleRequirementAllowsBlockedOrCoveredTargetsOnly() {
        assertTrue(ZombieBlockBreaker.isObstacleCheckSatisfied(false, false, false, false));
        assertTrue(ZombieBlockBreaker.isObstacleCheckSatisfied(true, true, false, false));
        assertTrue(ZombieBlockBreaker.isObstacleCheckSatisfied(true, false, true, false));
        assertFalse(ZombieBlockBreaker.isObstacleCheckSatisfied(true, false, true, true));
        assertFalse(ZombieBlockBreaker.isObstacleCheckSatisfied(true, false, false, false));
    }

    @Test
    void hardnessLimitRejectsUnbreakableAndTooHardBlocks() {
        assertFalse(ZombieBlockBreaker.isHardnessAllowed(-1.0F, 50.0));
        assertTrue(ZombieBlockBreaker.isHardnessAllowed(0.0F, 0.0));
        assertTrue(ZombieBlockBreaker.isHardnessAllowed(2.0F, 2.0));
        assertFalse(ZombieBlockBreaker.isHardnessAllowed(2.1F, 2.0));
        assertFalse(ZombieBlockBreaker.isHardnessAllowed(0.1F, -1.0));
    }

    @Test
    void configProtectionCanBlockContainersToolBlocksAndLights() {
        assertTrue(ZombieBlockBreaker.isProtectedByConfig(true, false, false, false, 0, false));
        assertFalse(ZombieBlockBreaker.isProtectedByConfig(true, true, false, false, 0, false));

        assertTrue(ZombieBlockBreaker.isProtectedByConfig(false, false, true, false, 0, false));
        assertFalse(ZombieBlockBreaker.isProtectedByConfig(false, false, true, true, 0, false));

        assertTrue(ZombieBlockBreaker.isProtectedByConfig(false, false, false, false, 15, false));
        assertFalse(ZombieBlockBreaker.isProtectedByConfig(false, false, false, false, 15, true));
    }

    @Test
    void directionalSearchOnlyPassesThroughAirAndNonCollidingFluids() {
        assertTrue(ZombieBlockBreaker.isSearchPassThrough(true, false, false));
        assertTrue(ZombieBlockBreaker.isSearchPassThrough(false, true, true));
        assertFalse(ZombieBlockBreaker.isSearchPassThrough(false, false, true));
        assertFalse(ZombieBlockBreaker.isSearchPassThrough(false, true, false));
    }
}
