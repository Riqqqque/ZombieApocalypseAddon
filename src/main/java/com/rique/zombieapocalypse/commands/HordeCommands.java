package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;

import com.rique.zombieapocalypse.Config;
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
                        .executes(context -> showStatus(context.getSource()))));
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
                        .executes(context -> showStatus(context.getSource()))));
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

        CommandUtil.feedback(source, status.toString(), false);
        return 1;
    }
}
