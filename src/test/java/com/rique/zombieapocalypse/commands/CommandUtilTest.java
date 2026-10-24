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

    @Test
    void startLineSaysWhenAFeatureRuns() {
        assertEquals("Active now (current day 12, start day 10).", CommandUtil.startLine(12, 10, "/za breaking dayone"));
        assertEquals("Active now (current day 0, start day 0).", CommandUtil.startLine(0, 0, "/za breaking dayone"));
        assertEquals("Starts on day 50 (today is day 12). /za breaking dayone starts it now.",
                CommandUtil.startLine(12, 50, "/za breaking dayone"));
    }

    @Test
    void notesFollowTheHeadlineOnTheirOwnLines() {
        assertEquals("Hordes: ON", CommandUtil.withNotes("Hordes: ON", java.util.List.of()));
        assertEquals("Hordes: ON\nfirst\nsecond", CommandUtil.withNotes("Hordes: ON", java.util.List.of("first", "second")));
    }

    @Test
    void dayStatusShowsClockTimeAndPhase() {
        assertEquals("06:00 (day)", WorldCommands.describeTime(0));
        assertEquals("12:00 (day)", WorldCommands.describeTime(6000));
        assertEquals("18:00 (dusk)", WorldCommands.describeTime(12000));
        assertEquals("19:30 (night)", WorldCommands.describeTime(13500));
        assertEquals("05:00 (dawn)", WorldCommands.describeTime(23000));
        assertEquals("06:00 (day)", WorldCommands.describeTime(24000));
    }
}
