package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.neoforged.neoforge.common.ModConfigSpec;

class ConfigSpecTest {

    @Test
    void toweringDefaultsAreBalancedAndReviewedRangesAreAccepted() {
        assertEquals(0, defaultValue(Config.COMMON.zombieToweringMaxStackSize));
        assertEquals(3, defaultValue(Config.COMMON.zombieToweringMaxTowersPerPlayer));
        assertEquals(true, defaultValue(Config.COMMON.zombieToweringDynamicHeightEnabled));
        assertEquals(1, defaultValue(Config.COMMON.zombieToweringTargetHeightOffset));
        assertEquals(true, defaultValue(Config.COMMON.zombieToweringSmartDismountEnabled));
        assertEquals(false, defaultValue(Config.COMMON.zombieToweringJumpingEnabled));
        assertEquals(10, defaultValue(Config.COMMON.zombieToweringJumpCooldownTicks));

        assertAccepts(Config.COMMON.zombieToweringMaxStackSize, 0);
        assertAccepts(Config.COMMON.zombieToweringMaxStackSize, ConfigLimits.MAX_TOWER_STACK_SIZE);
        assertRejects(Config.COMMON.zombieToweringMaxStackSize, ConfigLimits.MAX_TOWER_STACK_SIZE + 1);
        assertAccepts(Config.COMMON.zombieToweringMaxTowersPerPlayer, 0);
        assertAccepts(Config.COMMON.zombieToweringMaxTowersPerPlayer, ConfigLimits.MAX_TOWERS_PER_PLAYER);
        assertRejects(Config.COMMON.zombieToweringMaxTowersPerPlayer, ConfigLimits.MAX_TOWERS_PER_PLAYER + 1);
        assertAccepts(Config.COMMON.zombieToweringTargetHeightOffset, ConfigLimits.MAX_TOWER_HEIGHT_OFFSET);
        assertRejects(Config.COMMON.zombieToweringTargetHeightOffset, ConfigLimits.MAX_TOWER_HEIGHT_OFFSET + 1);
        assertAccepts(Config.COMMON.zombieToweringMaxHeightAboveTarget, 0);
        assertAccepts(Config.COMMON.zombieToweringMaxHeightAboveTarget, ConfigLimits.MAX_TOWER_HEIGHT_LIMIT);
        assertRejects(Config.COMMON.zombieToweringMaxHeightAboveTarget, ConfigLimits.MAX_TOWER_HEIGHT_LIMIT + 1);
        assertAccepts(Config.COMMON.zombieToweringInterval, 1);
    }

    @Test
    void everyDayGateUsesTheSharedLongWorldRange() {
        assertAccepts(Config.COMMON.daylightSpawnStartDay, ConfigLimits.MAX_APOCALYPSE_DAY);
        assertAccepts(Config.COMMON.zombieBlockBreakingStartDay, ConfigLimits.MAX_APOCALYPSE_DAY);
        assertAccepts(Config.COMMON.zombieBlockPlacingStartDay, ConfigLimits.MAX_APOCALYPSE_DAY);
        assertAccepts(Config.COMMON.zombieToweringStartDay, ConfigLimits.MAX_APOCALYPSE_DAY);
        assertAccepts(Config.COMMON.scalingStartDay, ConfigLimits.MAX_APOCALYPSE_DAY);
        assertAccepts(Config.COMMON.maxScalingDay, ConfigLimits.MAX_APOCALYPSE_DAY);
        assertAccepts(Config.COMMON.huntStartDay, ConfigLimits.MAX_APOCALYPSE_DAY);
    }

