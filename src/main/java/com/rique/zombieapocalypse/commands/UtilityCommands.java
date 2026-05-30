package com.rique.zombieapocalypse.commands;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;

import com.rique.zombieapocalypse.ApocalypseWorldData;
import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.EventHandler;
import com.rique.zombieapocalypse.ZombieClassMobs;

public final class UtilityCommands {

    private UtilityCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerZBurn(dispatcher);
        registerZKill(dispatcher);
        registerZCleanup(dispatcher);
    }

    private static void registerZBurn(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zburn")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean enableBurning = BoolArgumentType.getBool(context, "enabled");
                            Config.COMMON.preventSunBurn.set(!enableBurning);
                            CommandUtil.feedback(context.getSource(),
                                    "Zombie daylight burning: " + CommandUtil.onOff(enableBurning),
                                    true);
                            return 1;
                        })));
    }

    private static void registerZKill(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zkill")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    int removed = removeZombieClassEntities(context.getSource().getServer());

                    CommandUtil.feedback(context.getSource(),
                            "Removed " + removed + " zombie-class entities.",
                            true);
                    return removed;
                }));
    }

    private static void registerZCleanup(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zcleanup")
                .requires(source -> source.hasPermission(2))
                .executes(context -> runCleanup(context.getSource(), false))
                .then(Commands.literal("uninstall")
                        .executes(context -> runCleanup(context.getSource(), true))));
    }

    private static int runCleanup(CommandSourceStack source, boolean uninstallPrep) {
        MinecraftServer server = source.getServer();
        int removed = removeZombieClassEntities(server);
        ApocalypseWorldData.get(server).resetEventScheduleState();
        EventHandler.clearRuntimeState();

        if (uninstallPrep) {
            Config.COMMON.enableDaySpawning.set(false);
            Config.COMMON.enableHordeEvents.set(false);
            Config.COMMON.enableBloodMoon.set(false);
            Config.COMMON.enableDifficultyScaling.set(false);
            Config.COMMON.enableAttributeModifiers.set(false);
        }

        CommandUtil.feedback(source, buildCleanupMessage(removed, uninstallPrep), true);
        return Math.max(1, removed);
    }

    static int removeZombieClassEntities(MinecraftServer server) {
        int removed = 0;
        for (ServerLevel level : server.getAllLevels()) {
            List<Zombie> toRemove = new ArrayList<>();
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Zombie zombie && ZombieClassMobs.isZombieClass(zombie)) {
                    toRemove.add(zombie);
                }
            }
            toRemove.forEach(Zombie::discard);
            removed += toRemove.size();
        }
        return removed;
    }

    static String buildCleanupMessage(int removed, boolean uninstallPrep) {
        String message = "Cleanup complete. Removed " + removed
                + " zombie-class entities and reset apocalypse event state.";
        if (uninstallPrep) {
            return message + " Custom spawning, hordes, blood moons, scaling, and attribute tuning are disabled for this server session.";
        }
        return message;
    }
}
