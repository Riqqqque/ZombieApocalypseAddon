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
                        + "/zdayspawn - show the main spawn settings\n"
                        + "/zhorde - show active event status\n"
                        + "/zstats - show your zombie kills\n"
                        + "/zblockbreak, /zblockplace, /ztower - optional base-pressure features\n"
                        + "/zcleanup uninstall - prepare a world before removing the mod\n"
                        + "More help: /za help <start|spawning|events|difficulty|bases|admin|advanced|all>",
                false);
        return 1;
    }

    private static int showStartHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "First setup:\n"
                        + "1. Run /za status to see what is active.\n"
                        + "2. An admin can run /za preset standard for the recommended balance.\n"
                        + "3. Use casual for a slower start or hardcore for more pressure.\n"
                        + "4. Run /zdayspawn if you only want to tune spawning.\n"
                        + "5. Run /za config to find the config and beginner sections.\n"
                        + "Presets never enable block breaking, block placing, or towering.",
                false);
        return 1;
    }

    private static int showSpawningHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Spawning commands:\n"
                        + "/zdayspawn - show the important spawn settings\n"
                        + "/zdayspawn on|off - enable or pause custom waves\n"
                        + "/zdayspawn daytime <true|false> - false makes custom spawning night-only\n"
                        + "/zdayspawn chance <0.0-1.0> - wave chance; 0.25 means 25%\n"
                        + "/zdayspawn interval <ticks> - time between checks; 20 ticks = 1 second\n"
                        + "/zdayspawn amount <1-50> - zombies attempted per wave\n"
                        + "/zdayspawn max <1-500> - nearby cap per player\n"
                        + "/zdayspawn daylightstart <day> - temporary daytime grace period\n"
                        + "/zdayspawn maxlight <-1-15> - -1 ignores lights; 7 lets bright bases block spawns\n"
                        + "/zdayspawn babychance <0.0-1.0> - use 0 to disable baby zombies\n"
                        + "/zdayspawn status all - show every live spawn-related setting",
                false);
        return 1;
    }

    private static int showEventHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Event commands:\n"
                        + "/zhorde - show horde and blood moon status\n"
                        + "/zhorde start - start a horde now\n"
                        + "/zhorde stop - stop the active horde\n"
                        + "/zbloodmoon - start a blood moon now or queue it for tonight\n"
                        + "/zdayspawn horde <true|false> - enable scheduled hordes\n"
                        + "/zdayspawn hordechance <0.0-1.0> - scheduled horde chance\n"
                        + "/zdayspawn bloodmoon <true|false> - enable random blood moons\n"
                        + "/zdayspawn daycounter <true|false> - toggle morning day titles\n"
                        + "Hordes and blood moons need custom waves to be on. Night-only mode pauses scheduled dawn hordes.",
                false);
        return 1;
    }

    private static int showDifficultyHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Difficulty commands:\n"
                        + "/zscaling - show current day-based scaling\n"
                        + "/zday - show the apocalypse day\n"
                        + "/zday set <day> - change the apocalypse day safely\n"
                        + "/zdayspawn scaling <true|false> - toggle day-based scaling\n"
                        + "/zdayspawn attributes <true|false> - toggle advanced attribute tuning\n"
                        + "Use /za help advanced only if you need exact stat profiles.",
                false);
        return 1;
    }

    private static int showBaseHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Base-pressure commands (all OFF by default):\n"
                        + "/zblockbreak [on|off|status] - let zombies damage allowed blocks\n"
                        + "/zblockplace [on|off|status] - let zombies build limited steps and bridges\n"
                        + "/ztower [on|off|status] - let zombie crowds climb over each other\n"
                        + "Use <command> startday <day> to delay a feature.\n"
                        + "Use <command> dayone to enable it immediately.\n"
                        + "Block breaking and placing respect mobGriefing by default.",
                false);
        return 1;
    }

    private static int showAdminHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Stats and maintenance commands:\n"
                        + "/zstats [player|all] - view kill totals\n"
                        + "/zstats clear - admin-only reset for stats, cooldowns, and milestones\n"
                        + "/zkill - remove loaded zombie-class mobs\n"
                        + "/zcleanup - remove loaded zombies and reset event state\n"
                        + "/zcleanup uninstall - disable gameplay systems before removing the mod\n"
                        + "/zburn <true|false> - choose whether zombies burn in daylight\n"
                        + "/zcompat - show mod compatibility safeguards",
                false);
        return 1;
    }

    private static int showAdvancedHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Advanced tuning:\n"
                        + "/zattr status - show attribute system state\n"
                        + "/zattr keys - explain key format\n"
                        + "/zattr keys all - print every available key\n"
                        + "/zattr get <key> - read one value\n"
                        + "/zattr set <key> <value> - change one numeric value\n"
                        + "/zattr toggle <key> <true|false> - change an attribute toggle\n"
                        + "/zcompat status - inspect mixed-mod safeguards\n"
                        + "Most servers should leave advanced attributes and compatibility at their defaults.",
                false);
        return 1;
    }

    static int showAllHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "Command families:\n"
                        + "/za - dashboard, presets, config help, and command overview\n"
                        + "/zdayspawn - custom spawning and main feature toggles\n"
                        + "/zhorde, /zbloodmoon - event controls\n"
                        + "/zday, /zscaling - day counter and difficulty\n"
                        + "/zblockbreak, /zblockplace, /ztower - optional base pressure\n"
                        + "/zstats - kill totals and milestone reset\n"
                        + "/zattr - advanced numeric attributes\n"
                        + "/zcompat - mixed-mod compatibility\n"
                        + "/zburn, /zkill, /zcleanup - utilities and removal\n"
                        + "Use /za help <topic> for examples. Full reference: github.com/Riqqqque/ZombieApocalypseAddon/wiki/Commands",
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
