package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;

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

    @Test
    void saveLoadRoundTripPreservesTrackedState() {
        StatisticsManager original = new StatisticsManager();
        UUID playerUuid = UUID.randomUUID();
        UUID offlineUuid = UUID.randomUUID();

        original.recordZombieClassKill(playerUuid, true);
        original.recordZombieClassKill(playerUuid, false);
        original.startDeathCooldown(playerUuid, 1000L, 5);
        original.queueAdvancementResets(List.of(playerUuid, offlineUuid));

        StatisticsManager loaded = StatisticsManager.load(original.save(new CompoundTag(), null), null);

        assertEquals(1, loaded.getKills(playerUuid));
        assertEquals(2, loaded.getMilestoneKills(playerUuid));
        assertEquals(0, loaded.pruneExpiredCooldowns(1099L));
        assertEquals(1, loaded.pruneExpiredCooldowns(1100L));
        assertTrue(loaded.consumePendingAdvancementReset(offlineUuid));
        assertTrue(loaded.consumePendingAdvancementReset(playerUuid));
    }

    @Test
    void loadSkipsMalformedListEntries() {
        UUID goodUuid = UUID.randomUUID();
        UUID clampedUuid = UUID.randomUUID();

        ListTag kills = new ListTag();
        CompoundTag good = new CompoundTag();
        good.putUUID("uuid", goodUuid);
        good.putInt("count", 5);
        CompoundTag missingUuid = new CompoundTag();
        missingUuid.putInt("count", 9);
        CompoundTag badUuidType = new CompoundTag();
        badUuidType.putString("uuid", "not-a-uuid");
        badUuidType.putInt("count", 7);
        CompoundTag negative = new CompoundTag();
        negative.putUUID("uuid", clampedUuid);
        negative.putInt("count", -3);
        kills.add(good);
        kills.add(missingUuid);
        kills.add(badUuidType);
        kills.add(negative);

        CompoundTag tag = new CompoundTag();
        tag.put("kills", kills);

        StatisticsManager loaded = StatisticsManager.load(tag, null);

        assertEquals(5, loaded.getKills(goodUuid));
        assertEquals(0, loaded.getKills(clampedUuid));
        assertEquals(2, loaded.getAllKills().size());
    }

    @Test
    void loadIgnoresNonListAndWrongElementTypeData() {
        CompoundTag wrongType = new CompoundTag();
        wrongType.putInt("kills", 42);
        assertEquals(0, StatisticsManager.load(wrongType, null).getAllKills().size());

        ListTag ints = new ListTag();
        ints.add(IntTag.valueOf(3));
        CompoundTag wrongElements = new CompoundTag();
        wrongElements.put("kills", ints);
        wrongElements.put("pendingAdvancementResets", ints);

        StatisticsManager loaded = StatisticsManager.load(wrongElements, null);

        assertEquals(0, loaded.getAllKills().size());
        assertFalse(loaded.consumePendingAdvancementReset(UUID.randomUUID()));
    }

    @Test
    void loadHandlesEmptyAndLegacySaves() {
        StatisticsManager loaded = StatisticsManager.load(new CompoundTag(), null);

        assertEquals(0, loaded.getAllKills().size());
        assertEquals(0, loaded.getTotalKills());
        assertFalse(loaded.consumePendingAdvancementReset(UUID.randomUUID()));
        assertEquals(0, loaded.pruneExpiredCooldowns(0L));
    }
}
