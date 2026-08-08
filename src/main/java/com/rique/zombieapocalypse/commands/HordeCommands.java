package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.HordeManager;

public final class HordeCommands {

    private HordeCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerZHorde(dispatcher);
        registerZBloodMoon(dispatcher);
    }

    private static void registerZHorde(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zhorde")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showStatus(context.getSource()))
                .then(Commands.literal("start")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            if (!HordeManager.startHorde(level)) {
                                context.getSource().sendFailure(Component.literal(
                                        "Custom zombie waves are off. Run /zdayspawn on before starting a horde."));
                                return 0;
                            }
                            CommandUtil.feedback(context.getSource(), "Horde event started.", true);
                            return 1;
                        }))
                .then(Commands.literal("stop")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            HordeManager.stopHorde(level);
                            CommandUtil.feedback(context.getSource(), "Horde event stopped.", true);
                            return 1;
                        }))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource()))));
    }

    private static void registerZBloodMoon(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zbloodmoon")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    if (!Config.COMMON.enableDaySpawning.get()) {
                        context.getSource().sendFailure(Component.literal(
                                "Custom zombie waves are off. Run /zdayspawn on before forcing a blood moon."));
                        return 0;
                    }
                    ServerLevel level = context.getSource().getLevel();
                    boolean activeNow = HordeManager.triggerBloodMoon(level);
                    CommandUtil.feedback(
                            context.getSource(),
                            activeNow ? "Blood moon is active now." : "Blood moon queued for tonight.",
                            true);
                    return 1;
                }));
    }

    private static int showStatus(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        boolean horde = HordeManager.isHordeActive(level);
        boolean bloodMoon = HordeManager.isBloodMoonActive(level);
        boolean forcedBloodMoon = HordeManager.isBloodMoonForced(level);

        StringBuilder status = new StringBuilder("Event status:\n");
        status.append("Custom zombie waves: ")
                .append(CommandUtil.onOff(Config.COMMON.enableDaySpawning.get())).append('\n');
        status.append("Scheduled hordes: ").append(CommandUtil.onOff(Config.COMMON.enableHordeEvents.get()))
                .append(" | Current horde: ").append(horde ? "ACTIVE" : "inactive");
        if (horde) {
            status.append(" (").append(HordeManager.getHordeRemainingSeconds(level)).append(" seconds left)");
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
