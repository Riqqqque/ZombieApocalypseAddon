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
                            Config.set(Config.COMMON.preventSunBurn, !enableBurning);
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
            Config.edit(() -> {
                Config.set(Config.COMMON.enableDaySpawning, false);
                Config.set(Config.COMMON.enableHordeEvents, false);
                Config.set(Config.COMMON.enableBloodMoon, false);
                Config.set(Config.COMMON.enableDifficultyScaling, false);
                Config.set(Config.COMMON.enableAttributeModifiers, false);
                Config.set(Config.COMMON.enableZombieBlockBreaking, false);
                Config.set(Config.COMMON.enableZombieBlockPlacing, false);
                Config.set(Config.COMMON.enableZombieTowering, false);
                Config.set(Config.COMMON.enableDayCounterAnnouncements, false);
                Config.set(Config.COMMON.preventSunBurn, false);
                Config.set(Config.COMMON.enableExtraDrops, false);
                Config.set(Config.COMMON.enableDeathCooldown, false);
            });
        }

        CommandUtil.feedback(source, buildCleanupMessage(removed, uninstallPrep), true);
        return Math.max(1, removed);
    }

    static int removeZombieClassEntities(MinecraftServer server) {
        int removed = 0;
        for (ServerLevel level : server.getAllLevels()) {
            List<Entity> toRemove = new ArrayList<>();
            for (Entity entity : level.getAllEntities()) {
                if (ZombieClassMobs.isZombieClass(entity)) {
                    toRemove.add(entity);
                }
            }
            toRemove.forEach(Entity::discard);
            removed += toRemove.size();
        }
        return removed;
    }

    static String buildCleanupMessage(int removed, boolean uninstallPrep) {
        String message = "Cleanup complete. Removed " + removed
                + " zombie-class entities and reset apocalypse event state.";
        if (uninstallPrep) {
            return message + " Custom spawning, events, scaling, attributes, block breaking, block placing, towering, sunlight immunity, extra drops, and death cooldowns are disabled.";
        }
        return message;
    }
}
