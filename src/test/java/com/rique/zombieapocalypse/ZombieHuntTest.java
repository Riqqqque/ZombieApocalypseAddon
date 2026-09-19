package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ZombieHuntTest {

    @Test
    void bonusRollCountScalesWithBoostOverBaseHealthRatio() {
        assertEquals(1, ZombieHunt.bonusRollCount(20.0, 20.0, 1.0));
        assertEquals(2, ZombieHunt.bonusRollCount(40.0, 20.0, 1.0));
        assertEquals(0, ZombieHunt.bonusRollCount(19.9, 20.0, 1.0));
        assertEquals(1, ZombieHunt.bonusRollCount(40.0, 20.0, 2.0));
        assertEquals(5, ZombieHunt.bonusRollCount(100.0, 20.0, 1.0));
    }

    @Test
    void bonusRollCountHandlesDegenerateInputs() {
        assertEquals(0, ZombieHunt.bonusRollCount(0.0, 20.0, 1.0));
        assertEquals(0, ZombieHunt.bonusRollCount(10.0, 0.0, 1.0));
        assertEquals(0, ZombieHunt.bonusRollCount(10.0, -5.0, 1.0));
        assertEquals(0, ZombieHunt.bonusRollCount(5.0, 20.0, 1.0));
    }
}
