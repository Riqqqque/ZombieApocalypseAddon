package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

class EventHandlerTest {

    @Test
    void computeSpawnQuotaRespectsRemainingCapacity() {
        assertEquals(5, EventHandler.computeSpawnQuota(10, 50, 5));
        assertEquals(1, EventHandler.computeSpawnQuota(49, 50, 5));
        assertEquals(0, EventHandler.computeSpawnQuota(50, 50, 5));
        assertEquals(0, EventHandler.computeSpawnQuota(55, 50, 5));
    }

    @Test
    void daytimeSpawnGateHandlesNightOnlyGraceHordesAndFixedTimeDimensions() {
        assertFalse(EventHandler.isDaytimeCustomSpawningBlocked(true, false, false, 0, 10, false));
        assertTrue(EventHandler.isDaytimeCustomSpawningBlocked(true, true, false, 10, 0, false));
        assertTrue(EventHandler.isDaytimeCustomSpawningBlocked(true, true, false, 10, 0, true));
        assertTrue(EventHandler.isDaytimeCustomSpawningBlocked(true, true, true, 9, 10, false));
        assertFalse(EventHandler.isDaytimeCustomSpawningBlocked(true, true, true, 10, 10, false));
        assertFalse(EventHandler.isDaytimeCustomSpawningBlocked(true, true, true, 0, 0, false));
        assertFalse(EventHandler.isDaytimeCustomSpawningBlocked(true, true, true, 5, 10, true));
        assertFalse(EventHandler.isDaytimeCustomSpawningBlocked(false, true, false, 0, 10, false));
    }

    @Test
    void spawnDistanceIsOnlyImpossibleOutsideTheSpawnSquare() {
        assertEquals(false, SpawnMath.isSpawnDistanceImpossible(12, 30));
        assertEquals(false, SpawnMath.isSpawnDistanceImpossible(30, 30));
        assertEquals(false, SpawnMath.isSpawnDistanceImpossible(42, 30));
        assertEquals(true, SpawnMath.isSpawnDistanceImpossible(43, 30));
        assertEquals(42, SpawnMath.maxHorizontalDistance(30));
    }

    @Test
    void minimumSpawnDistanceUsesHorizontalDistanceOnly() {
        assertEquals(25L, EventHandler.horizontalDistanceSquared(new BlockPos(3, 90, 4), new BlockPos(0, 20, 0)));
    }

    @Test
    void blockLightLimitCanDisableOrCapCustomSpawns() {
        assertTrue(EventHandler.isBlockLightSpawnAllowed(15, -1));
        assertTrue(EventHandler.isBlockLightSpawnAllowed(0, 0));
        assertFalse(EventHandler.isBlockLightSpawnAllowed(1, 0));
        assertTrue(EventHandler.isBlockLightSpawnAllowed(7, 7));
        assertFalse(EventHandler.isBlockLightSpawnAllowed(8, 7));
        assertTrue(EventHandler.isBlockLightSpawnAllowed(20, 15));
        assertTrue(EventHandler.isBlockLightSpawnAllowed(-5, 0));
    }

    @Test
    void mushroomSafeZoneDoesNotDependOnVariantBiomeModifiers() {
        assertFalse(EventHandler.isBiomeSpawnAllowed(true, true));
        assertTrue(EventHandler.isBiomeSpawnAllowed(false, true));
        assertTrue(EventHandler.isBiomeSpawnAllowed(true, false));
    }

    @Test
    void nightBoostOnlyAppliesToDimensionsWithARealNight() {
        assertTrue(EventHandler.shouldApplyNightBoost(true, false, true));
        assertFalse(EventHandler.shouldApplyNightBoost(true, true, true));
        assertFalse(EventHandler.shouldApplyNightBoost(false, false, true));
        assertFalse(EventHandler.shouldApplyNightBoost(true, false, false));
    }

    @Test
    void disabledSiegeFeaturesUseTheFastPath() {
        assertFalse(EventHandler.hasEnabledSiegeFeature(false, false, false));
        assertTrue(EventHandler.hasEnabledSiegeFeature(true, false, false));
        assertTrue(EventHandler.hasEnabledSiegeFeature(false, true, false));
        assertTrue(EventHandler.hasEnabledSiegeFeature(false, false, true));
    }

    @Test
    void customWavesIgnoreDeadAndNonSurvivalPlayers() {
        assertTrue(EventHandler.isEligibleSpawnPlayer(true, false, false));
        assertFalse(EventHandler.isEligibleSpawnPlayer(false, false, false));
        assertFalse(EventHandler.isEligibleSpawnPlayer(true, true, false));
        assertFalse(EventHandler.isEligibleSpawnPlayer(true, false, true));
    }

    @Test
    void maxBlockLightDebugTextShowsIgnoredOrClampedValue() {
        assertEquals("ignored", EventHandler.formatMaxBlockLight(-1));
        assertEquals("0", EventHandler.formatMaxBlockLight(0));
        assertEquals("15", EventHandler.formatMaxBlockLight(20));
    }

    @Test
    void babyZombieChanceZeroForcesAdultsOnly() {
        assertTrue(EventHandler.shouldForceAdultZombie(true, 0.0));
        assertTrue(EventHandler.shouldForceAdultZombie(true, -1.0));
        assertFalse(EventHandler.shouldForceAdultZombie(true, 0.01));
        assertFalse(EventHandler.shouldForceAdultZombie(false, 0.0));
    }

    @Test
    void sunFireCancelOnlyAppliesToVanillaOnFireTicks() {
        assertTrue(EventHandler.shouldCancelSunFireDamage(true, true, false));
        assertFalse(EventHandler.shouldCancelSunFireDamage(false, true, false));
        assertFalse(EventHandler.shouldCancelSunFireDamage(true, false, false));
        assertFalse(EventHandler.shouldCancelSunFireDamage(true, true, true));
    }

    @Test
    void sunlightThresholdMatchesVanillaBrightnessCurve() {
        assertFalse(EventHandler.hasStrongSunlight(0, 0.0F));
        assertFalse(EventHandler.hasStrongSunlight(11, 0.0F));
        assertTrue(EventHandler.hasStrongSunlight(12, 0.0F));
        assertTrue(EventHandler.hasStrongSunlight(0, 0.75F));
    }
}
