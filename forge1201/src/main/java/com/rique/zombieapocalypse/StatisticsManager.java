package com.rique.zombieapocalypse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Persistent per-server player statistics and post-death spawn cooldown state.
 */
public class StatisticsManager extends SavedData {

    private static final String DATA_NAME = ZombieApocalypseAddon.MODID + "_statistics";

    private static final String KILLS_KEY = "kills";
    private static final String MILESTONE_KILLS_KEY = "milestoneKills";
    private static final String COOLDOWNS_KEY = "cooldowns";
    private static final String PENDING_ADVANCEMENT_RESETS_KEY = "pendingAdvancementResets";

    private final Map<UUID, Integer> playerKills = new HashMap<>();
    private final Map<UUID, Integer> milestoneKills = new HashMap<>();
    private final Map<UUID, Long> deathCooldowns = new HashMap<>();
    private final Set<UUID> pendingAdvancementResets = new HashSet<>();

    public static StatisticsManager load(CompoundTag tag) {
        StatisticsManager manager = new StatisticsManager();

        if (tag.contains(KILLS_KEY, Tag.TAG_LIST)) {
            ListTag killsList = tag.getList(KILLS_KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < killsList.size(); i++) {
                CompoundTag entry = killsList.getCompound(i);
                if (entry.hasUUID("uuid")) {
                    manager.playerKills.put(entry.getUUID("uuid"), entry.getInt("count"));
                }
            }
        }

        if (tag.contains(MILESTONE_KILLS_KEY, Tag.TAG_LIST)) {
            ListTag milestoneList = tag.getList(MILESTONE_KILLS_KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < milestoneList.size(); i++) {
                CompoundTag entry = milestoneList.getCompound(i);
                if (entry.hasUUID("uuid")) {
                    manager.milestoneKills.put(entry.getUUID("uuid"), entry.getInt("count"));
                }
            }
        }

        if (tag.contains(COOLDOWNS_KEY, Tag.TAG_LIST)) {
            ListTag cooldownList = tag.getList(COOLDOWNS_KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < cooldownList.size(); i++) {
                CompoundTag entry = cooldownList.getCompound(i);
                if (entry.hasUUID("uuid")) {
                    manager.deathCooldowns.put(entry.getUUID("uuid"), entry.getLong("endGameTime"));
                }
            }
        }

        if (tag.contains(PENDING_ADVANCEMENT_RESETS_KEY, Tag.TAG_LIST)) {
            ListTag pendingList = tag.getList(PENDING_ADVANCEMENT_RESETS_KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < pendingList.size(); i++) {
                CompoundTag entry = pendingList.getCompound(i);
                if (entry.hasUUID("uuid")) {
                    manager.pendingAdvancementResets.add(entry.getUUID("uuid"));
                }
            }
        }

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag killsList = new ListTag();
        for (Map.Entry<UUID, Integer> entry : playerKills.entrySet()) {
            CompoundTag killEntry = new CompoundTag();
            killEntry.putUUID("uuid", entry.getKey());
            killEntry.putInt("count", entry.getValue());
            killsList.add(killEntry);
        }
        tag.put(KILLS_KEY, killsList);

        ListTag milestoneList = new ListTag();
        for (Map.Entry<UUID, Integer> entry : milestoneKills.entrySet()) {
            CompoundTag milestoneEntry = new CompoundTag();
            milestoneEntry.putUUID("uuid", entry.getKey());
            milestoneEntry.putInt("count", entry.getValue());
            milestoneList.add(milestoneEntry);
        }
        tag.put(MILESTONE_KILLS_KEY, milestoneList);

        ListTag cooldownList = new ListTag();
        for (Map.Entry<UUID, Long> entry : deathCooldowns.entrySet()) {
            CompoundTag cooldownEntry = new CompoundTag();
            cooldownEntry.putUUID("uuid", entry.getKey());
            cooldownEntry.putLong("endGameTime", entry.getValue());
            cooldownList.add(cooldownEntry);
        }
        tag.put(COOLDOWNS_KEY, cooldownList);

        ListTag pendingList = new ListTag();
        for (UUID uuid : pendingAdvancementResets) {
            CompoundTag pendingEntry = new CompoundTag();
            pendingEntry.putUUID("uuid", uuid);
            pendingList.add(pendingEntry);
        }
        tag.put(PENDING_ADVANCEMENT_RESETS_KEY, pendingList);

        return tag;
    }

