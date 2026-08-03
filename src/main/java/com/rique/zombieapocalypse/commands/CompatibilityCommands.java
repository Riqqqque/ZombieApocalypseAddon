package com.rique.zombieapocalypse.commands;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.ZombieCompatibility;

public final class CompatibilityCommands {

    private CompatibilityCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zcompat")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showStatus(context.getSource()))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource())))
                .then(toggleBoolNode("modded", Config.COMMON.enableModdedZombieCompatibility::set,
                        "Automatic modded zombie recognition"))
                .then(toggleBoolNode("difficulty", Config.COMMON.applyDifficultyToModdedZombies::set,
                        "Addon difficulty for modded zombies"))
                .then(toggleBoolNode("ai", Config.COMMON.applyAiFeaturesToModdedZombies::set,
                        "Addon AI features for modded zombies"))
                .then(toggleBoolNode("spawnrules", Config.COMMON.respectExternalSpawnRules::set,
                        "External spawn rules"))
                .then(toggleBoolNode("externalai", Config.COMMON.respectExternalZombieAi::set,
                        "External zombie AI ownership"))
                .then(toggleBoolNode("externaldifficulty", Config.COMMON.respectExternalDifficulty::set,
                        "External difficulty ownership"))
                .then(toggleBoolNode("doors", Config.COMMON.respectZombieDoorBreakingAbility::set,
                        "Zombie door protection"))
                .then(toggleBoolNode("equipment", Config.COMMON.preserveExistingZombieEquipment::set,
                        "Preserve existing zombie equipment")));
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

    private static int showStatus(CommandSourceStack source) {
        List<String> loadedMods = ZombieCompatibility.loadedKnownMods();
        String loaded = loadedMods.isEmpty() ? "none detected" : String.join(", ", loadedMods);

        StringBuilder status = new StringBuilder("Mod compatibility settings:\n");
        status.append("Known compatible mods loaded: ").append(loaded).append('\n');
        status.append("Recognize modded Zombie subclasses: ")
                .append(CommandUtil.onOff(Config.COMMON.enableModdedZombieCompatibility.get())).append('\n');
        status.append("Apply addon difficulty to modded zombies: ")
                .append(CommandUtil.onOff(Config.COMMON.applyDifficultyToModdedZombies.get())).append('\n');
        status.append("Apply addon AI features to modded zombies: ")
                .append(CommandUtil.onOff(Config.COMMON.applyAiFeaturesToModdedZombies.get())).append('\n');
        status.append("Respect external spawn rules: ")
                .append(CommandUtil.onOff(Config.COMMON.respectExternalSpawnRules.get())).append('\n');
        status.append("Avoid conflicting external AI: ")
                .append(CommandUtil.onOff(Config.COMMON.respectExternalZombieAi.get())).append('\n');
        status.append("Avoid double difficulty scaling: ")
                .append(CommandUtil.onOff(Config.COMMON.respectExternalDifficulty.get())).append('\n');
        status.append("Respect mods that disable door breaking: ")
                .append(CommandUtil.onOff(Config.COMMON.respectZombieDoorBreakingAbility.get())).append('\n');
        status.append("Preserve existing zombie equipment: ")
                .append(CommandUtil.onOff(Config.COMMON.preserveExistingZombieEquipment.get()));
        CommandUtil.feedback(source, status.toString(), false);
        return 1;
    }
}
