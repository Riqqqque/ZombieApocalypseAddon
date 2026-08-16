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
