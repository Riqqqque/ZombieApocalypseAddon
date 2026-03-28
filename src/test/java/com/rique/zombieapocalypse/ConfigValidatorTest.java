package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class ConfigValidatorTest {

    @Test
    void biomeAdjustedVariantWeightsKeepsBaseWhenVariantsAreDisabled() {
        SpawnMath.VariantWeights base = SpawnMath.normalizeVariantWeights(0.2, 0.3, 0.1);

        SpawnMath.VariantWeights adjusted = ConfigValidator.biomeAdjustedVariantWeights(
                base,
                false,
                true,
                0.5,
                0.5,
                true,
                true);

        assertSame(base, adjusted);
    }

    @Test
    void biomeAdjustedVariantWeightsAppliesBiomeBonusesAndRenormalizes() {
        SpawnMath.VariantWeights base = SpawnMath.normalizeVariantWeights(0.10, 0.20, 0.10);

        SpawnMath.VariantWeights adjusted = ConfigValidator.biomeAdjustedVariantWeights(
                base,
                true,
                true,
                0.30,
                0.40,
                true,
                true);

        SpawnMath.VariantWeights expected = SpawnMath.normalizeVariantWeights(0.10, 0.50, 0.50);
        assertEquals(expected.zombieVillagerChance(), adjusted.zombieVillagerChance(), 1.0e-9);
        assertEquals(expected.huskChance(), adjusted.huskChance(), 1.0e-9);
        assertEquals(expected.drownedChance(), adjusted.drownedChance(), 1.0e-9);
    }
}
