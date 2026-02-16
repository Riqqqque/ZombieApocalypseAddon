package com.rique.zombieapocalypse.commands;

import java.util.Map;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import com.rique.zombieapocalypse.StatisticsManager;

public final class StatsCommands {

    private StatsCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zstats")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    if (context.getSource().getEntity() instanceof ServerPlayer player) {
                        showPlayerStats(context.getSource(), player);
                    } else {
                        showServerStats(context.getSource());
                    }
                    return 1;
                })
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            showPlayerStats(context.getSource(), player);
                            return 1;
                        }))
                .then(Commands.literal("all")
                        .executes(context -> {
                            showServerStats(context.getSource());
                            return 1;
                        }))
                .then(Commands.literal("clear")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            StatisticsManager.get(level).clearAll();
                            CommandUtil.feedback(context.getSource(), "All statistics and cooldowns were cleared.", true);
                            return 1;
                        })));
    }

    private static void showPlayerStats(CommandSourceStack source, ServerPlayer player) {
        ServerLevel level = source.getLevel();
        StatisticsManager stats = StatisticsManager.get(level);

        int kills = stats.getKills(player.getUUID());
        int cooldown = stats.getRemainingCooldown(player.getUUID(), level.getGameTime());

        StringBuilder sb = new StringBuilder();
        sb.append("Stats for ").append(player.getGameProfile().getName()).append(":\n");
        sb.append("Zombie kills: ").append(kills);

        if (cooldown > 0) {
            sb.append("\nDeath cooldown: ").append(cooldown).append("s remaining");
        }

        CommandUtil.feedback(source, sb.toString(), false);
    }

    private static void showServerStats(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        StatisticsManager stats = StatisticsManager.get(level);

        Map<UUID, Integer> allKills = stats.getAllKills();
        int totalKills = stats.getTotalKills();

        CommandUtil.feedback(source,
                "Server statistics:\n"
                        + "Total zombie kills: " + totalKills + "\n"
                        + "Tracked players: " + allKills.size(),
                false);
    }
}
