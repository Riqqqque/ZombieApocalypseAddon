package com.rique.zombieapocalypse;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Persistent world state for horde and blood moon systems.
 */
public class ApocalypseWorldData extends SavedData {

    private static final String DATA_NAME = ZombieApocalypseAddon.MODID + "_world_state";

    private boolean hordeActive;
    private long hordeEndGameTime;
    private long lastHordeRollDay = -1L;

    private boolean bloodMoonActive;
    private long bloodMoonNightDay = -1L;
    private boolean forcedBloodMoonPending;

    public static ApocalypseWorldData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(ApocalypseWorldData::new, ApocalypseWorldData::load),
                DATA_NAME);
    }

    public static ApocalypseWorldData load(CompoundTag tag, HolderLookup.Provider provider) {
        ApocalypseWorldData data = new ApocalypseWorldData();
        data.hordeActive = tag.getBoolean("hordeActive");
        data.hordeEndGameTime = tag.getLong("hordeEndGameTime");
        data.lastHordeRollDay = tag.contains("lastHordeRollDay") ? tag.getLong("lastHordeRollDay") : -1L;

        data.bloodMoonActive = tag.getBoolean("bloodMoonActive");
        data.bloodMoonNightDay = tag.contains("bloodMoonNightDay") ? tag.getLong("bloodMoonNightDay") : -1L;
        data.forcedBloodMoonPending = tag.getBoolean("forcedBloodMoonPending");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putBoolean("hordeActive", hordeActive);
        tag.putLong("hordeEndGameTime", hordeEndGameTime);
        tag.putLong("lastHordeRollDay", lastHordeRollDay);

        tag.putBoolean("bloodMoonActive", bloodMoonActive);
        tag.putLong("bloodMoonNightDay", bloodMoonNightDay);
        tag.putBoolean("forcedBloodMoonPending", forcedBloodMoonPending);
        return tag;
    }

    public boolean isHordeActive() {
        return hordeActive;
    }

    public void setHordeActive(boolean hordeActive) {
        this.hordeActive = hordeActive;
        setDirty();
    }

    public long getHordeEndGameTime() {
        return hordeEndGameTime;
    }

    public void setHordeEndGameTime(long hordeEndGameTime) {
        this.hordeEndGameTime = hordeEndGameTime;
        setDirty();
    }

    public long getLastHordeRollDay() {
        return lastHordeRollDay;
    }

    public void setLastHordeRollDay(long lastHordeRollDay) {
        this.lastHordeRollDay = lastHordeRollDay;
        setDirty();
    }

    public boolean isBloodMoonActive() {
        return bloodMoonActive;
    }

    public void setBloodMoonActive(boolean bloodMoonActive) {
        this.bloodMoonActive = bloodMoonActive;
        setDirty();
    }

    public long getBloodMoonNightDay() {
        return bloodMoonNightDay;
    }

    public void setBloodMoonNightDay(long bloodMoonNightDay) {
        this.bloodMoonNightDay = bloodMoonNightDay;
        setDirty();
    }

    public boolean isForcedBloodMoonPending() {
        return forcedBloodMoonPending;
    }

    public void setForcedBloodMoonPending(boolean forcedBloodMoonPending) {
        this.forcedBloodMoonPending = forcedBloodMoonPending;
        setDirty();
    }
}
