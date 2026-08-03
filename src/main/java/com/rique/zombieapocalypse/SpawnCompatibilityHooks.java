package com.rique.zombieapocalypse;

import javax.annotation.Nullable;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

public final class SpawnCompatibilityHooks {

    private SpawnCompatibilityHooks() {
    }

    public static boolean isPositionAllowed(
            Mob mob,
            ServerLevelAccessor level,
            MobSpawnType spawnType) {
        if (!Config.COMMON.respectExternalSpawnRules.get()) {
            return true;
        }

        MobSpawnEvent.SpawnPlacementCheck placementEvent = new MobSpawnEvent.SpawnPlacementCheck(
                mob.getType(),
                level,
                spawnType,
                mob.blockPosition(),
                level.getRandom(),
                true);
        NeoForge.EVENT_BUS.post(placementEvent);
        if (placementEvent.getResult() == MobSpawnEvent.SpawnPlacementCheck.Result.FAIL) {
            return false;
        }

        MobSpawnEvent.PositionCheck event = new MobSpawnEvent.PositionCheck(mob, level, spawnType, null);
        NeoForge.EVENT_BUS.post(event);
        return event.getResult() != MobSpawnEvent.PositionCheck.Result.FAIL;
    }

    @Nullable
    public static SpawnGroupData finalizeSpawn(
            Mob mob,
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnType,
            @Nullable SpawnGroupData spawnData) {
        return EventHooks.finalizeMobSpawn(mob, level, difficulty, spawnType, spawnData);
    }
}
