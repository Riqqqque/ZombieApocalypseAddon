package com.rique.zombieapocalypse.commands;

import java.util.Locale;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

final class CommandUtil {

    private CommandUtil() {
    }

    static void feedback(CommandSourceStack source, String message, boolean broadcastToOps) {
        source.sendSuccess(() -> Component.literal(message), broadcastToOps);
    }

    static String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }

    static String percent(double value) {
        return String.format(Locale.ROOT, "%.0f%%", value * 100.0);
    }

    static String multiplier(double value) {
        return String.format(Locale.ROOT, "%.2fx", value);
    }
}
