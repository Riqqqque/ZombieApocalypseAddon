package com.rique.zombieapocalypse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.HolderLookup;
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
    private static final String COOLDOWNS_KEY = "cooldowns";

    private final Map<UUID, Integer> playerKills = new HashMap<>();
    private final Map<UUID, Long> deathCooldowns = new HashMap<>();

    public static StatisticsManager load(CompoundTag tag, HolderLookup.Provider provider) {
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

        if (tag.contains(COOLDOWNS_KEY, Tag.TAG_LIST)) {
            ListTag cooldownList = tag.getList(COOLDOWNS_KEY, Tag.TAG_COMPOUND);
            for (int i = 0; i < cooldownList.size(); i++) {
                CompoundTag entry = cooldownList.getCompound(i);
                if (entry.hasUUID("uuid")) {
                    manager.deathCooldowns.put(entry.getUUID("uuid"), entry.getLong("endGameTime"));
                }
            }
        }

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag killsList = new ListTag();
        for (Map.Entry<UUID, Integer> entry : playerKills.entrySet()) {
            CompoundTag killEntry = new CompoundTag();
            killEntry.putUUID("uuid", entry.getKey());
            killEntry.putInt("count", entry.getValue());
            killsList.add(killEntry);
        }
        tag.put(KILLS_KEY, killsList);

        ListTag cooldownList = new ListTag();
        for (Map.Entry<UUID, Long> entry : deathCooldowns.entrySet()) {
            CompoundTag cooldownEntry = new CompoundTag();
            cooldownEntry.putUUID("uuid", entry.getKey());
            cooldownEntry.putLong("endGameTime", entry.getValue());
            cooldownList.add(cooldownEntry);
        }
        tag.put(COOLDOWNS_KEY, cooldownList);

        return tag;
    }

    public static StatisticsManager get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new Factory<>(StatisticsManager::new, StatisticsManager::load),
                DATA_NAME);
    }

    public void addKill(UUID playerUuid) {
        if (!Config.COMMON.enableStatistics.get()) {
            return;
        }

        playerKills.merge(playerUuid, 1, Integer::sum);
        setDirty();
    }

    public int getKills(UUID playerUuid) {
        return playerKills.getOrDefault(playerUuid, 0);
    }

    public Map<UUID, Integer> getAllKills() {
        return new HashMap<>(playerKills);
    }

    public int getTotalKills() {
        return playerKills.values().stream().mapToInt(Integer::intValue).sum();
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
        deathCooldowns.clear();
        setDirty();
    }
}
