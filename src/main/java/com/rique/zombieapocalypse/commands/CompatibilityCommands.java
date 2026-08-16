package com.rique.zombieapocalypse.commands;

import java.util.List;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.ZombieCompatibility;

public final class CompatibilityCommands {

    private CompatibilityCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zcompat")
                .executes(context -> showStatus(context.getSource()))
                .then(CommandUtil.admin(Commands.literal("on")
                        .executes(context -> setEnabled(context.getSource(), true))))
                .then(CommandUtil.admin(Commands.literal("off")
                        .executes(context -> setEnabled(context.getSource(), false))))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource())))
                .then(CommandUtil.toggleSetting("modded", Config.COMMON.enableModdedZombieCompatibility::get,
                        CompatibilityCommands::setEnabledValue, "Automatic modded zombie recognition"))
                .then(CommandUtil.toggleSetting("difficulty", Config.COMMON.applyDifficultyToModdedZombies::get,
                        value -> Config.set(Config.COMMON.applyDifficultyToModdedZombies, value), "Addon difficulty for modded zombies"))
                .then(CommandUtil.toggleSetting("ai", Config.COMMON.applyAiFeaturesToModdedZombies::get,
                        value -> Config.set(Config.COMMON.applyAiFeaturesToModdedZombies, value), "Addon AI features for modded zombies"))
                .then(CommandUtil.toggleSetting("spawnrules", Config.COMMON.respectExternalSpawnRules::get,
                        value -> Config.set(Config.COMMON.respectExternalSpawnRules, value), "External spawn rules"))
                .then(CommandUtil.toggleSetting("externalai", Config.COMMON.respectExternalZombieAi::get,
                        value -> Config.set(Config.COMMON.respectExternalZombieAi, value), "External zombie AI ownership"))
                .then(CommandUtil.toggleSetting("externaldifficulty", Config.COMMON.respectExternalDifficulty::get,
                        value -> Config.set(Config.COMMON.respectExternalDifficulty, value), "External difficulty ownership"))
                .then(CommandUtil.toggleSetting("doors", Config.COMMON.respectZombieDoorBreakingAbility::get,
                        value -> Config.set(Config.COMMON.respectZombieDoorBreakingAbility, value), "Zombie door protection"))
                .then(CommandUtil.toggleSetting("equipment", Config.COMMON.preserveExistingZombieEquipment::get,
                        value -> Config.set(Config.COMMON.preserveExistingZombieEquipment, value), "Preserve existing zombie equipment")));
    }

    private static int setEnabled(CommandSourceStack source, boolean enabled) {
        if (enabled) {
            FeaturePresets.enableCompatibility();
            CommandUtil.feedback(source,
                    "Mod compatibility: ON\nRecommended conflict protection, equipment preservation, and external-rule safeguards were enabled.",
                    true);
        } else {
            Config.set(Config.COMMON.enableModdedZombieCompatibility, false);
            CommandUtil.feedback(source, "Mod compatibility: OFF", true);
        }
        return 1;
    }

    private static void setEnabledValue(boolean enabled) {
        if (enabled) {
            FeaturePresets.enableCompatibility();
        } else {
            Config.set(Config.COMMON.enableModdedZombieCompatibility, false);
        }
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
