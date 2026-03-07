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
}
