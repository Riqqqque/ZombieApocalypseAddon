package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import net.minecraft.world.level.Level;

class TowerLinksTest {

    private final UUID bottom = UUID.randomUUID();
    private final UUID middle = UUID.randomUUID();
    private final UUID top = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();

    @AfterEach
    void clearLinks() {
        TowerLinks.clear();
    }

    @Test
    void linksWorkInBothDirections() {
        TowerLinks.link(middle, bottom, Level.OVERWORLD);
        TowerLinks.link(top, middle, Level.OVERWORLD);

        assertEquals(bottom, TowerLinks.supportIdOf(middle));
        assertEquals(middle, TowerLinks.supportIdOf(top));
        assertEquals(middle, TowerLinks.upperIdOf(bottom));
        assertEquals(top, TowerLinks.upperIdOf(middle));
        assertNull(TowerLinks.upperIdOf(top));
        assertNull(TowerLinks.supportIdOf(bottom));
    }

    @Test
    void unlinkingClearsBothDirectionsAndLeavesTheRest() {
        TowerLinks.link(middle, bottom, Level.OVERWORLD);
        TowerLinks.link(top, middle, Level.OVERWORLD);

        TowerLinks.unlink(middle);

        assertNull(TowerLinks.supportIdOf(middle));
        assertNull(TowerLinks.upperIdOf(bottom));
        // The zombie above keeps standing on the one that was released, like a rider does.
        assertEquals(middle, TowerLinks.supportIdOf(top));
    }

    @Test
    void relinkingMovesTheZombieInsteadOfLeavingAStaleEntry() {
        TowerLinks.link(top, middle, Level.OVERWORLD);
        TowerLinks.link(top, other, Level.OVERWORLD);

        assertEquals(other, TowerLinks.supportIdOf(top));
        assertEquals(top, TowerLinks.upperIdOf(other));
        assertNull(TowerLinks.upperIdOf(middle));
    }

    @Test
    void aSupportHoldsOnlyOneZombie() {
        TowerLinks.link(middle, bottom, Level.OVERWORLD);
        TowerLinks.link(other, bottom, Level.OVERWORLD);

        assertEquals(other, TowerLinks.upperIdOf(bottom));
        assertNull(TowerLinks.supportIdOf(middle));
    }

    @Test
    void unlinkingAnUnknownOrReplacedIdIsHarmless() {
        TowerLinks.link(middle, bottom, Level.OVERWORLD);
        TowerLinks.unlink(UUID.randomUUID());
        assertEquals(middle, TowerLinks.upperIdOf(bottom));

        TowerLinks.clear();
        assertTrue(TowerLinks.isEmpty());
    }

    @Test
    void linksAreListedPerDimension() {
        TowerLinks.link(middle, bottom, Level.OVERWORLD);
        TowerLinks.link(top, other, Level.NETHER);

        assertEquals(java.util.List.of(middle), TowerLinks.uppersIn(Level.OVERWORLD));
        assertEquals(java.util.List.of(top), TowerLinks.uppersIn(Level.NETHER));
    }
}
