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
