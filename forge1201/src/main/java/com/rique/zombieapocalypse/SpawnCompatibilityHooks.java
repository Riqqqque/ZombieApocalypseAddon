package com.rique.zombieapocalypse;

import javax.annotation.Nullable;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;

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
        MinecraftForge.EVENT_BUS.post(placementEvent);
        if (placementEvent.getResult() == Event.Result.DENY) {
            return false;
        }

        MobSpawnEvent.PositionCheck event = new MobSpawnEvent.PositionCheck(mob, level, spawnType, null);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult() != Event.Result.DENY;
    }

    @Nullable
    public static SpawnGroupData finalizeSpawn(
            Mob mob,
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnType,
            @Nullable SpawnGroupData spawnData) {
        return ForgeEventFactory.onFinalizeSpawn(mob, level, difficulty, spawnType, spawnData, null);
    }
}