    @Test
    void animalHuntingIsOffByDefaultWithBalancedRanges() {
        assertEquals(false, defaultValue(Config.COMMON.enableAnimalHunting));
        assertEquals(false, defaultValue(Config.COMMON.huntBabies));
        assertEquals(false, defaultValue(Config.COMMON.alwaysHunting));
        assertEquals(false, defaultValue(Config.COMMON.berserkerHunting));
        assertEquals(0, defaultValue(Config.COMMON.huntStartDay));
        assertEquals(1200, defaultValue(Config.COMMON.huntCooldownTicks));
        assertEquals(10, defaultValue(Config.COMMON.huntCapPerZombiePerDay));
        assertEquals(20, defaultValue(Config.COMMON.huntCapPerLevelPerDay));
        assertEquals(1.0, defaultValue(Config.COMMON.huntFollowDistanceFactor));
        assertEquals(true, defaultValue(Config.COMMON.eatDroppedFood));
        assertEquals(40, defaultValue(Config.COMMON.eatCooldownTicks));
        assertEquals(1, defaultValue(Config.COMMON.healthPerNutrition));
        assertEquals(20, defaultValue(Config.COMMON.feedingMaxHealthBoost));
        assertEquals(20, defaultValue(Config.COMMON.feedingMaxHealthBoostHardBonus));
        assertEquals(true, defaultValue(Config.COMMON.rottenFleshGivesResistance));
        assertEquals(100, defaultValue(Config.COMMON.foodCombatLockTicks));
        assertEquals(8.0, defaultValue(Config.COMMON.foodCombatLockDistance));
        assertEquals(false, defaultValue(Config.COMMON.persistentAfterEating));
        assertEquals(false, defaultValue(Config.COMMON.fedZombiesDropExtraLoot));
        assertEquals(1.0, defaultValue(Config.COMMON.extraLootHealthRatio));
        assertEquals(true, defaultValue(Config.COMMON.fedZombiesBecomeLeaders));
        assertEquals(false, defaultValue(Config.COMMON.zombifyHorses));
        assertEquals(false, defaultValue(Config.COMMON.zombifyTamedHorses));
        assertEquals(false, defaultValue(Config.COMMON.rideZombieHorses));
        assertEquals(true, defaultValue(Config.COMMON.neverHuntZombieHorses));
        assertEquals("", defaultValue(Config.COMMON.additionalHuntTargets));
        assertEquals("", defaultValue(Config.COMMON.excludedHuntTargets));

        assertAccepts(Config.COMMON.huntCooldownTicks, 0);
        assertAccepts(Config.COMMON.huntCooldownTicks, 72000);
        assertRejects(Config.COMMON.huntCooldownTicks, -1);
        assertRejects(Config.COMMON.huntCooldownTicks, 72001);
        assertAccepts(Config.COMMON.huntCapPerZombiePerDay, 0);
        assertRejects(Config.COMMON.huntCapPerZombiePerDay, -1);
        assertAccepts(Config.COMMON.huntCapPerLevelPerDay, 0);
        assertRejects(Config.COMMON.huntCapPerLevelPerDay, -1);
        assertAccepts(Config.COMMON.huntFollowDistanceFactor, 0.0);
        assertAccepts(Config.COMMON.huntFollowDistanceFactor, 64.0);
        assertRejects(Config.COMMON.huntFollowDistanceFactor, 64.1);
        assertAccepts(Config.COMMON.eatCooldownTicks, 0);
        assertRejects(Config.COMMON.eatCooldownTicks, -1);
        assertAccepts(Config.COMMON.healthPerNutrition, 0);
        assertAccepts(Config.COMMON.healthPerNutrition, 40);
        assertRejects(Config.COMMON.healthPerNutrition, 41);
        assertAccepts(Config.COMMON.feedingMaxHealthBoost, 0);
        assertAccepts(Config.COMMON.feedingMaxHealthBoost, 1024);
        assertRejects(Config.COMMON.feedingMaxHealthBoost, 1025);
        assertAccepts(Config.COMMON.feedingMaxHealthBoostHardBonus, 0);
        assertRejects(Config.COMMON.feedingMaxHealthBoostHardBonus, 1025);
        assertAccepts(Config.COMMON.foodCombatLockTicks, 0);
        assertRejects(Config.COMMON.foodCombatLockTicks, -1);
        assertAccepts(Config.COMMON.foodCombatLockDistance, 0.0);
        assertAccepts(Config.COMMON.foodCombatLockDistance, 64.0);
        assertRejects(Config.COMMON.foodCombatLockDistance, 64.1);
        assertAccepts(Config.COMMON.extraLootHealthRatio, 0.1);
        assertRejects(Config.COMMON.extraLootHealthRatio, 0.05);
        assertRejects(Config.COMMON.extraLootHealthRatio, 64.1);
    }

    private static void assertAccepts(ModConfigSpec.ConfigValue<?> setting, Object value) {
        assertTrue(valueSpec(setting).test(value), setting.getPath().toString());
    }

    private static void assertRejects(ModConfigSpec.ConfigValue<?> setting, Object value) {
        assertFalse(valueSpec(setting).test(value), setting.getPath().toString());
    }

    private static Object defaultValue(ModConfigSpec.ConfigValue<?> setting) {
        return valueSpec(setting).getDefault();
    }

    private static ModConfigSpec.ValueSpec valueSpec(ModConfigSpec.ConfigValue<?> setting) {
        ModConfigSpec.ValueSpec spec = Config.COMMON_SPEC.getSpec().get(setting.getPath());
        assertNotNull(spec, setting.getPath().toString());
        return spec;
    }
}
