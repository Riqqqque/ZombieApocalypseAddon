package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.DifficultyManager;
import com.rique.zombieapocalypse.ZombieBlockPlacer;
import com.rique.zombieapocalypse.ZombieClassMobs;

public final class BlockPlaceCommands {

    private BlockPlaceCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zblockplace")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showStatus(context.getSource()))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource())))
                .then(Commands.literal("dayone")
                        .executes(context -> {
                            Config.COMMON.enableZombieBlockPlacing.set(true);
                            Config.COMMON.zombieBlockPlacingStartDay.set(0);
                            CommandUtil.feedback(context.getSource(),
                                    "Zombie block placing enabled and set to start immediately.", true);
                            return 1;
                        }))
                .then(toggleBoolNode("enabled", Config.COMMON.enableZombieBlockPlacing::set,
                        "Zombie block placing"))
                .then(Commands.literal("startday")
                        .then(Commands.argument("day", IntegerArgumentType.integer(0, 3650))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "day");
                                    Config.COMMON.zombieBlockPlacingStartDay.set(value);
                                    String message = value <= 0
                                            ? "Zombie block placing can start immediately when enabled."
                                            : "Zombie block placing starts on day " + value + '.';
                                    CommandUtil.feedback(context.getSource(), message, true);
                                    return 1;
                                })))
                .then(Commands.literal("interval")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(20, 72000))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "ticks");
                                    Config.COMMON.zombieBlockPlacingInterval.set(value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie block placing interval: " + value + " ticks", true);
                                    return 1;
                                })))
                .then(Commands.literal("chance")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0, 1.0))
                                .executes(context -> {
                                    double value = DoubleArgumentType.getDouble(context, "value");
                                    Config.COMMON.zombieBlockPlacingChance.set(value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie block placing chance: " + CommandUtil.percent(value), true);
                                    return 1;
                                })))
                .then(Commands.literal("block")
                        .then(Commands.argument("id", ResourceLocationArgument.id())
                                .executes(context -> setPlacementBlock(
                                        context.getSource(),
                                        ResourceLocationArgument.getId(context, "id").toString()))))
                .then(Commands.literal("limit")
                        .then(Commands.argument("blocks", IntegerArgumentType.integer(0, 256))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "blocks");
                                    Config.COMMON.zombieBlockPlacingMaxPerZombie.set(value);
                                    String label = value == 0 ? "unlimited" : Integer.toString(value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie block placement limit per zombie: " + label, true);
                                    return 1;
                                })))
                .then(Commands.literal("distance")
                        .then(Commands.argument("blocks", IntegerArgumentType.integer(4, 128))
                                .executes(context -> {
                                    int value = IntegerArgumentType.getInteger(context, "blocks");
                                    Config.COMMON.zombieBlockPlacingMaxTargetDistance.set(value);
                                    CommandUtil.feedback(context.getSource(),
                                            "Zombie block placing target distance: " + value + " blocks", true);
                                    return 1;
                                })))
                .then(toggleBoolNode("target", Config.COMMON.zombieBlockPlacingRequireTarget::set,
                        "Require zombie target"))
                .then(toggleBoolNode("obstacle", Config.COMMON.zombieBlockPlacingRequireObstacle::set,
                        "Require obstacle, covered target, or gap"))
                .then(toggleBoolNode("mobgriefing", Config.COMMON.zombieBlockPlacingRespectMobGriefing::set,
                        "Respect mobGriefing"))
                .then(toggleBoolNode("bridges", Config.COMMON.zombieBlockPlacingAllowBridges::set,
                        "Zombie gap bridging"))
                .then(toggleBoolNode("steps", Config.COMMON.zombieBlockPlacingAllowSteps::set,
                        "Zombie step placing"))
                .then(toggleBoolNode("fluids", Config.COMMON.zombieBlockPlacingReplaceFluids::set,
                        "Replace fluid blocks"))
                .then(toggleBoolNode("replaceable",
                        Config.COMMON.zombieBlockPlacingReplaceReplaceableBlocks::set,
                        "Replace plants/snow/other replaceable blocks"))
                .then(Commands.literal("resetcounts")
                        .executes(context -> {
                            int reset = resetLoadedPlacementCounts(context.getSource());
                            CommandUtil.feedback(context.getSource(),
                                    "Reset block placement counts for " + reset + " loaded zombie-class mobs.",
                                    true);
                            return Math.max(1, reset);
                        })));
    }

    private static int setPlacementBlock(CommandSourceStack source, String blockId) {
        String error = ZombieBlockPlacer.validatePlacementBlock(
                source.getLevel(),
                BlockPos.containing(source.getPosition()),
                blockId);
        if (error != null) {
            source.sendFailure(Component.literal(error));
            return 0;
        }

        Config.COMMON.zombieBlockPlacingBlock.set(blockId);
        ZombieBlockPlacer.clearRuntimeState();
        CommandUtil.feedback(source, "Zombie placement block: " + blockId, true);
        return 1;
    }

    private static int showStatus(CommandSourceStack source) {
        CommandUtil.feedback(source, buildStatusMessage(source), false);
        return 1;
    }

    private static com.mojang.brigadier.builder.ArgumentBuilder<CommandSourceStack, ?> toggleBoolNode(
            String literal,
            java.util.function.Consumer<Boolean> setter,
            String label) {
        return Commands.literal(literal)
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                            setter.accept(enabled);
                            CommandUtil.feedback(context.getSource(), label + ": " + CommandUtil.onOff(enabled), true);
                            return 1;
                        }));
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
        status.append("Interval: ").append(Config.COMMON.zombieBlockPlacingInterval.get()).append(" ticks\n");
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
