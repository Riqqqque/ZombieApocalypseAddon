package com.rique.zombieapocalypse;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Horde and blood moon state machine.
 *
 * State is persisted in {@link ApocalypseWorldData} and ticked from a single
 * server time source (overworld level tick).
 */
public final class HordeManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Snapshot of event state for a single tick, avoiding repeated
     * {@link ApocalypseWorldData#get} lookups.
     */
    public record EventState(boolean hordeActive, boolean bloodMoonActive,
                              double spawnMultiplier, int zombiesPerSpawn) {
    }

    private HordeManager() {
    }

    public static EventState getEventState(ServerLevel level) {
        ApocalypseWorldData state = ApocalypseWorldData.get(level.getServer());

        boolean hordeActive = state.isHordeActive();
        boolean bloodMoonActive = state.isBloodMoonActive();

        double spawnMultiplier = 1.0;
        if (hordeActive) {
            spawnMultiplier *= Config.COMMON.hordeSpawnMultiplier.get();
        }
        if (bloodMoonActive) {
            spawnMultiplier *= Config.COMMON.bloodMoonSpawnMultiplier.get();
        }

        int zombiesPerSpawn;
        if (hordeActive) {
            zombiesPerSpawn = Config.COMMON.hordeZombiesPerSpawn.get();
        } else if (bloodMoonActive) {
            zombiesPerSpawn = Config.COMMON.bloodMoonZombiesPerSpawn.get();
        } else {
            zombiesPerSpawn = Config.COMMON.zombiesPerSpawn.get();
        }

        return new EventState(hordeActive, bloodMoonActive, spawnMultiplier, zombiesPerSpawn);
    }

    public static void tick(ServerLevel overworldLevel) {
        ApocalypseWorldData state = ApocalypseWorldData.get(overworldLevel.getServer());

        long gameTime = overworldLevel.getGameTime();
        long dayTime = overworldLevel.getDayTime() % 24000L;
        long currentDay = overworldLevel.getDayTime() / 24000L;

        expireHordeIfNeeded(overworldLevel, state, gameTime);
        tryStartScheduledHorde(overworldLevel, state, currentDay, dayTime);
        updateBloodMoon(overworldLevel, state, currentDay, dayTime);
    }

    private static void expireHordeIfNeeded(ServerLevel level, ApocalypseWorldData state, long gameTime) {
        if (!state.isHordeActive() || gameTime < state.getHordeEndGameTime()) {
            return;
        }

        state.setHordeActive(false);
        state.setHordeEndGameTime(0L);
        notifyAllPlayers(level, "Horde Ended", "The zombie horde has dispersed.");

        if (Config.COMMON.enableDebugLogging.get()) {
            LOGGER.info("[ZombieApocalypse] Horde ended");
        }
    }

    private static void tryStartScheduledHorde(ServerLevel level, ApocalypseWorldData state, long currentDay, long dayTime) {
        if (!Config.COMMON.enableHordeEvents.get() || state.isHordeActive()) {
            return;
        }

        if (!EventSchedule.isHordeRollWindow(dayTime)) {
            return;
        }

        long lastRollDay = state.getLastHordeRollDay();
        if (lastRollDay == currentDay) {
            return;
        }

        state.setLastHordeRollDay(currentDay);

        if (isScheduledHordeBlockedByGrace(currentDay, Config.COMMON.daylightSpawnStartDay.get())) {
            return;
        }

        int intervalDays = Math.max(1, Config.COMMON.hordeIntervalDays.get());
        if (!EventSchedule.shouldRollHorde(currentDay, dayTime, lastRollDay, intervalDays)) {
            return;
        }

        double chance = ConfigValidator.probability(Config.COMMON.hordeStartChance.get());
        if (level.getRandom().nextDouble() < chance) {
            startHorde(level);
        }
    }

    private static void updateBloodMoon(ServerLevel level, ApocalypseWorldData state, long currentDay, long dayTime) {
        boolean isNight = EventSchedule.isNight(dayTime);

        if (!isNight) {
            if (state.isBloodMoonActive()) {
                state.setBloodMoonActive(false);
                notifyAllPlayers(level, "Dawn Breaks", "The blood moon fades.");
            }
            return;
        }

        if (state.getBloodMoonNightDay() == currentDay) {
            return;
        }

        state.setBloodMoonNightDay(currentDay);

        boolean activateBloodMoon = false;
        if (state.isForcedBloodMoonPending()) {
            activateBloodMoon = true;
            state.setForcedBloodMoonPending(false);
        } else if (Config.COMMON.enableBloodMoon.get()) {
            double chance = ConfigValidator.probability(Config.COMMON.bloodMoonChance.get());
            activateBloodMoon = level.getRandom().nextDouble() < chance;
        }

        state.setBloodMoonActive(activateBloodMoon);

        if (activateBloodMoon) {
            notifyAllPlayers(level, "BLOOD MOON", "Zombies are swarming tonight.");
            if (Config.COMMON.enableDebugLogging.get()) {
                LOGGER.info("[ZombieApocalypse] Blood moon started for night {}", currentDay);
            }
        }
    }

    private static void notifyAllPlayers(ServerLevel level, String title, String subtitle) {
        if (!Config.COMMON.enableEventNotifications.get()) {
            return;
        }

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)));
        }
    }

    public static void startHorde(ServerLevel level) {
        ApocalypseWorldData state = ApocalypseWorldData.get(level.getServer());
        int durationMinutes = Math.max(1, Config.COMMON.hordeDurationMinutes.get());

        state.setHordeActive(true);
        state.setHordeEndGameTime(level.getGameTime() + (durationMinutes * 60L * 20L));

        notifyAllPlayers(level, "HORDE INCOMING", "Zombie waves for " + durationMinutes + " minutes.");

        if (Config.COMMON.enableDebugLogging.get()) {
            LOGGER.info("[ZombieApocalypse] Horde started; duration={} minutes", durationMinutes);
        }
    }

    public static void stopHorde(ServerLevel level) {
        ApocalypseWorldData state = ApocalypseWorldData.get(level.getServer());
        state.setHordeActive(false);
        state.setHordeEndGameTime(0L);

        if (Config.COMMON.enableDebugLogging.get()) {
            LOGGER.info("[ZombieApocalypse] Horde stopped by command");
        }
    }

    public static void triggerBloodMoon(ServerLevel level) {
        ApocalypseWorldData state = ApocalypseWorldData.get(level.getServer());
        long dayTime = level.getDayTime() % 24000L;
        long currentDay = level.getDayTime() / 24000L;

        if (EventSchedule.isNight(dayTime)) {
            boolean alreadyActive = state.isBloodMoonActive();
            state.setBloodMoonNightDay(currentDay);
            state.setForcedBloodMoonPending(false);
            state.setBloodMoonActive(true);
            if (!alreadyActive) {
                notifyAllPlayers(level, "BLOOD MOON", "Zombies are swarming tonight.");
            }
            return;
        }

        state.setForcedBloodMoonPending(true);
    }

    public static boolean isHordeActive(ServerLevel level) {
        return ApocalypseWorldData.get(level.getServer()).isHordeActive();
    }

    public static boolean isBloodMoonActive(ServerLevel level) {
        return ApocalypseWorldData.get(level.getServer()).isBloodMoonActive();
    }

    public static boolean isBloodMoonForced(ServerLevel level) {
        return ApocalypseWorldData.get(level.getServer()).isForcedBloodMoonPending();
    }

    public static long getHordeRemainingSeconds(ServerLevel level) {
        ApocalypseWorldData state = ApocalypseWorldData.get(level.getServer());
        if (!state.isHordeActive()) {
            return 0L;
        }

        long remaining = (state.getHordeEndGameTime() - level.getGameTime()) / 20L;
        return Math.max(0L, remaining);
    }

    public static double getSpawnMultiplier(ServerLevel level) {
        ApocalypseWorldData state = ApocalypseWorldData.get(level.getServer());
        double multiplier = 1.0;

        if (state.isHordeActive()) {
            multiplier *= Config.COMMON.hordeSpawnMultiplier.get();
        }

        if (state.isBloodMoonActive()) {
            multiplier *= Config.COMMON.bloodMoonSpawnMultiplier.get();
        }

        return multiplier;
    }

    public static int getZombiesPerSpawn(ServerLevel level) {
        ApocalypseWorldData state = ApocalypseWorldData.get(level.getServer());

        if (state.isHordeActive()) {
            return Config.COMMON.hordeZombiesPerSpawn.get();
        }

        if (state.isBloodMoonActive()) {
            return Config.COMMON.bloodMoonZombiesPerSpawn.get();
        }

        return Config.COMMON.zombiesPerSpawn.get();
    }

    public static int getEventSpawnInterval() {
        return Math.max(1, Config.COMMON.eventSpawnInterval.get());
    }

    static boolean isScheduledHordeBlockedByGrace(long currentDay, int daylightSpawnStartDay) {
        return currentDay < Math.max(0, daylightSpawnStartDay);
    }
}
