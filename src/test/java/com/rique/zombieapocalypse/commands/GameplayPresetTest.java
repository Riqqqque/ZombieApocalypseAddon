package com.rique.zombieapocalypse.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.ConfigGuard;

class GameplayPresetTest {

    @Test
    void freshInstallMatchesTheStandardPreset() {
        Map<List<String>, Object> defaults = Config.defaultValues();
        GameplayPreset.STANDARD.settings().forEach((path, value) ->
                assertTrue(ConfigGuard.sameValue(defaults.get(path), value),
                        String.join(".", path) + ": default " + defaults.get(path) + ", standard preset " + value));
    }

    @Test
    void everyPresetValueIsAllowedByTheConfig() {
        for (GameplayPreset preset : GameplayPreset.values()) {
            preset.settings().forEach((path, value) -> {
                Object spec = Config.COMMON_SPEC.getSpec().get(path);
                assertTrue(spec instanceof net.neoforged.neoforge.common.ModConfigSpec.ValueSpec valueSpec
                                && valueSpec.test(value),
                        preset + " " + String.join(".", path) + " = " + value);
            });
        }
    }
}
