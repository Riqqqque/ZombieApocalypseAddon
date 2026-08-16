package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.ConfigLimits;
import com.rique.zombieapocalypse.HordeManager;
import com.rique.zombieapocalypse.HordeManager.HordeStartResult;

public final class HordeCommands {

    private HordeCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerZHorde(dispatcher);
        registerZBloodMoon(dispatcher);
    }

    private static void registerZHorde(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zhorde")
                .executes(context -> showStatus(context.getSource()))
                .then(CommandUtil.admin(Commands.literal("on")
                        .executes(context -> setHordesEnabled(context.getSource(), true))))
                .then(CommandUtil.admin(Commands.literal("off")
                        .executes(context -> setHordesEnabled(context.getSource(), false))))
                .then(CommandUtil.admin(Commands.literal("start")
                        .executes(context -> startHorde(context.getSource()))))
                .then(CommandUtil.admin(Commands.literal("stop")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            HordeManager.stopHorde(level);
                            CommandUtil.feedback(context.getSource(), "Horde event stopped.", true);
                            return 1;
                        })))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource())))
                .then(CommandUtil.toggleSetting("enabled", Config.COMMON.enableHordeEvents::get,
                        HordeCommands::setHordesEnabledValue, "Scheduled hordes"))
                .then(CommandUtil.intSetting("interval", "days", 1, ConfigLimits.MAX_APOCALYPSE_DAY,
                        Config.COMMON.hordeIntervalDays::get,
                        value -> Config.set(Config.COMMON.hordeIntervalDays, value),
                        value -> "Scheduled horde interval: every " + value + (value == 1 ? " day" : " days"),
                        1, 2, 3, 5, 7, 10, 15, 30, 50, 100, 365, 1000))
                .then(CommandUtil.doubleSetting("chance", "chance", 0.0, 1.0,
                        Config.COMMON.hordeStartChance::get,
                        value -> Config.set(Config.COMMON.hordeStartChance, value),
                        value -> "Scheduled horde chance: " + CommandUtil.percent(value),
                        0.0, 0.1, 0.25, 0.5, 0.75, 1.0))
                .then(CommandUtil.intSetting("duration", "minutes", 1, 10080,
                        Config.COMMON.hordeDurationMinutes::get,
                        value -> Config.set(Config.COMMON.hordeDurationMinutes, value),
                        value -> "Horde duration: " + value + (value == 1 ? " minute" : " minutes"),
                        1, 3, 5, 10, 15, 30, 60, 120, 360, 1440, 10080))
                .then(CommandUtil.doubleSetting("multiplier", "multiplier", 1.0, 20.0,
                        Config.COMMON.hordeSpawnMultiplier::get,
                        value -> Config.set(Config.COMMON.hordeSpawnMultiplier, value),
                        value -> "Horde spawn chance multiplier: " + CommandUtil.multiplier(value),
                        1.0, 1.5, 2.0, 3.0, 5.0, 10.0, 20.0))
                .then(CommandUtil.intSetting("amount", "zombies", 1, 100,
                        Config.COMMON.hordeZombiesPerSpawn::get,
                        value -> Config.set(Config.COMMON.hordeZombiesPerSpawn, value),
                        value -> "Zombies attempted per horde wave: " + value,
                        1, 2, 5, 10, 20, 50, 100))
                .then(CommandUtil.intSetting("spawninterval", "ticks", 1, 200,
                        Config.COMMON.eventSpawnInterval::get,
                        value -> Config.set(Config.COMMON.eventSpawnInterval, value),
                        value -> "Event spawn interval: " + CommandUtil.ticks(value),
                        1, 5, 10, 20, 40, 100, 200))
                .then(CommandUtil.toggleSetting("notifications", Config.COMMON.enableEventNotifications::get,
                        value -> Config.set(Config.COMMON.enableEventNotifications, value),
                        "Event title notifications")));
    }

    private static void registerZBloodMoon(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zbloodmoon")
                .executes(context -> startBloodMoonLegacy(context.getSource()))
                .then(CommandUtil.admin(Commands.literal("on")
                        .executes(context -> setBloodMoonsEnabled(context.getSource(), true))))
                .then(CommandUtil.admin(Commands.literal("off")
                        .executes(context -> setBloodMoonsEnabled(context.getSource(), false))))
                .then(CommandUtil.admin(Commands.literal("start")
                        .executes(context -> startBloodMoon(context.getSource()))))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource())))
                .then(CommandUtil.toggleSetting("enabled", Config.COMMON.enableBloodMoon::get,
                        HordeCommands::setBloodMoonsEnabledValue, "Random blood moons"))
                .then(CommandUtil.doubleSetting("chance", "chance", 0.0, 1.0,
                        Config.COMMON.bloodMoonChance::get,
                        value -> Config.set(Config.COMMON.bloodMoonChance, value),
                        value -> "Nightly blood moon chance: " + CommandUtil.percent(value),
                        0.0, 0.05, 0.1, 0.15, 0.25, 0.5, 0.75, 1.0))
                .then(CommandUtil.doubleSetting("multiplier", "multiplier", 1.0, 50.0,
                        Config.COMMON.bloodMoonSpawnMultiplier::get,
                        value -> Config.set(Config.COMMON.bloodMoonSpawnMultiplier, value),
                        value -> "Blood moon spawn chance multiplier: " + CommandUtil.multiplier(value),
                        1.0, 2.0, 3.0, 5.0, 10.0, 20.0, 50.0))
                .then(CommandUtil.intSetting("amount", "zombies", 1, 100,
                        Config.COMMON.bloodMoonZombiesPerSpawn::get,
                        value -> Config.set(Config.COMMON.bloodMoonZombiesPerSpawn, value),
                        value -> "Zombies attempted per blood moon wave: " + value,
                        1, 2, 4, 5, 10, 20, 50, 100))
                .then(CommandUtil.intSetting("spawninterval", "ticks", 1, 200,
                        Config.COMMON.eventSpawnInterval::get,
                        value -> Config.set(Config.COMMON.eventSpawnInterval, value),
                        value -> "Event spawn interval: " + CommandUtil.ticks(value),
                        1, 5, 10, 20, 40, 100, 200))
                .then(CommandUtil.toggleSetting("notifications", Config.COMMON.enableEventNotifications::get,
                        value -> Config.set(Config.COMMON.enableEventNotifications, value),
                        "Event title notifications")));
    }

    private static int startHorde(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        HordeStartResult result = HordeManager.startHorde(level);
        if (result == HordeStartResult.CUSTOM_SPAWNING_DISABLED) {
            CommandUtil.failure(source, "Custom zombie waves are off. Run /za spawn on before starting a horde.");
            return 0;
        }
        if (result == HordeStartResult.DAYTIME_SPAWNING_DISABLED) {
            CommandUtil.failure(source,
                    "Daytime custom waves are off. Wait until night or run /za spawn daytime on.");
            return 0;
        }
        CommandUtil.feedback(source, "Horde event started.", true);
        return 1;
    }

    private static int startBloodMoon(CommandSourceStack source) {
        if (!Config.COMMON.enableDaySpawning.get()) {
            CommandUtil.failure(source,
                    "Custom zombie waves are off. Run /za spawn on before forcing a blood moon.");
            return 0;
        }
        ServerLevel level = source.getLevel();
        boolean activeNow = HordeManager.triggerBloodMoon(level);
        CommandUtil.feedback(
                source,
                activeNow ? "Blood moon is active now." : "Blood moon queued for tonight.",
                true);
        return 1;
    }

    private static int startBloodMoonLegacy(CommandSourceStack source) {
        if (!source.hasPermission(2)) {
            CommandUtil.failure(source, "Starting a blood moon requires permission level 2. Use /za bloodmoon status to view it.");
            return 0;
        }
        return startBloodMoon(source);
    }

    private static int setHordesEnabled(CommandSourceStack source, boolean enabled) {
        if (enabled) {
            FeaturePresets.enableHordes();
            CommandUtil.feedback(source,
                    "Scheduled hordes: ON\nBalanced event preset loaded. Custom day/night waves were enabled so scheduled dawn hordes can run.",
                    true);
        } else {
            Config.set(Config.COMMON.enableHordeEvents, false);
            HordeManager.stopHorde(source.getLevel());
            CommandUtil.feedback(source, "Scheduled hordes: OFF\nAny active horde was stopped.", true);
        }
        return 1;
    }

    private static void setHordesEnabledValue(CommandSourceStack source, boolean enabled) {
        if (enabled) {
            FeaturePresets.enableHordes();
        } else {
            Config.set(Config.COMMON.enableHordeEvents, false);
            HordeManager.stopHorde(source.getLevel());
        }
    }

    private static int setBloodMoonsEnabled(CommandSourceStack source, boolean enabled) {
        if (enabled) {
            FeaturePresets.enableBloodMoons();
            CommandUtil.feedback(source,
                    "Random blood moons: ON\nBalanced night-event preset loaded. Custom zombie waves were enabled automatically.",
                    true);
        } else {
            Config.set(Config.COMMON.enableBloodMoon, false);
            HordeManager.stopBloodMoon(source.getLevel());
            CommandUtil.feedback(source, "Random blood moons: OFF\nAny active or queued blood moon was stopped.", true);
        }
        return 1;
    }

    private static void setBloodMoonsEnabledValue(CommandSourceStack source, boolean enabled) {
        if (enabled) {
            FeaturePresets.enableBloodMoons();
        } else {
            Config.set(Config.COMMON.enableBloodMoon, false);
            HordeManager.stopBloodMoon(source.getLevel());
        }
    }

    private static int showStatus(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        boolean horde = HordeManager.isHordeActive(level);
        boolean bloodMoon = HordeManager.isBloodMoonActive(level);
        boolean forcedBloodMoon = HordeManager.isBloodMoonForced(level);

        StringBuilder status = new StringBuilder("Event status:\n");
        status.append("Custom zombie waves: ")
                .append(CommandUtil.onOff(Config.COMMON.enableDaySpawning.get())).append('\n');
        boolean daytimeSpawning = Config.COMMON.enableDaytimeSpawning.get();
        String scheduledHordes = !Config.COMMON.enableHordeEvents.get()
                ? "OFF"
                : !Config.COMMON.enableDaySpawning.get()
                ? "PAUSED (custom waves off)"
                : !daytimeSpawning
                        ? "PAUSED (night-only mode)"
                        : "ON";
        status.append("Daytime custom waves: ").append(CommandUtil.onOff(daytimeSpawning)).append('\n');
        status.append("Scheduled hordes: ").append(scheduledHordes)
                .append(" | Current horde: ").append(horde ? "ACTIVE" : "inactive");
        if (horde) {
            status.append(" (").append(HordeManager.getHordeRemainingSeconds(level)).append(" seconds left)");
            ServerLevel overworld = source.getServer().overworld();
            if (!daytimeSpawning && overworld.isDay()) {
                status.append("; Overworld daytime waves are blocked");
            }
        }
        status.append("\nRandom blood moons: ")
                .append(CommandUtil.onOff(Config.COMMON.enableBloodMoon.get()))
                .append(" | Current blood moon: ").append(bloodMoon ? "ACTIVE" : "inactive");
        if (forcedBloodMoon) {
            status.append(" (forced)");
        }
        status.append("\nCurrent spawn multiplier: ")
                .append(CommandUtil.multiplier(HordeManager.getSpawnMultiplier(level)));
        status.append("\nScheduled horde setup: every ").append(Config.COMMON.hordeIntervalDays.get())
                .append(" days at ").append(CommandUtil.percent(Config.COMMON.hordeStartChance.get()))
                .append(" chance, ").append(Config.COMMON.hordeDurationMinutes.get()).append(" minutes");
        status.append("\nHorde pressure: ").append(Config.COMMON.hordeZombiesPerSpawn.get())
                .append(" zombies per wave at ")
                .append(CommandUtil.multiplier(Config.COMMON.hordeSpawnMultiplier.get()));
        status.append("\nBlood moon setup: ").append(CommandUtil.percent(Config.COMMON.bloodMoonChance.get()))
                .append(" nightly chance, ").append(Config.COMMON.bloodMoonZombiesPerSpawn.get())
                .append(" zombies per wave at ")
                .append(CommandUtil.multiplier(Config.COMMON.bloodMoonSpawnMultiplier.get()));
        status.append("\nEvent interval: ").append(CommandUtil.ticks(Config.COMMON.eventSpawnInterval.get()))
                .append(" | Titles: ").append(CommandUtil.onOff(Config.COMMON.enableEventNotifications.get()));

        CommandUtil.feedback(source, status.toString(), false);
        return 1;
    }
}