    public static StatisticsManager get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                StatisticsManager::load,
                StatisticsManager::new,
                DATA_NAME);
    }

    public record KillUpdate(int statisticsKills, int milestoneKills) {
    }

    public KillUpdate recordZombieClassKill(UUID playerUuid) {
        return recordZombieClassKill(playerUuid, Config.COMMON.enableStatistics.get());
    }

    KillUpdate recordZombieClassKill(UUID playerUuid, boolean trackStatistics) {
        int statsKills = playerKills.getOrDefault(playerUuid, 0);
        if (trackStatistics) {
            statsKills = playerKills.merge(playerUuid, 1, Integer::sum);
        }

        int totalMilestoneKills = milestoneKills.merge(playerUuid, 1, Integer::sum);
        setDirty();
        return new KillUpdate(statsKills, totalMilestoneKills);
    }

    public int getKills(UUID playerUuid) {
        return playerKills.getOrDefault(playerUuid, 0);
    }

    int getMilestoneKills(UUID playerUuid) {
        return milestoneKills.getOrDefault(playerUuid, 0);
    }

    public Map<UUID, Integer> getAllKills() {
        return new HashMap<>(playerKills);
    }

    public Set<UUID> getPlayersWithTrackedProgress() {
        Set<UUID> trackedPlayers = new HashSet<>(playerKills.keySet());
        trackedPlayers.addAll(milestoneKills.keySet());
        return trackedPlayers;
    }

    public int getTotalKills() {
        return playerKills.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void queueAdvancementResets(Iterable<UUID> playerUuids) {
        boolean changed = false;
        for (UUID playerUuid : playerUuids) {
            if (pendingAdvancementResets.add(playerUuid)) {
                changed = true;
            }
        }

        if (changed) {
            setDirty();
        }
    }

    public boolean consumePendingAdvancementReset(UUID playerUuid) {
        if (!pendingAdvancementResets.remove(playerUuid)) {
            return false;
        }

        setDirty();
        return true;
    }

    public void startDeathCooldown(UUID playerUuid, long gameTime) {
        if (!Config.COMMON.enableDeathCooldown.get()) {
            return;
        }

        int cooldownSeconds = Config.COMMON.deathCooldownSeconds.get();
        long cooldownEndTime = gameTime + (cooldownSeconds * 20L);
        deathCooldowns.put(playerUuid, cooldownEndTime);
        setDirty();
    }

    public boolean isInCooldown(UUID playerUuid, long gameTime) {
        if (!Config.COMMON.enableDeathCooldown.get()) {
            return false;
        }

        Long endTime = deathCooldowns.get(playerUuid);
        if (endTime == null) {
            return false;
        }

        if (gameTime >= endTime) {
            deathCooldowns.remove(playerUuid);
            setDirty();
            return false;
        }

        return true;
    }

    public int getRemainingCooldown(UUID playerUuid, long gameTime) {
        if (!isInCooldown(playerUuid, gameTime)) {
            return 0;
        }

        Long endTime = deathCooldowns.get(playerUuid);
        if (endTime == null) {
            return 0;
        }

        long remaining = (endTime - gameTime) / 20L;
        return (int) Math.max(0L, remaining);
    }

    public double getSpawnFactor(UUID playerUuid, long gameTime) {
        if (!isInCooldown(playerUuid, gameTime)) {
            return 1.0;
        }

        return 1.0 - SpawnMath.clampProbability(Config.COMMON.cooldownSpawnReduction.get());
    }

    public void clearAll() {
        playerKills.clear();
        milestoneKills.clear();
        deathCooldowns.clear();
        setDirty();
    }
}
