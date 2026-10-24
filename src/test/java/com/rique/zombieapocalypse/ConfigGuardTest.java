package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ConfigGuardTest {

    private static final List<String> CHANCE = List.of("dayspawning", "daySpawnChance");
    private static final List<String> RANGE = List.of("dayspawning", "spawnRange");
    private static final List<String> LIGHT = List.of("dayspawning", "maxBlockLightForSpawning");
    private static final List<String> HORDES = List.of("horde", "enableHordeEvents");

    private static Map<List<String>, Object> values(Object chance, Object range, Object light, Object hordes) {
        Map<List<String>, Object> values = new LinkedHashMap<>();
        values.put(CHANCE, chance);
        values.put(RANGE, range);
        values.put(LIGHT, light);
        values.put(HORDES, hordes);
        return values;
    }

    private static final Map<List<String>, Object> DEFAULTS = values(0.5, 30, 0, true);

    @Test
    void customValuesThatJumpToDefaultsAreWiped() {
        Map<List<String>, Object> before = values(0.2, 40, 7, false);
        Map<List<String>, Object> after = values(0.5, 30, 0, true);

        Map<List<String>, Object> wiped = ConfigGuard.wipedSettings(before, after, DEFAULTS);

        assertEquals(before, wiped);
    }

    @Test
    void settingsThatWereAlreadyDefaultOrChangedElsewhereAreLeftAlone() {
        Map<List<String>, Object> before = values(0.5, 40, 7, true);
        Map<List<String>, Object> after = values(0.5, 30, 12, true);

        Map<List<String>, Object> wiped = ConfigGuard.wipedSettings(before, after, DEFAULTS);

        assertEquals(Map.of(RANGE, 40), wiped);
    }

    @Test
    void numbersCompareByValueAcrossTypes() {
        Map<List<String>, Object> before = values(0.2, 40L, 7, false);
        Map<List<String>, Object> after = values(0.5, 30L, 0.0, true);

        Map<List<String>, Object> wiped = ConfigGuard.wipedSettings(before, after, DEFAULTS);

        assertEquals(4, wiped.size());
        assertTrue(ConfigGuard.sameValue(30, 30L));
        assertTrue(ConfigGuard.sameValue(0, 0.0));
        assertFalse(ConfigGuard.sameValue(0.2, 0.5));
        assertFalse(ConfigGuard.sameValue("minecraft:dirt", "minecraft:cobblestone"));
    }

    @Test
    void restoreSkipsSettingsTheEditorSaveAlreadyFixed() {
        Map<List<String>, Object> wiped = values(0.2, 40, 7, false);
        Map<List<String>, Object> now = values(0.33, 30, 0, true);

        Map<List<String>, Object> restore = ConfigGuard.stillWiped(wiped, now, DEFAULTS);

        assertFalse(restore.containsKey(CHANCE));
        assertEquals(40, restore.get(RANGE));
        assertEquals(7, restore.get(LIGHT));
        assertEquals(false, restore.get(HORDES));
    }

    @Test
    void recognizesLoaderBackupNames() {
        String base = "zombieapocalypseaddon-common";
        assertTrue(ConfigGuard.isBackupName("zombieapocalypseaddon-common-1.toml.bak", base));
        assertTrue(ConfigGuard.isBackupName("zombieapocalypseaddon-common-12.toml.bak", base));
        assertFalse(ConfigGuard.isBackupName("zombieapocalypseaddon-common.toml", base));
        assertFalse(ConfigGuard.isBackupName("zombieapocalypseaddon-common.toml.tmp", base));
        assertFalse(ConfigGuard.isBackupName("othermod-common-1.toml.bak", base));
    }
}
