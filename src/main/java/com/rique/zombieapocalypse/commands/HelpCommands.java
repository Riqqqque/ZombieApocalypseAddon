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
                        + "/zhorde <start|stop|status> - manage horde events\n"
                        + "/zbloodmoon - force a blood moon (now or tonight)\n"
                        + "/zstats [player|all|clear] - statistics and cooldown data\n"
                        + "/zscaling status - difficulty scaling status\n"
                        + "/zdayspawn status - view spawn settings\n"
                        + "/zdayspawn <option> <value> - update live settings",
                false);
        return 1;
    }
}
