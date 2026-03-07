package com.rique.zombieapocalypse;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;

public final class ZombieKillAdvancements {

    private static final String CRITERION = "manual";
    private static final Milestone[] MILESTONES = {
            new Milestone(250, "zombie_kills/kill_250"),
            new Milestone(1000, "zombie_kills/kill_1000"),
            new Milestone(3000, "zombie_kills/kill_3000")
    };

    private record Milestone(int kills, String path) {
    }

    private ZombieKillAdvancements() {
    }

    public static void awardForKillCount(ServerPlayer player, int totalKills) {
        if (totalKills <= 0) {
            return;
        }

        if (player.getServer() == null) {
            return;
        }

        ServerAdvancementManager advancementManager = player.getServer().getAdvancements();
        for (Milestone milestone : MILESTONES) {
            if (totalKills < milestone.kills()) {
                break;
            }

            AdvancementHolder advancement = advancementManager.get(
                    ResourceLocation.fromNamespaceAndPath(ZombieApocalypseAddon.MODID, milestone.path()));
            if (advancement != null) {
                player.getAdvancements().award(advancement, CRITERION);
            }
        }
    }

    public static void clearMilestones(ServerPlayer player) {
        if (player.getServer() == null) {
            return;
        }

        ServerAdvancementManager advancementManager = player.getServer().getAdvancements();
        for (int i = MILESTONES.length - 1; i >= 0; i--) {
            Milestone milestone = MILESTONES[i];
            AdvancementHolder advancement = advancementManager.get(
                    ResourceLocation.fromNamespaceAndPath(ZombieApocalypseAddon.MODID, milestone.path()));
            if (advancement != null) {
                player.getAdvancements().revoke(advancement, CRITERION);
            }
        }
    }
}
