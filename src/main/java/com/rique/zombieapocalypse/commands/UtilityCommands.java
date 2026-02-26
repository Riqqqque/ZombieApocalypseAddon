package com.rique.zombieapocalypse.commands;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;

import com.rique.zombieapocalypse.Config;

public final class UtilityCommands {

    private UtilityCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerZBurn(dispatcher);
        registerZKill(dispatcher);
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
                    int removed = 0;
                    for (ServerLevel level : context.getSource().getServer().getAllLevels()) {
                        List<Zombie> toRemove = new ArrayList<>();
                        for (Entity entity : level.getAllEntities()) {
                            if (entity instanceof Zombie zombie) {
                                toRemove.add(zombie);
                            }
                        }
                        toRemove.forEach(Zombie::discard);
                        removed += toRemove.size();
                    }

                    CommandUtil.feedback(context.getSource(),
                            "Removed " + removed + " zombie-class entities.",
                            true);
                    return removed;
                }));
    }
}
