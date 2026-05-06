package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class HelpCommands {

    private HelpCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zhelp")
                .requires(source -> source.hasPermission(2))
                .executes(context -> showHelp(context.getSource())));
    }

    private static int showHelp(CommandSourceStack source) {
        CommandUtil.feedback(source,
                "ZombieApocalypseAddon commands:\n"
                        + "/zhelp - show this help\n"
                        + "/zburn <true|false> - allow zombie daylight burning\n"
                        + "/zkill - remove all zombie-class mobs\n"
                        + "/zday [status|set <day>] - view or set the world day counter\n"
                        + "/zhorde <start|stop|status> - manage horde events\n"
                        + "/zbloodmoon - force a blood moon (now or tonight)\n"
                        + "/zstats [player|all|clear] - statistics and cooldown data\n"
                        + "/zscaling status - difficulty scaling status\n"
                        + "/zdayspawn status - view spawn settings\n"
                        + "/zdayspawn babychance <0.0-1.0> - set baby zombie chance; 0 disables baby zombies\n"
                        + "/zdayspawn daylightstart <day> - delay daytime custom spawning until that day counter\n"
                        + "/zdayspawn maxlight <-1-15> - max block light for custom spawns; -1 ignores light\n"
                        + "/zdayspawn daycounter <true|false> - toggle morning day counter titles\n"
                        + "/zdayspawn attributes <true|false> - toggle base attribute tuning\n"
                        + "/zdayspawn attributescaling <true|false> - scale attributes by day factor\n"
                        + "/zdayspawn variantprofiles <true|false> - toggle per-variant attribute profiles\n"
                        + "/zdayspawn contextprofiles <true|false> - toggle biome/dimension multipliers\n"
                        + "/zattr status - attribute tuning status\n"
                        + "/zattr keys [all] - list attribute tuning keys\n"
                        + "/zattr get <key> - read a numeric setting\n"
                        + "/zattr set <key> <value> - live-update numeric attribute setting\n"
                        + "/zattr toggle <key> <true|false> - live-update toggles (attributes.enabled, attributes.scaling, ...)\n"
                        + "/zdayspawn <option> <value> - update live settings",
                false);
        return 1;
    }
}
