package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class HelpCommands {

    private HelpCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(buildNode("zhelp"));
    }

    static LiteralArgumentBuilder<CommandSourceStack> buildNode(String name) {
        LiteralArgumentBuilder<CommandSourceStack> help = Commands.literal(name)
                .executes(context -> showQuickHelp(context.getSource()));

        help.then(topic("start", HelpCommands::showStartHelp));
        help.then(topic("spawning", HelpCommands::showSpawningHelp));
        help.then(topic("events", HelpCommands::showEventHelp));
        help.then(topic("difficulty", HelpCommands::showDifficultyHelp));
        help.then(topic("bases", HelpCommands::showBaseHelp));
        help.then(topic("admin", HelpCommands::showAdminHelp));
        help.then(topic("advanced", HelpCommands::showAdvancedHelp));
        help.then(topic("all", HelpCommands::showAllHelp));
        help.then(topic("commands", HelpCommands::showAllHelp));
        return help;
    }

    static int showQuickHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Zombie Apocalypse quick help:\n"
                        + "/za - show the main dashboard\n"
                        + "/za preset - compare the three gameplay presets\n"
                        + "/za spawn - show the main spawn settings\n"
                        + "/za events - show horde and blood moon status\n"
                        + "/za stats - show your zombie kills\n"
                        + "/za help <topic> - focused examples for one system\n"
                        + "Press Tab after /za to see every command family. Existing /z... commands still work.",
                false);
        return 1;
    }

    private static int showStartHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "First setup:\n"
                        + "1. Run /za status to see what is active.\n"
                        + "2. An admin can run /za preset standard for the recommended balance.\n"
                        + "3. Use casual for a slower start or hardcore for more pressure.\n"
                        + "4. Run /za spawn if you only want to tune spawning.\n"
                        + "5. Run /za config to find the config and beginner sections.\n"
                        + "Presets never enable block breaking, block placing, or towering. Press Tab at any step for choices.",
                false);
        return 1;
    }

    private static int showSpawningHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Spawning commands:\n"
                        + "/za spawn - show the important spawn settings\n"
                        + "/za spawn on|off - enable or pause custom waves\n"
                        + "/za spawn daytime on|off - off makes custom spawning night-only\n"
                        + "/za spawn chance <0.0-1.0> - wave chance; 0.25 means 25%\n"
                        + "/za spawn interval <ticks> - time between checks; 20 ticks = 1 second\n"
                        + "/za spawn amount <1-50> - zombies attempted per wave\n"
                        + "/za spawn max <1-500> - nearby cap per player\n"
                        + "/za spawn daylightstart <day> - temporary daytime grace period\n"
                        + "/za spawn maxlight <-1-15> - -1 ignores lights; 7 protects bright bases\n"
                        + "/za spawn babychance <0.0-1.0> - use 0 to disable baby zombies\n"
                        + "Run a setting without a value to see its current value. Tab suggests useful values.",
                false);
        return 1;
    }

    private static int showEventHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Event commands:\n"
                        + "/za events - show horde and blood moon status\n"
                        + "/za events on|off - enable scheduled hordes with balanced defaults\n"
                        + "/za events start|stop - control a horde\n"
                        + "/za events interval|chance|duration - tune the schedule\n"
                        + "/za events multiplier|amount|spawninterval - tune event pressure\n"
                        + "/za bloodmoon on|off - enable random blood moons with balanced defaults\n"
                        + "/za bloodmoon start - start one now or queue it for tonight\n"
                        + "/za bloodmoon chance|multiplier|amount - tune blood moons\n"
                        + "/za spawn horde on|off - enable scheduled hordes\n"
                        + "/za spawn hordechance <0.0-1.0> - scheduled horde chance\n"
                        + "/za spawn bloodmoon on|off - enable random blood moons\n"
                        + "/za spawn daycounter on|off - toggle morning day titles\n"
                        + "Hordes and blood moons need custom waves to be on. Night-only mode pauses scheduled dawn hordes.",
                false);
        return 1;
    }

    private static int showDifficultyHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Difficulty commands:\n"
                        + "/za scaling - show current day-based scaling\n"
                        + "/za day - show the apocalypse day\n"
                        + "/za day set <day> - change the apocalypse day safely\n"
                        + "/za spawn scaling on|off - toggle day-based scaling\n"
                        + "/za scaling startday|maxday - set the progression window\n"
                        + "/za scaling speed|health|armorchance|weaponchance - tune full-strength bonuses\n"
                        + "/za spawn attributes on|off - toggle advanced attribute tuning\n"
                        + "Use /za help advanced only if you need exact stat profiles.",
                false);
        return 1;
    }

    private static int showBaseHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Base-pressure commands (all OFF by default):\n"
                        + "/za breaking [on|off|status] - let zombies damage allowed blocks\n"
                        + "/za placing [on|off|status] - let zombies build limited steps and bridges\n"
                        + "/za towering [on|off|status] - let zombie crowds form smart moving stacks\n"
                        + "/za towering unlimited - remove the count and height caps (use carefully)\n"
                        + "/za towering stacksize <count> - zombie cap; 0 is unlimited\n"
                        + "/za towering maxperplayer <count> - tower cap per target; 0 is unlimited\n"
                        + "/za towering dynamic on|off - follow the target's block height\n"
                        + "/za towering offset <blocks> - extra levels above the target in dynamic mode\n"
                        + "/za towering smartdismount on|off - safely collapse towers on reachable ground\n"
                        + "/za towering jumping on|off - control whether top zombies leap to attack\n"
                        + "/za towering jumpcooldown <ticks> - delay between jump attacks\n"
                        + "Each on command loads safe, usable defaults and starts on the current day.\n"
                        + "Use <command> startday <day> to delay a feature.\n"
                        + "Use <command> dayone to enable it immediately.\n"
                        + "Block breaking and placing respect mobGriefing by default.",
                false);
        return 1;
    }

    private static int showAdminHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Stats and maintenance commands:\n"
                        + "/za stats [player|all] - view kill totals\n"
                        + "/za stats clear - admin-only reset for stats, cooldowns, and milestones\n"
                        + "/za kill - remove loaded zombie-class mobs\n"
                        + "/za cleanup - remove loaded zombies and reset event state\n"
                        + "/za cleanup uninstall - disable gameplay systems before removing the mod\n"
                        + "/za burn [on|off] - view or change daylight burning\n"
                        + "/za compatibility - show mod compatibility safeguards",
                false);
        return 1;
    }

    private static int showAdvancedHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Advanced tuning:\n"
                        + "/za attributes - show attribute system state\n"
                        + "/za attributes keys [all] - explain or list keys\n"
                        + "/za attributes get <key> - read one value\n"
                        + "/za attributes set <key> <value> - change one numeric value\n"
                        + "/za attributes toggle <key> <on|off> - change an attribute toggle\n"
                        + "/za compatibility - inspect mixed-mod safeguards\n"
                        + "Most servers should leave advanced attributes and compatibility at their defaults.",
                false);
        return 1;
    }

    static int showAllHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Command families:\n"
                        + "/za spawn - spawning and main feature toggles\n"
                        + "/za events, /za bloodmoon - event controls\n"
                        + "/za day, /za scaling - day counter and difficulty\n"
                        + "/za breaking, /za placing, /za towering - optional base pressure\n"
                        + "/za stats - kill totals and milestone reset\n"
                        + "/za attributes, /za compatibility - advanced controls\n"
                        + "/za burn, /za kill, /za cleanup - utilities and removal\n"
                        + "Existing /z... roots remain compatible. Press Tab after /za or use /za help <topic>.",
                false);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> topic(
            String name,
            java.util.function.ToIntFunction<CommandSourceStack> handler) {
        return Commands.literal(name)
                .executes(context -> handler.applyAsInt(context.getSource()));
    }
}
