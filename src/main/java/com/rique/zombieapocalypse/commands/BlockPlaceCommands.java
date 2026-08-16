package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.ConfigLimits;
import com.rique.zombieapocalypse.DifficultyManager;
import com.rique.zombieapocalypse.ZombieBlockPlacer;
import com.rique.zombieapocalypse.ZombieClassMobs;

public final class BlockPlaceCommands {

    private BlockPlaceCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zblockplace")
                .executes(context -> showStatus(context.getSource()))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource())))
                .then(CommandUtil.admin(Commands.literal("on")
                        .executes(context -> setEnabled(context.getSource(), true))))
                .then(CommandUtil.admin(Commands.literal("off")
                        .executes(context -> setEnabled(context.getSource(), false))))
                .then(CommandUtil.admin(Commands.literal("dayone")
                        .executes(context -> setEnabled(context.getSource(), true))))
                .then(CommandUtil.toggleSetting("enabled", Config.COMMON.enableZombieBlockPlacing::get,
                        BlockPlaceCommands::setEnabledValue, "Zombie block placing"))
                .then(CommandUtil.intSetting("startday", "day", 0, ConfigLimits.MAX_APOCALYPSE_DAY,
                        Config.COMMON.zombieBlockPlacingStartDay::get,
                        value -> Config.set(Config.COMMON.zombieBlockPlacingStartDay, value),
                        value -> value <= 0
                                ? "Zombie block placing can start immediately when enabled."
                                : "Zombie block placing starts on day " + value + '.',
                        0, 1, 5, 10, 15, 30, 50, 100))
                .then(CommandUtil.intSetting("interval", "ticks", 20, 72000,
                        Config.COMMON.zombieBlockPlacingInterval::get,
                        value -> Config.set(Config.COMMON.zombieBlockPlacingInterval, value),
                        value -> "Zombie block placing interval: " + CommandUtil.ticks(value),
                        20, 40, 100, 200, 600, 1200))
                .then(CommandUtil.doubleSetting("chance", "chance", 0.0, 1.0,
                        Config.COMMON.zombieBlockPlacingChance::get,
                        value -> Config.set(Config.COMMON.zombieBlockPlacingChance, value),
                        value -> "Zombie block placing chance: " + CommandUtil.percent(value),
                        0.0, 0.1, 0.25, 0.5, 0.75, 1.0))
                .then(Commands.literal("block")
                        .executes(context -> {
                            CommandUtil.feedback(context.getSource(),
                                    "Zombie placement block: " + Config.COMMON.zombieBlockPlacingBlock.get(), false);
                            return 1;
                        })
                        .then(CommandUtil.admin(Commands.argument("id", ResourceLocationArgument.id())
                                .suggests((context, builder) ->
                                        SharedSuggestionProvider.suggestResource(
                                                context.getSource().registryAccess()
                                                        .registryOrThrow(Registries.BLOCK)
                                                        .keySet(),
                                                builder))
                                .executes(context -> setPlacementBlock(
                                        context.getSource(),
                                        ResourceLocationArgument.getId(context, "id").toString())))))
                .then(CommandUtil.intSetting("limit", "blocks", 0, 256,
                        Config.COMMON.zombieBlockPlacingMaxPerZombie::get,
                        value -> Config.set(Config.COMMON.zombieBlockPlacingMaxPerZombie, value),
                        value -> "Zombie block placement limit per zombie: " + (value == 0 ? "unlimited" : value),
                        0, 4, 8, 16, 32, 64, 128, 256))
                .then(CommandUtil.intSetting("distance", "blocks", 4, 128,
                        Config.COMMON.zombieBlockPlacingMaxTargetDistance::get,
                        value -> Config.set(Config.COMMON.zombieBlockPlacingMaxTargetDistance, value),
                        value -> "Zombie block placing target distance: " + value + " blocks",
                        4, 8, 16, 24, 32, 64, 128))
                .then(CommandUtil.toggleSetting("target", Config.COMMON.zombieBlockPlacingRequireTarget::get,
                        value -> Config.set(Config.COMMON.zombieBlockPlacingRequireTarget, value), "Require zombie target"))
                .then(CommandUtil.toggleSetting("obstacle", Config.COMMON.zombieBlockPlacingRequireObstacle::get,
                        value -> Config.set(Config.COMMON.zombieBlockPlacingRequireObstacle, value), "Require obstacle, covered target, or gap"))
                .then(CommandUtil.toggleSetting("mobgriefing", Config.COMMON.zombieBlockPlacingRespectMobGriefing::get,
                        value -> Config.set(Config.COMMON.zombieBlockPlacingRespectMobGriefing, value), "Respect mobGriefing"))
                .then(CommandUtil.toggleSetting("bridges", Config.COMMON.zombieBlockPlacingAllowBridges::get,
                        value -> Config.set(Config.COMMON.zombieBlockPlacingAllowBridges, value), "Zombie gap bridging"))
                .then(CommandUtil.toggleSetting("steps", Config.COMMON.zombieBlockPlacingAllowSteps::get,
                        value -> Config.set(Config.COMMON.zombieBlockPlacingAllowSteps, value), "Zombie step placing"))
                .then(CommandUtil.toggleSetting("fluids", Config.COMMON.zombieBlockPlacingReplaceFluids::get,
                        value -> Config.set(Config.COMMON.zombieBlockPlacingReplaceFluids, value), "Replace fluid blocks"))
                .then(CommandUtil.toggleSetting("replaceable", Config.COMMON.zombieBlockPlacingReplaceReplaceableBlocks::get,
                        value -> Config.set(Config.COMMON.zombieBlockPlacingReplaceReplaceableBlocks, value),
                        "Replace plants/snow/other replaceable blocks"))
                .then(CommandUtil.admin(Commands.literal("resetcounts")
                        .executes(context -> {
                            int reset = resetLoadedPlacementCounts(context.getSource());
                            CommandUtil.feedback(context.getSource(),
                                    "Reset block placement counts for " + reset + " loaded zombie-class mobs.",
                                    true);
                            return Math.max(1, reset);
                        }))));
    }

    private static int setPlacementBlock(CommandSourceStack source, String blockId) {
        String error = ZombieBlockPlacer.validatePlacementBlock(
                source.getLevel(),
                BlockPos.containing(source.getPosition()),
                blockId);
        if (error != null) {
            CommandUtil.failure(source, error);
            return 0;
        }

        Config.set(Config.COMMON.zombieBlockPlacingBlock, blockId);
        ZombieBlockPlacer.clearRuntimeState();
        CommandUtil.feedback(source, "Zombie placement block: " + blockId, true);
        return 1;
    }

    private static int setEnabled(CommandSourceStack source, boolean enabled) {
        if (enabled) {
            FeaturePresets.enableBlockPlacing();
            ZombieBlockPlacer.clearRuntimeState();
            CommandUtil.feedback(source,
                    "Zombie block placing: ON\nBalanced cobblestone step/bridge preset loaded and active immediately. mobGriefing and protection events are respected.",
                    true);
        } else {
            Config.set(Config.COMMON.enableZombieBlockPlacing, false);
            CommandUtil.feedback(source, "Zombie block placing: OFF", true);
        }
        return 1;
    }

    private static void setEnabledValue(boolean enabled) {
        if (enabled) {
            FeaturePresets.enableBlockPlacing();
            ZombieBlockPlacer.clearRuntimeState();
        } else {
            Config.set(Config.COMMON.enableZombieBlockPlacing, false);
        }
    }

    private static int showStatus(CommandSourceStack source) {
        CommandUtil.feedback(source, buildStatusMessage(source), false);
        return 1;
    }

    private static int resetLoadedPlacementCounts(CommandSourceStack source) {
        int reset = 0;
        for (ServerLevel level : source.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Zombie zombie && ZombieClassMobs.isZombieClass(zombie)) {
                    ZombieBlockPlacer.resetPlacementCount(zombie);
                    reset++;
                }
            }
        }
        return reset;
    }

    private static String buildStatusMessage(CommandSourceStack source) {
        long currentDay = DifficultyManager.getCurrentDay(source.getLevel());
        int startDay = Config.COMMON.zombieBlockPlacingStartDay.get();
        boolean enabled = Config.COMMON.enableZombieBlockPlacing.get();
        boolean active = enabled && currentDay >= startDay;
        int limit = Config.COMMON.zombieBlockPlacingMaxPerZombie.get();

        StringBuilder status = new StringBuilder();
        status.append("Zombie block placing settings:\n");
        status.append("Enabled: ").append(CommandUtil.onOff(enabled)).append('\n');
        status.append("Active today: ").append(CommandUtil.onOff(active))
                .append(" (current day ").append(currentDay)
                .append(", start day ").append(startDay).append(")\n");
        status.append("Block: ").append(Config.COMMON.zombieBlockPlacingBlock.get()).append('\n');
        status.append("Interval: ").append(CommandUtil.ticks(Config.COMMON.zombieBlockPlacingInterval.get())).append('\n');
        status.append("Chance: ").append(CommandUtil.percent(Config.COMMON.zombieBlockPlacingChance.get())).append('\n');
        status.append("Limit per zombie: ").append(limit == 0 ? "unlimited" : limit).append('\n');
        status.append("Max target distance: ")
                .append(Config.COMMON.zombieBlockPlacingMaxTargetDistance.get()).append(" blocks\n");
        status.append("Require target: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieBlockPlacingRequireTarget.get())).append('\n');
        status.append("Require obstacle/gap: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieBlockPlacingRequireObstacle.get())).append('\n');
        status.append("Respect mobGriefing: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieBlockPlacingRespectMobGriefing.get())).append('\n');
        status.append("Allow bridges: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieBlockPlacingAllowBridges.get())).append('\n');
        status.append("Allow steps: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieBlockPlacingAllowSteps.get())).append('\n');
        status.append("Replace fluids: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieBlockPlacingReplaceFluids.get())).append('\n');
        status.append("Replace plants/snow: ")
                .append(CommandUtil.onOff(Config.COMMON.zombieBlockPlacingReplaceReplaceableBlocks.get()));
        return status.toString();
    }
}
