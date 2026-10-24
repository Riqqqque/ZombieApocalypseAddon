package com.rique.zombieapocalypse.commands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerLevel;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.ConfigGuard;
import com.rique.zombieapocalypse.ZombieBlockPlacer;
import com.rique.zombieapocalypse.DifficultyManager;
import com.rique.zombieapocalypse.HordeManager;
import com.rique.zombieapocalypse.SpawnMath;

public final class MainCommands {

    private MainCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> root = dispatcher.register(buildRoot(dispatcher));
        dispatcher.register(Commands.literal("zombieapocalypse")
                .executes(root.getCommand())
                .redirect(root));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildRoot(
            CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("za")
                .executes(context -> showStatus(context.getSource()));

        root.then(Commands.literal("status")
                .executes(context -> showStatus(context.getSource())));
        root.then(HelpCommands.buildNode("help"));
        root.then(Commands.literal("commands")
                .executes(context -> HelpCommands.showAllHelp(context.getSource())));
        root.then(Commands.literal("config")
                .executes(context -> showConfigHelp(context.getSource()))
                .then(CommandUtil.admin(Commands.literal("reset")
                        .executes(context -> showResetHelp(context.getSource()))
                        .then(Commands.argument("section", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(resetTargets(), builder))
                                .executes(context -> resetSection(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "section")))))));

        LiteralArgumentBuilder<CommandSourceStack> presets = Commands.literal("preset")
                .executes(context -> showPresets(context.getSource()));
        for (GameplayPreset preset : GameplayPreset.values()) {
            presets.then(CommandUtil.admin(Commands.literal(preset.name().toLowerCase(java.util.Locale.ROOT))
                    .executes(context -> applyPreset(context.getSource(), preset))));
        }
        root.then(presets);

