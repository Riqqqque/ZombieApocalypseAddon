package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.world.phys.Vec3;

class ZombieToweringTest {

    @Test
    void toweringStartsOnlyWhenEnabledAndDayReached() {
        assertFalse(ZombieTowering.isToweringActive(false, 40, 20));
        assertFalse(ZombieTowering.isToweringActive(true, 19, 20));
        assertTrue(ZombieTowering.isToweringActive(true, 20, 20));
        assertTrue(ZombieTowering.isToweringActive(true, 0, -5));
    }

    @Test
    void targetLimitsPreventDistantOrRunawayTowers() {
        assertTrue(ZombieTowering.isTargetDistanceAllowed(1024.0, 32));
        assertFalse(ZombieTowering.isTargetDistanceAllowed(1024.01, 32));
        assertTrue(ZombieTowering.isHeightAllowed(18.0, 10.0, 8));
        assertFalse(ZombieTowering.isHeightAllowed(18.01, 10.0, 8));
    }

    @Test
    void obstacleRuleAllowsWallsCollisionsAndRaisedOrCoveredTargets() {
        assertTrue(ZombieTowering.shouldAttemptTower(false, false, false, false, true));
        assertTrue(ZombieTowering.shouldAttemptTower(true, true, false, false, true));
        assertTrue(ZombieTowering.shouldAttemptTower(true, false, true, false, true));
        assertTrue(ZombieTowering.shouldAttemptTower(true, false, false, true, true));
        assertTrue(ZombieTowering.shouldAttemptTower(true, false, false, false, false));
        assertFalse(ZombieTowering.shouldAttemptTower(true, false, false, false, true));
    }

    @Test
    void crowdAndSupportRulesRequireARealNearbySwarm() {
        assertFalse(ZombieTowering.hasRequiredCrowd(1, 2));
        assertTrue(ZombieTowering.hasRequiredCrowd(2, 2));
        assertTrue(ZombieTowering.isSupportPosition(1.0, 0.0, 1.35));
        assertTrue(ZombieTowering.isSupportPosition(1.0, 1.75, 1.35));
        assertFalse(ZombieTowering.isSupportPosition(2.0, 0.0, 1.35));
        assertFalse(ZombieTowering.isSupportPosition(1.0, 1.76, 1.35));
    }

    @Test
    void boostPreservesUpwardMomentumAndCapsHorizontalSpeed() {
        Vec3 boosted = ZombieTowering.computeBoostedMovement(
                new Vec3(2.0, 0.7, -2.0),
                new Vec3(1.0, 0.0, 0.0),
                0.48,
                0.18);

        assertEquals(0.27, boosted.x, 1.0E-9);
        assertEquals(0.7, boosted.y, 1.0E-9);
        assertEquals(-0.27, boosted.z, 1.0E-9);
    }
}
