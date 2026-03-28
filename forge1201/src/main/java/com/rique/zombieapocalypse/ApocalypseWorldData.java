package com.rique.zombieapocalypse;

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
    private long lastDayAnnouncementDay = -1L;

    public static ApocalypseWorldData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                ApocalypseWorldData::load,
                ApocalypseWorldData::new,
                DATA_NAME);
    }

    public static ApocalypseWorldData load(CompoundTag tag) {
        ApocalypseWorldData data = new ApocalypseWorldData();
        data.hordeActive = tag.getBoolean("hordeActive");
        data.hordeEndGameTime = tag.getLong("hordeEndGameTime");
        data.lastHordeRollDay = tag.contains("lastHordeRollDay") ? tag.getLong("lastHordeRollDay") : -1L;

        data.bloodMoonActive = tag.getBoolean("bloodMoonActive");
        data.bloodMoonNightDay = tag.contains("bloodMoonNightDay") ? tag.getLong("bloodMoonNightDay") : -1L;
        data.forcedBloodMoonPending = tag.getBoolean("forcedBloodMoonPending");
        data.lastDayAnnouncementDay = tag.contains("lastDayAnnouncementDay") ? tag.getLong("lastDayAnnouncementDay") : -1L;
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("hordeActive", hordeActive);
        tag.putLong("hordeEndGameTime", hordeEndGameTime);
        tag.putLong("lastHordeRollDay", lastHordeRollDay);

        tag.putBoolean("bloodMoonActive", bloodMoonActive);
        tag.putLong("bloodMoonNightDay", bloodMoonNightDay);
        tag.putBoolean("forcedBloodMoonPending", forcedBloodMoonPending);
        tag.putLong("lastDayAnnouncementDay", lastDayAnnouncementDay);
        return tag;
    }

    public boolean isHordeActive() {
        return hordeActive;
    }

    public void setHordeActive(boolean hordeActive) {
        if (this.hordeActive == hordeActive) return;
        this.hordeActive = hordeActive;
        setDirty();
    }

    public long getHordeEndGameTime() {
        return hordeEndGameTime;
    }

    public void setHordeEndGameTime(long hordeEndGameTime) {
        if (this.hordeEndGameTime == hordeEndGameTime) return;
        this.hordeEndGameTime = hordeEndGameTime;
        setDirty();
    }

    public long getLastHordeRollDay() {
        return lastHordeRollDay;
    }

    public void setLastHordeRollDay(long lastHordeRollDay) {
        if (this.lastHordeRollDay == lastHordeRollDay) return;
        this.lastHordeRollDay = lastHordeRollDay;
        setDirty();
    }

    public boolean isBloodMoonActive() {
        return bloodMoonActive;
    }

    public void setBloodMoonActive(boolean bloodMoonActive) {
        if (this.bloodMoonActive == bloodMoonActive) return;
        this.bloodMoonActive = bloodMoonActive;
        setDirty();
    }

    public long getBloodMoonNightDay() {
        return bloodMoonNightDay;
    }

    public void setBloodMoonNightDay(long bloodMoonNightDay) {
        if (this.bloodMoonNightDay == bloodMoonNightDay) return;
        this.bloodMoonNightDay = bloodMoonNightDay;
        setDirty();
    }

    public boolean isForcedBloodMoonPending() {
        return forcedBloodMoonPending;
    }

    public void setForcedBloodMoonPending(boolean forcedBloodMoonPending) {
        if (this.forcedBloodMoonPending == forcedBloodMoonPending) return;
        this.forcedBloodMoonPending = forcedBloodMoonPending;
        setDirty();
    }

    public long getLastDayAnnouncementDay() {
        return lastDayAnnouncementDay;
    }

    public void setLastDayAnnouncementDay(long lastDayAnnouncementDay) {
        if (this.lastDayAnnouncementDay == lastDayAnnouncementDay) return;
        this.lastDayAnnouncementDay = lastDayAnnouncementDay;
        setDirty();
    }

    public void resetEventScheduleState() {
        boolean changed = false;

        if (hordeActive) {
            hordeActive = false;
            changed = true;
        }
        if (hordeEndGameTime != 0L) {
            hordeEndGameTime = 0L;
            changed = true;
        }
        if (lastHordeRollDay != -1L) {
            lastHordeRollDay = -1L;
            changed = true;
        }

        if (bloodMoonActive) {
            bloodMoonActive = false;
            changed = true;
        }
        if (bloodMoonNightDay != -1L) {
            bloodMoonNightDay = -1L;
            changed = true;
        }
        if (forcedBloodMoonPending) {
            forcedBloodMoonPending = false;
            changed = true;
        }
        if (lastDayAnnouncementDay != -1L) {
            lastDayAnnouncementDay = -1L;
            changed = true;
        }

        if (changed) {
            setDirty();
        }
    }
}
