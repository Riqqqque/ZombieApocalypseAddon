package com.rique.zombieapocalypse.commands;

import java.math.BigDecimal;
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

    static String ticks(int ticks) {
        String seconds = BigDecimal.valueOf(ticks)
                .divide(BigDecimal.valueOf(20))
                .stripTrailingZeros()
                .toPlainString();
        return ticks + (ticks == 1 ? " tick (" : " ticks (")
                + seconds + (ticks == 20 ? " second)" : " seconds)");
    }

    static String count(int value, String singular) {
        return value + " " + singular + (value == 1 ? "" : "s");
    }

    static String number(double value) {
        return String.format(Locale.ROOT, "%.4f", value);
    }
}
