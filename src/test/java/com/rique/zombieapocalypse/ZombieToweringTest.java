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
        assertTrue(ZombieTowering.isHeightAllowed(65.99, 64.0, true, 1, 8));
        assertFalse(ZombieTowering.isHeightAllowed(66.0, 64.0, true, 1, 8));
        assertTrue(ZombieTowering.isHeightAllowed(18.0, 10.0, false, 1, 8));
        assertFalse(ZombieTowering.isHeightAllowed(18.01, 10.0, false, 1, 8));
        assertTrue(ZombieTowering.isHeightAllowed(10_000.0, 10.0, false, 1, 0));
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
        assertTrue(ZombieTowering.isSupportPosition(1.0, -2.5, 1.35));
        assertTrue(ZombieTowering.isSupportPosition(1.0, 1.0, 1.35));
        assertFalse(ZombieTowering.isSupportPosition(2.0, 0.0, 1.35));
        assertFalse(ZombieTowering.isSupportPosition(1.0, -2.51, 1.35));
        assertFalse(ZombieTowering.isSupportPosition(1.0, 1.01, 1.35));
    }

    @Test
    void stackSizeSupportsFiniteDisabledAndUnlimitedModes() {
        assertTrue(ZombieTowering.canGrowStack(1, 4));
        assertTrue(ZombieTowering.canGrowStack(3, 4));
        assertFalse(ZombieTowering.canGrowStack(4, 4));
        assertFalse(ZombieTowering.canGrowStack(8, 4));
        assertFalse(ZombieTowering.canGrowStack(1, 1));
        assertTrue(ZombieTowering.canGrowStack(ConfigLimits.MAX_TOWER_STACK_SIZE, 0));
    }

    @Test
    void perPlayerTowerLimitSupportsFiniteAndUnlimitedModes() {
        assertTrue(ZombieTowering.hasTowerSlot(0, 3));
        assertTrue(ZombieTowering.hasTowerSlot(2, 3));
        assertFalse(ZombieTowering.hasTowerSlot(3, 3));
        assertFalse(ZombieTowering.hasTowerSlot(4, 3));
        assertTrue(ZombieTowering.hasTowerSlot(10_000, 0));
    }

    @Test
    void temporaryTargetLossDoesNotImmediatelyCollapseTower() {
        assertFalse(ZombieTowering.isTargetLossGraceExpired(1_000L, 1_100L));
        assertTrue(ZombieTowering.isTargetLossGraceExpired(1_000L, 1_101L));
    }

    @Test
    void jumpCooldownPreventsWholeTowerFromDismountingTogether() {
        assertTrue(ZombieTowering.canJumpFromTower(1_000L, -1L, 10));
        assertFalse(ZombieTowering.canJumpFromTower(1_009L, 1_000L, 10));
        assertTrue(ZombieTowering.canJumpFromTower(1_010L, 1_000L, 10));
    }

    @Test
    void topZombieDismountsOnlyWhenItCanReachTheTarget() {
        assertTrue(ZombieTowering.shouldDismount(4.0, 10.0, 10.0, 2.75, true));
        assertFalse(ZombieTowering.shouldDismount(0.99, 10.0, 10.0, 2.75, true));
        assertFalse(ZombieTowering.shouldDismount(4.0, 8.0, 10.0, 2.75, true));
        assertFalse(ZombieTowering.shouldDismount(9.0, 10.0, 10.0, 2.75, true));
        assertFalse(ZombieTowering.shouldDismount(4.0, 10.0, 10.0, 2.75, false));
    }

    @Test
    void smartDismountRequiresReachableGround() {
        assertTrue(ZombieTowering.shouldSmartDismount(true, true, 64.0, 64.0, false, false, true));
        assertFalse(ZombieTowering.shouldSmartDismount(false, true, 64.0, 64.0, false, false, true));
        assertFalse(ZombieTowering.shouldSmartDismount(true, false, 64.0, 64.0, false, false, true));
        assertFalse(ZombieTowering.shouldSmartDismount(true, true, 64.0, 66.0, false, false, true));
        assertFalse(ZombieTowering.shouldSmartDismount(true, true, 64.0, 64.0, true, false, true));
        assertFalse(ZombieTowering.shouldSmartDismount(true, true, 64.0, 64.0, false, true, true));
        assertFalse(ZombieTowering.shouldSmartDismount(true, true, 64.0, 64.0, false, false, false));
    }

    @Test
    void jumpBoostDiscardsInheritedHitVelocity() {
        Vec3 boosted = ZombieTowering.computeBoostedMovement(
                new Vec3(1.0, 0.0, 0.0),
                0.48,
                0.18);

        assertEquals(0.18, boosted.x, 1.0E-9);
        assertEquals(0.48, boosted.y, 1.0E-9);
        assertEquals(0.0, boosted.z, 1.0E-9);
    }

    @Test
    void safeDismountNeverAddsUpwardOrExtremeHorizontalVelocity() {
        Vec3 movement = ZombieTowering.computeSafeDismountMovement(
                new Vec3(8.0, 5.0, -8.0),
                new Vec3(1.0, 0.0, -1.0));

        assertEquals(0.12, movement.x, 1.0E-9);
        assertEquals(0.0, movement.y, 1.0E-9);
        assertEquals(-0.12, movement.z, 1.0E-9);
    }
}