        addRedirect(root, dispatcher, "spawn", "zdayspawn");
        addRedirect(root, dispatcher, "events", "zhorde");
        addRedirect(root, dispatcher, "bloodmoon", "zbloodmoon");
        addRedirect(root, dispatcher, "day", "zday");
        addRedirect(root, dispatcher, "scaling", "zscaling");
        addRedirect(root, dispatcher, "gear", "zgear");
        addRedirect(root, dispatcher, "stats", "zstats");
        addRedirect(root, dispatcher, "breaking", "zblockbreak");
        addRedirect(root, dispatcher, "placing", "zblockplace");
        addRedirect(root, dispatcher, "towering", "ztower");
        addRedirect(root, dispatcher, "attributes", "zattr");
        addRedirect(root, dispatcher, "compatibility", "zcompat");
        addRedirect(root, dispatcher, "burn", "zburn");
        addRedirect(root, dispatcher, "kill", "zkill");
        addRedirect(root, dispatcher, "hunt", "zhunt");
        addRedirect(root, dispatcher, "cleanup", "zcleanup");
        return root;
    }

    private static void addRedirect(
            LiteralArgumentBuilder<CommandSourceStack> root,
            CommandDispatcher<CommandSourceStack> dispatcher,
            String alias,
            String targetName) {
        CommandNode<CommandSourceStack> target = dispatcher.getRoot().getChild(targetName);
        if (target == null) {
            throw new IllegalStateException("Command target was not registered: " + targetName);
        }
        // A redirect alone leaves the bare alias incomplete, so the alias also runs the target's own command.
        root.then(Commands.literal(alias)
                .requires(target.getRequirement())
                .executes(target.getCommand())
                .redirect(target));
    }

    private static int applyPreset(CommandSourceStack source, GameplayPreset preset) {
        preset.apply();
        CommandUtil.feedback(source,
                preset.displayName() + " preset applied. " + preset.description()
                        + " Daytime custom waves were turned on. Optional block breaking, block placing, towering, and advanced attribute values were not changed.",
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
        message.append("Applying a preset requires permission level 2. Presets turn daytime custom waves on and change spawning, events, and basic scaling. They never enable world-damaging features.");
        CommandUtil.feedback(source, message.toString(), false);
        return 1;
    }

    private static int showConfigHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Config file: config/zombieapocalypseaddon-common.toml\n"
                        + "Start with [dayspawning], [variants], [horde], [bloodmoon], and [scaling].\n"
                        + "Zombie armor and weapon lists (tier1Gear to tier4Gear) are in [scaling]; /za gear edits them in game.\n"
                        + "Leave [compatibility] and [attributes] at their defaults unless you need advanced tuning.\n"
                        + "In-game commands are always safe and save right away. Feature on commands keep your other settings.\n"
                        + "Edit the file with the server stopped, or from the title screen in singleplayer.\n"
                        + "/za config reset <section> - put one section back to its defaults\n"
                        + "/za preset casual|standard|hardcore - replace the main settings with a ready-made setup",
                false);
        return 1;
    }

    private static List<String> resetTargets() {
        List<String> targets = new ArrayList<>(Config.sections());
        targets.add("all");
        return targets;
    }

    private static int showResetHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Config reset:\n"
                        + "/za config reset <section> puts every setting in that section back to its default.\n"
                        + "/za config reset all puts the whole config back to its defaults.\n"
                        + "Sections: " + String.join(", ", Config.sections()),
                false);
        return 1;
    }

    private static int resetSection(CommandSourceStack source, String input) {
        String section = input.toLowerCase(Locale.ROOT);
        boolean all = section.equals("all");
        if (!all && !Config.sections().contains(section)) {
            CommandUtil.failure(source, "Unknown config section '" + input + "'. Sections: "
                    + String.join(", ", Config.sections()) + ", or all.");
            return 0;
        }

        Map<List<String>, Object> current = Config.currentValues();
        Map<List<String>, Object> defaults = Config.defaultValues();
        Map<List<String>, Object> changes = new LinkedHashMap<>();
        defaults.forEach((path, defaultValue) -> {
            boolean inSection = all || path.get(0).equals(section);
            if (inSection && !ConfigGuard.sameValue(current.get(path), defaultValue)) {
                changes.put(path, defaultValue);
            }
        });

        String target = all ? "The whole config" : "[" + section + "]";
        if (changes.isEmpty()) {
            CommandUtil.feedback(source, target + " already matches the defaults.", false);
            return 1;
        }
        if (!CommandUtil.applied(source, () -> Config.setAll(changes))) {
            return 0;
        }
        if (all || section.equals("blockplacing")) {
            ZombieBlockPlacer.clearRuntimeState();
        }

        List<String> names = changes.keySet().stream().map(path -> path.get(path.size() - 1)).toList();
        String listed = names.size() <= 12
                ? String.join(", ", names)
                : String.join(", ", names.subList(0, 12)) + ", and " + (names.size() - 12) + " more";
        CommandUtil.feedback(source,
                target + ": reset " + CommandUtil.count(changes.size(), "setting") + " to defaults.\n" + listed,
                true);
        return 1;
    }

    private static int showStatus(CommandSourceStack source) {
        ServerLevel level = source.getServer().overworld();
        long day = DifficultyManager.getCurrentDay(level);
        boolean spawningEnabled = Config.COMMON.enableDaySpawning.get();
        boolean daytimeSpawningEnabled = Config.COMMON.enableDaytimeSpawning.get();
        boolean hordeScheduledAvailable = spawningEnabled
                && (daytimeSpawningEnabled || Config.COMMON.hordeStartsAtDusk.get());
        boolean hordeWavesNow = spawningEnabled && (daytimeSpawningEnabled || !level.isDay());
        int daylightStart = Config.COMMON.daylightSpawnStartDay.get();
        int lightLimit = Config.COMMON.maxBlockLightForSpawning.get();

        String daylight = !spawningEnabled
                ? "paused"
                : !daytimeSpawningEnabled
                        ? "OFF (night-only)"
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
                Config.COMMON.enableHordeEvents.get(), HordeManager.isHordeActive(level),
                hordeScheduledAvailable, hordeWavesNow))
                .append(" | blood moon ")
                .append(eventState(
                        Config.COMMON.enableBloodMoon.get(), HordeManager.isBloodMoonActive(level),
                        spawningEnabled, hordeWavesNow)).append('\n');
        status.append("Difficulty scaling: ").append(CommandUtil.onOff(Config.COMMON.enableDifficultyScaling.get()))
                .append(" (").append(CommandUtil.percent(DifficultyManager.getScalingFactor(level))).append(")\n");
        status.append("World pressure: breaking ").append(CommandUtil.onOff(Config.COMMON.enableZombieBlockBreaking.get()))
                .append(" | placing ").append(CommandUtil.onOff(Config.COMMON.enableZombieBlockPlacing.get()))
                .append(" | towering ").append(CommandUtil.onOff(Config.COMMON.enableZombieTowering.get()))
                .append(" | hunting ").append(CommandUtil.onOff(Config.COMMON.enableAnimalHunting.get())).append('\n');

        if (SpawnMath.isSpawnDistanceImpossible(
                Config.COMMON.minSpawnDistance.get(),
                Config.COMMON.spawnRange.get())) {
            status.append("WARNING: Spawn range is too small for the minimum distance. Custom waves are paused.\n");
        }
        status.append("Use /za preset for quick setup, /za help for guidance, or press Tab after /za.");

        CommandUtil.feedback(source, status.toString(), false);
        return 1;
    }

    private static String eventState(boolean enabled, boolean active, boolean scheduledAvailable, boolean wavesNow) {
        if (active) {
            return wavesNow ? "ACTIVE" : "ACTIVE (daytime blocked)";
        }
        if (!enabled) {
            return "OFF";
        }
        if (!scheduledAvailable) {
            return "PAUSED";
        }
        return "ON";
    }
}
