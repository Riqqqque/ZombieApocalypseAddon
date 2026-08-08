package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.DifficultyManager;
import com.rique.zombieapocalypse.HordeManager;
import com.rique.zombieapocalypse.SpawnMath;

public final class MainCommands {

    private MainCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(buildRoot("zombieapocalypse"));
        dispatcher.register(buildRoot("za"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildRoot(String name) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(name)
                .executes(context -> showStatus(context.getSource()));

        root.then(Commands.literal("status")
                .executes(context -> showStatus(context.getSource())));
        root.then(HelpCommands.buildNode("help"));
        root.then(Commands.literal("commands")
                .executes(context -> HelpCommands.showAllHelp(context.getSource())));
        root.then(Commands.literal("config")
                .executes(context -> showConfigHelp(context.getSource())));

        LiteralArgumentBuilder<CommandSourceStack> presets = Commands.literal("preset")
                .executes(context -> showPresets(context.getSource()));
        for (GameplayPreset preset : GameplayPreset.values()) {
            presets.then(Commands.literal(preset.name().toLowerCase(java.util.Locale.ROOT))
                    .requires(source -> source.hasPermission(2))
                    .executes(context -> applyPreset(context.getSource(), preset)));
        }
        root.then(presets);
        return root;
    }

    private static int applyPreset(CommandSourceStack source, GameplayPreset preset) {
        preset.apply();
        CommandUtil.feedback(source,
                preset.displayName() + " preset applied. " + preset.description()
                        + " Optional block breaking, block placing, towering, and advanced attribute values were not changed.",
                true);
        return 1;
    }

    private static int showPresets(CommandSourceStack source) {
        StringBuilder message = new StringBuilder("Gameplay presets:\n");
        for (GameplayPreset preset : GameplayPreset.values()) {
            message.append("/za preset ")
                    .append(preset.name().toLowerCase(java.util.Locale.ROOT))
                    .append(" - ")
                    .append(preset.description())
                    .append('\n');
        }
        message.append("Applying a preset requires permission level 2. Presets change spawning, events, and basic scaling only. They never enable world-damaging features.");
        CommandUtil.feedback(source, message.toString(), false);
        return 1;
    }

    private static int showConfigHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Config file: config/zombieapocalypseaddon-common.toml\n"
                        + "Start with [dayspawning], [variants], [horde], [bloodmoon], and [scaling].\n"
                        + "Leave [compatibility] and [attributes] at their defaults unless you need advanced tuning.\n"
                        + "For a fast setup, use /za preset casual, standard, or hardcore. Stop the server before manual edits.",
                false);
        return 1;
    }

    private static int showStatus(CommandSourceStack source) {
        ServerLevel level = source.getServer().overworld();
        long day = DifficultyManager.getCurrentDay(level);
        boolean spawningEnabled = Config.COMMON.enableDaySpawning.get();
        int daylightStart = Config.COMMON.daylightSpawnStartDay.get();
        int lightLimit = Config.COMMON.maxBlockLightForSpawning.get();

        String daylight = !spawningEnabled
                ? "paused"
                : day >= daylightStart ? "active" : "starts day " + daylightStart;
        String lightProtection = lightLimit < 0
                ? "OFF (block light ignored)"
                : "ON (spawns need light " + lightLimit + " or lower)";

        StringBuilder status = new StringBuilder("Zombie Apocalypse dashboard:\n");
        status.append("Preset: ").append(GameplayPreset.currentName())
                .append(" | World day: ").append(day).append('\n');
        status.append("Custom waves: ").append(CommandUtil.onOff(spawningEnabled))
                .append(" | ").append(CommandUtil.percent(Config.COMMON.daySpawnChance.get()))
                .append(" every ").append(CommandUtil.ticks(Config.COMMON.daySpawnInterval.get()))
                .append(" | ").append(CommandUtil.count(Config.COMMON.zombiesPerSpawn.get(), "zombie"))
                .append(" per wave\n");
        status.append("Nearby cap: ").append(Config.COMMON.maxDayZombiesPerPlayer.get())
                .append(" per player | Daytime: ").append(daylight).append('\n');
        status.append("Base light protection: ").append(lightProtection).append('\n');
        status.append("Events: horde ").append(eventState(
                Config.COMMON.enableHordeEvents.get(), HordeManager.isHordeActive(level), spawningEnabled))
                .append(" | blood moon ")
                .append(eventState(
                        Config.COMMON.enableBloodMoon.get(), HordeManager.isBloodMoonActive(level), spawningEnabled)).append('\n');
        status.append("Difficulty scaling: ").append(CommandUtil.onOff(Config.COMMON.enableDifficultyScaling.get()))
                .append(" (").append(CommandUtil.percent(DifficultyManager.getScalingFactor(level))).append(")\n");
        status.append("World pressure: breaking ").append(CommandUtil.onOff(Config.COMMON.enableZombieBlockBreaking.get()))
                .append(" | placing ").append(CommandUtil.onOff(Config.COMMON.enableZombieBlockPlacing.get()))
                .append(" | towering ").append(CommandUtil.onOff(Config.COMMON.enableZombieTowering.get())).append('\n');

        if (SpawnMath.isSpawnDistanceImpossible(
                Config.COMMON.minSpawnDistance.get(),
                Config.COMMON.spawnRange.get())) {
            status.append("WARNING: Spawn range is too small for the minimum distance. Custom waves are paused.\n");
        }
        status.append("Use /za preset for quick setup or /za help for command topics.");

        CommandUtil.feedback(source, status.toString(), false);
        return 1;
    }

    private static String eventState(boolean enabled, boolean active, boolean customSpawningEnabled) {
        if (active) {
            return "ACTIVE";
        }
        if (!customSpawningEnabled) {
            return "PAUSED";
        }
        return CommandUtil.onOff(enabled);
    }
}
