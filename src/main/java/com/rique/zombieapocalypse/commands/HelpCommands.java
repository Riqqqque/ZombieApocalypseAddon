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
                        + "/zcleanup [uninstall] - clear loaded zombie leftovers and reset apocalypse event state\n"
                        + "/zcompat [status] - view loaded mod integrations and compatibility settings\n"
                        + "/zcompat <modded|difficulty|ai|spawnrules|externalai|externaldifficulty|doors|equipment> <true|false> - change compatibility behavior\n"
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
                        + "/zblockbreak status - view zombie block-breaking settings\n"
                        + "/zblockbreak enabled <true|false> - toggle zombie block breaking\n"
                        + "/zblockbreak dayone - enable block breaking and start it immediately\n"
                        + "/zblockbreak startday <day> - set the day block breaking can begin\n"
                        + "/zblockbreak chance <0.0-1.0> - set block-breaking chance per interval\n"
                        + "/zblockbreak hardness <0.0-50.0> - set max breakable block hardness\n"
                        + "/zblockplace status - view zombie block-placing settings\n"
                        + "/zblockplace dayone - enable block placing and start it immediately\n"
                        + "/zblockplace block <namespace:id> - choose the solid block zombies place\n"
                        + "/zblockplace limit <0-256> - set the lifetime limit per zombie; 0 is unlimited\n"
                        + "/zblockplace bridges <true|false> - toggle one-block gap bridging\n"
                        + "/zblockplace steps <true|false> - toggle one-block step placement\n"
                        + "/ztower status - view World War Z-style zombie towering settings\n"
                        + "/ztower dayone - enable towering and start it immediately\n"
                        + "/ztower startday <day> - set the day towering can begin\n"
                        + "/ztower enabled <true|false> - toggle zombie towering\n"
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
