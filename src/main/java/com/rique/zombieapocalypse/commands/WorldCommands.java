package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;

import com.rique.zombieapocalypse.ApocalypseWorldData;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class WorldCommands {

    private WorldCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zday")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    CommandUtil.feedback(context.getSource(), buildStatusMessage(context.getSource().getServer()), false);
                    return 1;
                })
                .then(Commands.literal("status")
                        .executes(context -> {
                            CommandUtil.feedback(context.getSource(), buildStatusMessage(context.getSource().getServer()), false);
                            return 1;
                        }))
                .then(Commands.literal("set")
                        .then(Commands.argument("day", LongArgumentType.longArg(0L, 1_000_000L))
                                .executes(context -> {
                                    long day = LongArgumentType.getLong(context, "day");
                                    boolean resetEvents = setDayCounter(context.getSource().getServer(), day);
                                    CommandUtil.feedback(
                                            context.getSource(),
                                            resetEvents
                                                    ? "Set world day counter to " + day
                                                            + " and reset active apocalypse event scheduling state."
                                                    : "Set world day counter to " + day + ".",
                                            true);
                                    return 1;
                                }))));
    }

    private static String buildStatusMessage(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        long dayCounter = overworld.getDayTime() / 24000L;
        long timeOfDay = floorDayTime(overworld.getDayTime());
        return "World day counter: " + dayCounter + " | Time of day: " + timeOfDay;
    }

    private static boolean setDayCounter(MinecraftServer server, long dayCounter) {
        ServerLevel overworld = server.overworld();
        long currentDayCounter = overworld.getDayTime() / 24000L;
        long timeOfDay = floorDayTime(overworld.getDayTime());
        long absoluteDayTime = (dayCounter * 24000L) + timeOfDay;
        for (ServerLevel level : server.getAllLevels()) {
            level.setDayTime(absoluteDayTime);
        }

        if (currentDayCounter == dayCounter) {
            return false;
        }

        ApocalypseWorldData.get(server).resetEventScheduleState();
        return true;
    }

    private static long floorDayTime(long dayTime) {
        long result = dayTime % 24000L;
        return result < 0 ? result + 24000L : result;
    }
}
