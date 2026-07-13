package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class StatisticsManagerTest {

    @Test
    void recordZombieClassKillKeepsMilestoneProgressWhenStatsAreDisabled() {
        StatisticsManager manager = new StatisticsManager();
        UUID playerUuid = UUID.randomUUID();

        StatisticsManager.KillUpdate killUpdate = manager.recordZombieClassKill(playerUuid, false);

        assertEquals(0, killUpdate.statisticsKills());
        assertEquals(1, killUpdate.milestoneKills());
        assertEquals(0, manager.getKills(playerUuid));
        assertEquals(1, manager.getMilestoneKills(playerUuid));
    }

    @Test
    void recordZombieClassKillUpdatesBothCountersWhenStatsAreEnabled() {
        StatisticsManager manager = new StatisticsManager();
        UUID playerUuid = UUID.randomUUID();

        StatisticsManager.KillUpdate killUpdate = manager.recordZombieClassKill(playerUuid, true);

        assertEquals(1, killUpdate.statisticsKills());
        assertEquals(1, killUpdate.milestoneKills());
        assertEquals(1, manager.getKills(playerUuid));
        assertEquals(1, manager.getMilestoneKills(playerUuid));
    }

    @Test
    void queuedAdvancementResetCanBeConsumed() {
        StatisticsManager manager = new StatisticsManager();
        UUID playerUuid = UUID.randomUUID();

        manager.queueAdvancementResets(List.of(playerUuid));

        assertTrue(manager.consumePendingAdvancementReset(playerUuid));
        assertFalse(manager.consumePendingAdvancementReset(playerUuid));
    }

    @Test
    void clearAllKeepsQueuedAdvancementResetsForOfflineSync() {
        StatisticsManager manager = new StatisticsManager();
        UUID playerUuid = UUID.randomUUID();

        manager.recordZombieClassKill(playerUuid, true);
        manager.queueAdvancementResets(List.of(playerUuid));
        manager.clearAll();

        assertEquals(0, manager.getKills(playerUuid));
        assertEquals(0, manager.getMilestoneKills(playerUuid));
        assertTrue(manager.consumePendingAdvancementReset(playerUuid));
    }

    @Test
    void killCountersSaturateInsteadOfWrappingNegative() {
        assertEquals(1, StatisticsManager.incrementKillCount(-10));
        assertEquals(42, StatisticsManager.incrementKillCount(41));
        assertEquals(Integer.MAX_VALUE, StatisticsManager.incrementKillCount(Integer.MAX_VALUE));
    }

    @Test
    void cooldownDisplayRoundsUpAndExpiredEntriesArePruned() {
        StatisticsManager manager = new StatisticsManager();
        UUID playerUuid = UUID.randomUUID();

        manager.startDeathCooldown(playerUuid, 100L, 5);

        assertEquals(5, StatisticsManager.remainingCooldownSeconds(200L, 100L));
        assertEquals(1, StatisticsManager.remainingCooldownSeconds(200L, 199L));
        assertEquals(1, manager.pruneExpiredCooldowns(200L));
        assertEquals(0, manager.pruneExpiredCooldowns(200L));
    }
}
