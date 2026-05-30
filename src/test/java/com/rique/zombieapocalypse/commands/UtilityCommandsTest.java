package com.rique.zombieapocalypse.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UtilityCommandsTest {

    @Test
    void cleanupMessageExplainsNormalCleanup() {
        assertEquals(
                "Cleanup complete. Removed 3 zombie-class entities and reset apocalypse event state.",
                UtilityCommands.buildCleanupMessage(3, false));
    }

    @Test
    void cleanupMessageExplainsUninstallPrep() {
        assertEquals(
                "Cleanup complete. Removed 0 zombie-class entities and reset apocalypse event state. Custom spawning, hordes, blood moons, scaling, and attribute tuning are disabled for this server session.",
                UtilityCommands.buildCleanupMessage(0, true));
    }
}
