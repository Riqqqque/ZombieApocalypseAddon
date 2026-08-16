package com.rique.zombieapocalypse.commands;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;

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
                .executes(context -> showBurning(context.getSource()))
                .then(CommandUtil.admin(CommandUtil.toggleArgument("state")
                        .executes(context -> setBurning(
                                context.getSource(),
                                CommandUtil.getToggle(context, "state"))))));
    }

    private static int showBurning(CommandSourceStack source) {
        boolean burning = !Config.COMMON.preventSunBurn.get();
        CommandUtil.feedback(source, "Zombie daylight burning: " + CommandUtil.onOff(burning), false);
        return 1;
    }

    private static int setBurning(CommandSourceStack source, boolean burning) {
        Config.set(Config.COMMON.preventSunBurn, !burning);
        CommandUtil.feedback(source, "Zombie daylight burning: " + CommandUtil.onOff(burning), true);
        return 1;
    }

    private static void registerZKill(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(CommandUtil.admin(Commands.literal("zkill")
                .executes(context -> {
                    int removed = removeZombieClassEntities(context.getSource().getServer());

                    CommandUtil.feedback(context.getSource(),
                            "Removed " + removed + " zombie-class entities.",
                            true);
                    return removed;
                })));
    }

    private static void registerZCleanup(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(CommandUtil.admin(Commands.literal("zcleanup")
                .executes(context -> runCleanup(context.getSource(), false))
                .then(Commands.literal("uninstall")
                        .executes(context -> runCleanup(context.getSource(), true)))));
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
