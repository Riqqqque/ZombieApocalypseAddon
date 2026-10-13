package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SpawnMathTest {

    @Test
    void clampProbabilityHandlesInvalidAndBounds() {
        assertEquals(0.0, SpawnMath.clampProbability(Double.NaN));
        assertEquals(0.0, SpawnMath.clampProbability(-2.0));
        assertEquals(1.0, SpawnMath.clampProbability(5.0));
        assertEquals(0.35, SpawnMath.clampProbability(0.35));
    }

    @Test
    void computeLinearScaleHandlesRanges() {
        assertEquals(0.0, SpawnMath.computeLinearScale(2, 3, 10));
        assertEquals(0.0, SpawnMath.computeLinearScale(3, 3, 10));
        assertEquals(0.5, SpawnMath.computeLinearScale(6, 3, 9));
        assertEquals(1.0, SpawnMath.computeLinearScale(20, 3, 10));
        assertEquals(1.0, SpawnMath.computeLinearScale(5, 5, 5));
    }

    @Test
    void steppedScalingDayRisesTheMorningAfterEachStep() {
        assertEquals(7, SpawnMath.steppedScalingDay(7, 0));
        assertEquals(0, SpawnMath.steppedScalingDay(0, 10));
        assertEquals(0, SpawnMath.steppedScalingDay(1, 10));
        assertEquals(0, SpawnMath.steppedScalingDay(10, 10));
        assertEquals(10, SpawnMath.steppedScalingDay(11, 10));
        assertEquals(10, SpawnMath.steppedScalingDay(20, 10));
        assertEquals(20, SpawnMath.steppedScalingDay(21, 10));
        assertEquals(50, SpawnMath.steppedScalingDay(51, 10));
    }

    @Test
    void nextScalingStepDayPointsAtTheNextRise() {
        assertEquals(-1, SpawnMath.nextScalingStepDay(5, 0));
        assertEquals(11, SpawnMath.nextScalingStepDay(0, 10));
        assertEquals(11, SpawnMath.nextScalingStepDay(10, 10));
        assertEquals(21, SpawnMath.nextScalingStepDay(11, 10));
    }

    @Test
    void scaledExtraZombiesFollowsScalingStrength() {
        assertEquals(0, SpawnMath.scaledExtraZombies(0, 1.0));
        assertEquals(0, SpawnMath.scaledExtraZombies(4, 0.0));
        assertEquals(2, SpawnMath.scaledExtraZombies(4, 0.5));
        assertEquals(4, SpawnMath.scaledExtraZombies(4, 1.0));
        assertEquals(4, SpawnMath.scaledExtraZombies(4, 3.0));
        assertEquals(0, SpawnMath.scaledExtraZombies(4, Double.NaN));
    }

    @Test
    void normalizeVariantWeightsNeverExceedsOne() {
        SpawnMath.VariantWeights weights = SpawnMath.normalizeVariantWeights(0.8, 0.8, 0.8);
        assertEquals(1.0, weights.totalVariantChance(), 0.000001);

        SpawnMath.VariantWeights untouched = SpawnMath.normalizeVariantWeights(0.1, 0.2, 0.3);
        assertEquals(0.6, untouched.totalVariantChance(), 0.000001);
        assertEquals(0.1, untouched.zombieVillagerChance(), 0.000001);
        assertEquals(0.2, untouched.huskChance(), 0.000001);
        assertEquals(0.3, untouched.drownedChance(), 0.000001);
    }
}
