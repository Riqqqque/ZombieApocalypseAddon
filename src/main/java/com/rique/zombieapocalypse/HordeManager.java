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
    private static final String MORNING_DAY_SUBTITLE = "A new day begins.";

    private enum BloodMoonTransition {
        NONE,
        STARTED,
        ENDED
    }

    private enum HordeTransition {
        NONE,
        STARTED,
        ENDED
    }

    public enum HordeStartResult {
        STARTED,
        CUSTOM_SPAWNING_DISABLED,
        DAYTIME_SPAWNING_DISABLED
    }

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
        if (!Config.COMMON.enableDaySpawning.get()) {
            return new EventState(false, false, 1.0, Config.COMMON.zombiesPerSpawn.get());
        }

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

        int zombiesPerSpawn = selectEventWaveSize(
                hordeActive,
                bloodMoonActive,
                Config.COMMON.zombiesPerSpawn.get(),
                Config.COMMON.hordeZombiesPerSpawn.get(),
                Config.COMMON.bloodMoonZombiesPerSpawn.get());

        return new EventState(hordeActive, bloodMoonActive, spawnMultiplier, zombiesPerSpawn);
    }

    public static void tick(ServerLevel overworldLevel) {
        ApocalypseWorldData state = ApocalypseWorldData.get(overworldLevel.getServer());

        long gameTime = overworldLevel.getGameTime();
        long absoluteDayTime = overworldLevel.getDayTime();
        long dayTime = absoluteDayTime % 24000L;
        long currentDay = absoluteDayTime / 24000L;

        if (!Config.COMMON.enableDaySpawning.get()) {
            stopSpawnEvents(state, currentDay, dayTime);
            notifyDayTransitions(
                    overworldLevel,
                    state,
                    currentDay,
                    dayTime,
                    HordeTransition.NONE,
                    BloodMoonTransition.NONE);
            return;
        }

        HordeTransition hordeTransition = updateHorde(overworldLevel, state, gameTime, currentDay, dayTime);
        BloodMoonTransition bloodMoonTransition = updateBloodMoon(overworldLevel, state, currentDay, dayTime);
        notifyDayTransitions(overworldLevel, state, currentDay, dayTime, hordeTransition, bloodMoonTransition);
    }

    private static HordeTransition updateHorde(
            ServerLevel level,
            ApocalypseWorldData state,
            long gameTime,
            long currentDay,
            long dayTime) {
        if (expireHordeIfNeeded(state, gameTime)) {
            // A horde that reaches dawn consumes that day's roll. Otherwise it can
            // immediately restart during the same five-second scheduling window.
            if (shouldConsumeScheduledHordeRollAfterEnd(dayTime)) {
                state.setLastHordeRollDay(currentDay);
            }
            return HordeTransition.ENDED;
        }

        return tryStartScheduledHorde(level, state, currentDay, dayTime)
                ? HordeTransition.STARTED
                : HordeTransition.NONE;
    }

    private static boolean expireHordeIfNeeded(ApocalypseWorldData state, long gameTime) {
        if (!state.isHordeActive() || gameTime < state.getHordeEndGameTime()) {
            return false;
        }

        state.setHordeActive(false);
        state.setHordeEndGameTime(0L);

        if (Config.COMMON.enableDebugLogging.get()) {
            LOGGER.info("[ZombieApocalypse] Horde ended");
        }
        return true;
    }

    private static boolean tryStartScheduledHorde(ServerLevel level, ApocalypseWorldData state, long currentDay, long dayTime) {
        if (!Config.COMMON.enableDaySpawning.get()
                || !Config.COMMON.enableHordeEvents.get()
                || state.isHordeActive()) {
            return false;
        }

        if (!EventSchedule.isHordeRollWindow(dayTime)) {
            return false;
        }

        long lastRollDay = state.getLastHordeRollDay();
        if (lastRollDay == currentDay) {
            return false;
        }

        if (isScheduledHordeBlocked(
                Config.COMMON.enableDaytimeSpawning.get(),
                currentDay,
                Config.COMMON.daylightSpawnStartDay.get())) {
            return false;
        }

        state.setLastHordeRollDay(currentDay);

        int intervalDays = Math.max(1, Config.COMMON.hordeIntervalDays.get());
        if (!EventSchedule.shouldRollHorde(currentDay, dayTime, lastRollDay, intervalDays)) {
            return false;
        }

        double chance = ConfigValidator.probability(Config.COMMON.hordeStartChance.get());
        if (level.getRandom().nextDouble() < chance) {
            activateHorde(level, state);
            return true;
        }

        return false;
    }

    private static BloodMoonTransition updateBloodMoon(ServerLevel level, ApocalypseWorldData state, long currentDay, long dayTime) {
        boolean isNight = EventSchedule.isNight(dayTime);

        if (!isNight) {
            if (state.isBloodMoonActive()) {
                state.setBloodMoonActive(false);
                return BloodMoonTransition.ENDED;
            }
            return BloodMoonTransition.NONE;
        }

        if (state.getBloodMoonNightDay() == currentDay) {
            return BloodMoonTransition.NONE;
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
        if (!activateBloodMoon) {
            return BloodMoonTransition.NONE;
        }

        if (Config.COMMON.enableDebugLogging.get()) {
            LOGGER.info("[ZombieApocalypse] Blood moon started for night {}", currentDay);
        }
        return BloodMoonTransition.STARTED;
    }

    static void stopSpawnEvents(ApocalypseWorldData state, long currentDay, long dayTime) {
        if (state.isHordeActive()) {
            state.setHordeActive(false);
            if (shouldConsumeScheduledHordeRollAfterEnd(dayTime)) {
                state.setLastHordeRollDay(currentDay);
            }
        }
        state.setHordeEndGameTime(0L);
        state.setBloodMoonActive(false);
        state.setForcedBloodMoonPending(false);
    }

    private static void sendTitleToAllPlayers(ServerLevel level, String title, String subtitle) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(title)));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)));
        }
    }

    private static void notifyAllPlayers(ServerLevel level, String title, String subtitle) {
        if (!Config.COMMON.enableEventNotifications.get()) {
            return;
        }

        sendTitleToAllPlayers(level, title, subtitle);
    }

    private static void notifyDayTransitions(
            ServerLevel level,
            ApocalypseWorldData state,
            long currentDay,
            long dayTime,
            HordeTransition hordeTransition,
            BloodMoonTransition bloodMoonTransition) {
        if (bloodMoonTransition == BloodMoonTransition.STARTED) {
            notifyAllPlayers(level, "BLOOD MOON", "Zombies are swarming tonight.");
            return;
        }

        boolean hordeStarted = hordeTransition == HordeTransition.STARTED;
        boolean hordeEnded = hordeTransition == HordeTransition.ENDED;
        boolean bloodMoonEnded = bloodMoonTransition == BloodMoonTransition.ENDED;

        if (!isDayAnnouncementWindow(dayTime)) {
            if (hordeEnded || bloodMoonEnded) {
                notifyEndedEvents(level, hordeEnded, bloodMoonEnded, false, currentDay);
            }
            return;
        }

        boolean dayCounterEnabled = Config.COMMON.enableDayCounterAnnouncements.get();
        boolean eventNotificationsEnabled = Config.COMMON.enableEventNotifications.get();
        boolean shouldAnnounceDay = shouldAnnounceDay(currentDay, dayTime, state.getLastDayAnnouncementDay(), dayCounterEnabled);

        if (eventNotificationsEnabled && hordeStarted) {
            String subtitle = buildHordeIncomingSubtitle(
                    Math.max(1, Config.COMMON.hordeDurationMinutes.get()),
                    currentDay,
                    shouldAnnounceDay);
            if (bloodMoonEnded) {
                subtitle += " The blood moon fades.";
            }
            if (shouldAnnounceDay) {
                state.setLastDayAnnouncementDay(currentDay);
            }
            sendTitleToAllPlayers(level, "HORDE INCOMING", subtitle);
            return;
        }

        if (eventNotificationsEnabled && (hordeEnded || bloodMoonEnded)) {
            notifyEndedEvents(level, hordeEnded, bloodMoonEnded, shouldAnnounceDay, currentDay);
            if (shouldAnnounceDay) {
                state.setLastDayAnnouncementDay(currentDay);
            }
            return;
        }

        if (shouldAnnounceDay) {
            sendTitleToAllPlayers(level, "Day " + currentDay, MORNING_DAY_SUBTITLE);
            state.setLastDayAnnouncementDay(currentDay);
        }
    }

    private static void notifyEndedEvents(
            ServerLevel level,
            boolean hordeEnded,
            boolean bloodMoonEnded,
            boolean includeDayAnnouncement,
            long currentDay) {
        if (!Config.COMMON.enableEventNotifications.get()) {
            return;
        }

        String title;
        String subtitle;
        if (bloodMoonEnded && hordeEnded) {
            title = "Dawn Breaks";
        } else if (bloodMoonEnded) {
            title = "Dawn Breaks";
        } else {
            title = "Horde Ended";
        }

        subtitle = buildEndedEventsSubtitle(hordeEnded, bloodMoonEnded, includeDayAnnouncement, currentDay);
        sendTitleToAllPlayers(level, title, subtitle);
    }

    static String buildEndedEventsSubtitle(
            boolean hordeEnded,
            boolean bloodMoonEnded,
            boolean includeDayAnnouncement,
            long currentDay) {
        String subtitle;
        if (bloodMoonEnded && hordeEnded) {
            subtitle = "The blood moon fades and the zombie horde has dispersed.";
        } else if (bloodMoonEnded) {
            subtitle = "The blood moon fades.";
        } else {
            subtitle = "The zombie horde has dispersed.";
        }

        return includeDayAnnouncement ? "Day " + currentDay + " | " + subtitle : subtitle;
    }

    private static boolean isDayAnnouncementWindow(long dayTime) {
        return EventSchedule.isHordeRollWindow(dayTime);
    }

    static boolean shouldAnnounceDay(long currentDay, long dayTime, long lastAnnouncedDay, boolean enabled) {
        return enabled
                && currentDay >= 0L
                && isDayAnnouncementWindow(dayTime)
                && lastAnnouncedDay != currentDay;
    }

    static String buildHordeIncomingSubtitle(int durationMinutes, long currentDay, boolean includeDayAnnouncement) {
        String subtitle = "Zombie waves for " + Math.max(1, durationMinutes) + " minutes.";
        if (includeDayAnnouncement) {
            return "Day " + currentDay + " | " + subtitle;
        }
        return subtitle;
    }

    private static void activateHorde(ServerLevel level, ApocalypseWorldData state) {
        int durationMinutes = Math.max(1, Config.COMMON.hordeDurationMinutes.get());
        state.setHordeActive(true);
        state.setHordeEndGameTime(level.getGameTime() + (durationMinutes * 60L * 20L));
    }

    public static HordeStartResult startHorde(ServerLevel level) {
        if (!Config.COMMON.enableDaySpawning.get()) {
            return HordeStartResult.CUSTOM_SPAWNING_DISABLED;
        }

        ServerLevel eventLevel = eventLevel(level);
        if (isManualHordeBlockedByDaytime(
                !eventLevel.dimensionType().hasFixedTime(),
                eventLevel.isDay(),
                Config.COMMON.enableDaytimeSpawning.get())) {
            return HordeStartResult.DAYTIME_SPAWNING_DISABLED;
        }

        ApocalypseWorldData state = ApocalypseWorldData.get(eventLevel.getServer());
        int durationMinutes = Math.max(1, Config.COMMON.hordeDurationMinutes.get());
        long absoluteDayTime = eventLevel.getDayTime();
        long currentDay = absoluteDayTime / 24000L;
        long dayTime = absoluteDayTime % 24000L;
        boolean includeDayAnnouncement = shouldAnnounceDay(
                currentDay,
                dayTime,
                state.getLastDayAnnouncementDay(),
                Config.COMMON.enableDayCounterAnnouncements.get());
        boolean includeDayInHordeTitle = includeDayAnnouncement && Config.COMMON.enableEventNotifications.get();

        activateHorde(eventLevel, state);
        if (EventSchedule.isHordeRollWindow(dayTime)) {
            state.setLastHordeRollDay(currentDay);
        }
        if (includeDayInHordeTitle) {
            state.setLastDayAnnouncementDay(currentDay);
        }

        notifyAllPlayers(eventLevel, "HORDE INCOMING",
                buildHordeIncomingSubtitle(durationMinutes, currentDay, includeDayInHordeTitle));

        if (Config.COMMON.enableDebugLogging.get()) {
            LOGGER.info("[ZombieApocalypse] Horde started; duration={} minutes", durationMinutes);
        }
        return HordeStartResult.STARTED;
    }

    public static void stopHorde(ServerLevel level) {
        ServerLevel eventLevel = eventLevel(level);
        ApocalypseWorldData state = ApocalypseWorldData.get(eventLevel.getServer());
        state.setHordeActive(false);
        state.setHordeEndGameTime(0L);

        if (Config.COMMON.enableDebugLogging.get()) {
            LOGGER.info("[ZombieApocalypse] Horde stopped by command");
        }
    }

    public static boolean triggerBloodMoon(ServerLevel level) {
        if (!Config.COMMON.enableDaySpawning.get()) {
            return false;
        }

        ServerLevel eventLevel = eventLevel(level);
        ApocalypseWorldData state = ApocalypseWorldData.get(eventLevel.getServer());
        long absoluteDayTime = eventLevel.getDayTime();
        long dayTime = absoluteDayTime % 24000L;
        long currentDay = absoluteDayTime / 24000L;

        if (EventSchedule.isNight(dayTime)) {
            boolean alreadyActive = state.isBloodMoonActive();
            state.setBloodMoonNightDay(currentDay);
            state.setForcedBloodMoonPending(false);
            state.setBloodMoonActive(true);
            if (!alreadyActive) {
                notifyAllPlayers(eventLevel, "BLOOD MOON", "Zombies are swarming tonight.");
            }
            return true;
        }

        state.setForcedBloodMoonPending(true);
        return false;
    }

    public static void stopBloodMoon(ServerLevel level) {
        ServerLevel eventLevel = eventLevel(level);
        ApocalypseWorldData state = ApocalypseWorldData.get(eventLevel.getServer());
        state.setBloodMoonActive(false);
        state.setForcedBloodMoonPending(false);

        if (Config.COMMON.enableDebugLogging.get()) {
            LOGGER.info("[ZombieApocalypse] Blood moon stopped by command");
        }
    }

    public static void stopAllEvents(ServerLevel level) {
        ServerLevel eventLevel = eventLevel(level);
        ApocalypseWorldData state = ApocalypseWorldData.get(eventLevel.getServer());
        long absoluteDayTime = eventLevel.getDayTime();
        stopSpawnEvents(state, absoluteDayTime / 24000L, absoluteDayTime % 24000L);
    }

    public static boolean isHordeActive(ServerLevel level) {
        return Config.COMMON.enableDaySpawning.get()
                && ApocalypseWorldData.get(level.getServer()).isHordeActive();
    }

    public static boolean isBloodMoonActive(ServerLevel level) {
        return Config.COMMON.enableDaySpawning.get()
                && ApocalypseWorldData.get(level.getServer()).isBloodMoonActive();
    }

    public static boolean isBloodMoonForced(ServerLevel level) {
        return Config.COMMON.enableDaySpawning.get()
                && ApocalypseWorldData.get(level.getServer()).isForcedBloodMoonPending();
    }

    public static long getHordeRemainingSeconds(ServerLevel level) {
        ServerLevel eventLevel = eventLevel(level);
        ApocalypseWorldData state = ApocalypseWorldData.get(eventLevel.getServer());
        if (!Config.COMMON.enableDaySpawning.get() || !state.isHordeActive()) {
            return 0L;
        }

        return remainingSeconds(state.getHordeEndGameTime(), eventLevel.getGameTime());
    }

    static long remainingSeconds(long endGameTime, long gameTime) {
        long remainingTicks = endGameTime - gameTime;
        return remainingTicks <= 0L ? 0L : ((remainingTicks - 1L) / 20L) + 1L;
    }

    public static double getSpawnMultiplier(ServerLevel level) {
        if (!Config.COMMON.enableDaySpawning.get()) {
            return 1.0;
        }

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
        if (!Config.COMMON.enableDaySpawning.get()) {
            return Config.COMMON.zombiesPerSpawn.get();
        }

        ApocalypseWorldData state = ApocalypseWorldData.get(level.getServer());
        return selectEventWaveSize(
                state.isHordeActive(),
                state.isBloodMoonActive(),
                Config.COMMON.zombiesPerSpawn.get(),
                Config.COMMON.hordeZombiesPerSpawn.get(),
                Config.COMMON.bloodMoonZombiesPerSpawn.get());
    }

    static int selectEventWaveSize(
            boolean hordeActive,
            boolean bloodMoonActive,
            int normalSize,
            int hordeSize,
            int bloodMoonSize) {
        if (hordeActive && bloodMoonActive) {
            return Math.max(Math.max(1, hordeSize), Math.max(1, bloodMoonSize));
        }
        if (hordeActive) {
            return Math.max(1, hordeSize);
        }
        if (bloodMoonActive) {
            return Math.max(1, bloodMoonSize);
        }
        return Math.max(1, normalSize);
    }

    public static int getEventSpawnInterval() {
        return Math.max(1, Config.COMMON.eventSpawnInterval.get());
    }

    static boolean isScheduledHordeBlocked(
            boolean daytimeSpawningEnabled,
            long currentDay,
            int daylightSpawnStartDay) {
        return !daytimeSpawningEnabled || currentDay < Math.max(0, daylightSpawnStartDay);
    }

    static boolean isManualHordeBlockedByDaytime(
            boolean hasDayNightCycle,
            boolean isDay,
            boolean daytimeSpawningEnabled) {
        return hasDayNightCycle && isDay && !daytimeSpawningEnabled;
    }

    static boolean shouldConsumeScheduledHordeRollAfterEnd(long dayTime) {
        return EventSchedule.isHordeRollWindow(dayTime);
    }

    private static ServerLevel eventLevel(ServerLevel level) {
        return level.getServer().overworld();
    }
}
