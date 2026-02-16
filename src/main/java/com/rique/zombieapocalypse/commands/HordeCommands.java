package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;

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
                .then(Commands.literal("start")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            HordeManager.startHorde(level);
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
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();

                            boolean horde = HordeManager.isHordeActive(level);
                            boolean bloodMoon = HordeManager.isBloodMoonActive(level);
                            boolean forcedBloodMoon = HordeManager.isBloodMoonForced(level);

                            StringBuilder sb = new StringBuilder();
                            sb.append("Event status:\n");
                            sb.append("Horde: ").append(horde ? "ACTIVE" : "inactive");
                            if (horde) {
                                sb.append(" (").append(HordeManager.getHordeRemainingSeconds(level)).append("s remaining)");
                            }
                            sb.append("\nBlood moon: ").append(bloodMoon ? "ACTIVE" : "inactive");
                            if (forcedBloodMoon) {
                                sb.append(" (forced)");
                            }
                            sb.append("\nSpawn multiplier: ")
                                    .append(CommandUtil.multiplier(HordeManager.getSpawnMultiplier(level)));

                            CommandUtil.feedback(context.getSource(), sb.toString(), false);
                            return 1;
                        })));
    }

    private static void registerZBloodMoon(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zbloodmoon")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    ServerLevel level = context.getSource().getLevel();
                    HordeManager.triggerBloodMoon(level);
                    CommandUtil.feedback(context.getSource(), "Blood moon has been forced.", true);
                    return 1;
                }));
    }
}
