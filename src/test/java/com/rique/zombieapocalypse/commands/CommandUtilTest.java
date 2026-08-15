package com.rique.zombieapocalypse.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

class CommandUtilTest {

    @Test
    void ticksShowsWholeSeconds() {
        assertEquals("120 ticks (6 seconds)", CommandUtil.ticks(120));
    }

    @Test
    void ticksShowsFractionalSeconds() {
        assertEquals("5 ticks (0.25 seconds)", CommandUtil.ticks(5));
        assertEquals("1 tick (0.05 seconds)", CommandUtil.ticks(1));
    }

    @Test
    void ticksUsesSingularSecond() {
        assertEquals("20 ticks (1 second)", CommandUtil.ticks(20));
    }

    @Test
    void countUsesSingularAndPluralForms() {
        assertEquals("1 zombie", CommandUtil.count(1, "zombie"));
        assertEquals("2 zombies", CommandUtil.count(2, "zombie"));
    }

    @Test
    void toggleInputsAreSimpleAndBackwardCompatible() throws Exception {
        assertTrue(CommandUtil.parseToggle("on"));
        assertTrue(CommandUtil.parseToggle("TRUE"));
        assertFalse(CommandUtil.parseToggle("off"));
        assertFalse(CommandUtil.parseToggle("False"));
        assertThrows(CommandSyntaxException.class, () -> CommandUtil.parseToggle("maybe"));
    }
}
