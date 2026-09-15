package com.rique.zombieapocalypse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;

class ApocalypseWorldDataTest {

    @Test
    void resetEventScheduleStateClearsActiveAndPendingEventFlags() {
        ApocalypseWorldData data = new ApocalypseWorldData();

        data.setHordeActive(true);
        data.setHordeEndGameTime(1200L);
        data.setLastHordeRollDay(12L);
        data.setBloodMoonActive(true);
        data.setBloodMoonNightDay(12L);
        data.setForcedBloodMoonPending(true);
        data.setLastDayAnnouncementDay(12L);

        data.resetEventScheduleState();

        assertFalse(data.isHordeActive());
        assertEquals(0L, data.getHordeEndGameTime());
        assertEquals(-1L, data.getLastHordeRollDay());
        assertFalse(data.isBloodMoonActive());
        assertEquals(-1L, data.getBloodMoonNightDay());
        assertFalse(data.isForcedBloodMoonPending());
        assertEquals(-1L, data.getLastDayAnnouncementDay());
    }

    @Test
    void saveLoadRoundTripPreservesEventState() {
        ApocalypseWorldData original = new ApocalypseWorldData();
        original.setHordeActive(true);
        original.setHordeEndGameTime(123456L);
        original.setLastHordeRollDay(9L);
        original.setBloodMoonActive(true);
        original.setBloodMoonNightDay(7L);
        original.setForcedBloodMoonPending(true);
        original.setLastDayAnnouncementDay(4L);

        ApocalypseWorldData loaded = ApocalypseWorldData.load(original.save(new CompoundTag(), null), null);

        assertTrue(loaded.isHordeActive());
        assertEquals(123456L, loaded.getHordeEndGameTime());
        assertEquals(9L, loaded.getLastHordeRollDay());
        assertTrue(loaded.isBloodMoonActive());
        assertEquals(7L, loaded.getBloodMoonNightDay());
        assertTrue(loaded.isForcedBloodMoonPending());
        assertEquals(4L, loaded.getLastDayAnnouncementDay());
    }

    @Test
    void loadAppliesDefaultsForMissingAndLegacyKeys() {
        ApocalypseWorldData loaded = ApocalypseWorldData.load(new CompoundTag(), null);

        assertFalse(loaded.isHordeActive());
        assertEquals(0L, loaded.getHordeEndGameTime());
        assertEquals(-1L, loaded.getLastHordeRollDay());
        assertFalse(loaded.isBloodMoonActive());
        assertEquals(-1L, loaded.getBloodMoonNightDay());
        assertFalse(loaded.isForcedBloodMoonPending());
        assertEquals(-1L, loaded.getLastDayAnnouncementDay());
    }

    @Test
    void loadToleratesWrongValueTypes() {
        CompoundTag tag = new CompoundTag();
        tag.putString("hordeActive", "yes");
        tag.putString("hordeEndGameTime", "not-a-number");
        tag.putString("lastHordeRollDay", "not-a-number");
        tag.putString("bloodMoonActive", "yes");
        tag.putString("bloodMoonNightDay", "not-a-number");
        tag.putString("forcedBloodMoonPending", "yes");
        tag.putString("lastDayAnnouncementDay", "not-a-number");

        ApocalypseWorldData loaded = ApocalypseWorldData.load(tag, null);

        assertFalse(loaded.isHordeActive());
        assertEquals(0L, loaded.getHordeEndGameTime());
        assertEquals(0L, loaded.getLastHordeRollDay());
        assertFalse(loaded.isBloodMoonActive());
        assertEquals(0L, loaded.getBloodMoonNightDay());
        assertFalse(loaded.isForcedBloodMoonPending());
        assertEquals(0L, loaded.getLastDayAnnouncementDay());
    }
}
