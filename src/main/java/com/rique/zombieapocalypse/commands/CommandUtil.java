package com.rique.zombieapocalypse.commands;

import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

final class CommandUtil {

    @FunctionalInterface
    interface SourceToggleConsumer {
        void accept(CommandSourceStack source, boolean value);
    }

    @FunctionalInterface
    interface SourceIntConsumer {
        void accept(CommandSourceStack source, int value);
    }

    private static final DynamicCommandExceptionType INVALID_TOGGLE = new DynamicCommandExceptionType(value ->
            Component.literal("Unknown state '" + value + "'. Use on or off (true and false also work)."));

    private CommandUtil() {
    }

    static void feedback(CommandSourceStack source, String message, boolean broadcastToOps) {
        source.sendSuccess(() -> themed(message), broadcastToOps);
    }

    static void failure(CommandSourceStack source, String message) {
        source.sendFailure(Component.literal(message).withStyle(ChatFormatting.RED));
    }

    static <T extends ArgumentBuilder<CommandSourceStack, T>> T admin(T node) {
        return node.requires(source -> source != null && source.hasPermission(2));
    }

    static RequiredArgumentBuilder<CommandSourceStack, String> toggleArgument(String name) {
        return Commands.argument(name, StringArgumentType.word())
                .suggests(CommandSuggestions.fixed(CommandSuggestions.TOGGLE));
    }

    static boolean getToggle(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return parseToggle(StringArgumentType.getString(context, name));
    }

    static boolean parseToggle(String input) throws CommandSyntaxException {
        String value = input.toLowerCase(Locale.ROOT);
        return switch (value) {
            case "on", "true" -> true;
            case "off", "false" -> false;
            default -> throw INVALID_TOGGLE.create(value);
        };
    }

    static LiteralArgumentBuilder<CommandSourceStack> toggleSetting(
            String literal,
            BooleanSupplier getter,
            Consumer<Boolean> setter,
            String label) {
        return toggleSetting(literal, getter, (source, value) -> setter.accept(value), label);
    }

    static LiteralArgumentBuilder<CommandSourceStack> toggleSetting(
            String literal,
            BooleanSupplier getter,
            SourceToggleConsumer setter,
            String label) {
        return Commands.literal(literal)
                .executes(context -> {
                    feedback(context.getSource(), label + ": " + onOff(getter.getAsBoolean()), false);
                    return 1;
                })
                .then(admin(toggleArgument("state")
                        .executes(context -> {
                            boolean enabled = getToggle(context, "state");
                            if (!applied(context.getSource(), () -> setter.accept(context.getSource(), enabled))) {
                                return 0;
                            }
                            feedback(context.getSource(), label + ": " + onOff(enabled), true);
                            return 1;
                        })));
    }

    @FunctionalInterface
    interface NotedToggleConsumer {
        List<String> accept(CommandSourceStack source, boolean value);
    }

    @FunctionalInterface
    interface SourceToggleCommand {
        int run(CommandSourceStack source, boolean value);
    }

    /** A feature switch whose "on" may repair a broken value; those repairs are listed after the state. */
    static LiteralArgumentBuilder<CommandSourceStack> featureToggle(
            String literal,
            BooleanSupplier getter,
            NotedToggleConsumer setter,
            String label) {
        return Commands.literal(literal)
                .executes(context -> {
                    feedback(context.getSource(), label + ": " + onOff(getter.getAsBoolean()), false);
                    return 1;
                })
                .then(admin(toggleArgument("state")
                        .executes(context -> {
                            boolean enabled = getToggle(context, "state");
                            List<List<String>> notes = new ArrayList<>();
                            if (!applied(context.getSource(), () -> notes.add(setter.accept(context.getSource(), enabled)))) {
                                return 0;
                            }
                            feedback(context.getSource(), withNotes(label + ": " + onOff(enabled), notes.get(0)), true);
                            return 1;
                        })));
    }

    /** A switch that runs the family's own on/off command, so both forms give the same feedback. */
    static LiteralArgumentBuilder<CommandSourceStack> switchSetting(
            String literal,
            BooleanSupplier getter,
            SourceToggleCommand command,
            String label) {
        return Commands.literal(literal)
                .executes(context -> {
                    feedback(context.getSource(), label + ": " + onOff(getter.getAsBoolean()), false);
                    return 1;
                })
                .then(admin(toggleArgument("state")
                        .executes(context -> command.run(context.getSource(), getToggle(context, "state")))));
    }

    /** Runs a config change and reports a save problem as a normal command error. */
    static boolean applied(CommandSourceStack source, Runnable change) {
        try {
            change.run();
            return true;
        } catch (IllegalStateException | IllegalArgumentException | UncheckedIOException e) {
            failure(source, "Could not save the setting: " + e.getMessage());
            return false;
        }
    }

    static String withNotes(String headline, List<String> notes) {
        if (notes == null || notes.isEmpty()) {
            return headline;
        }
        return headline + '\n' + String.join("\n", notes);
    }

    /** One line saying whether a start-day feature runs today, and how to start it now if not. */
    static String startLine(long currentDay, int startDay, String startNowCommand) {
        if (currentDay >= startDay) {
            return "Active now (current day " + currentDay + ", start day " + startDay + ").";
        }
        return "Starts on day " + startDay + " (today is day " + currentDay + "). " + startNowCommand + " starts it now.";
    }

    /** Footer for feature "on" commands, so admins know nothing else was overwritten. */
    static String keptLine(String section) {
        return "Your other settings were kept. /za config reset " + section + " restores this section's defaults.";
    }

    /**
     * Turns a feature on and reports the result: the state, when it runs (for start-day features),
     * any value that had to be repaired, and a reminder that nothing else changed.
     */
    static int turnOn(
            CommandSourceStack source,
            String label,
            String section,
            Supplier<List<String>> enable,
            Supplier<String> startLine) {
        List<String> notes = new ArrayList<>();
        if (!applied(source, () -> notes.addAll(enable.get()))) {
            return 0;
        }
        StringBuilder message = new StringBuilder(label).append(": ON");
        if (startLine != null) {
            message.append('\n').append(startLine.get());
        }
        notes.forEach(note -> message.append('\n').append(note));
        message.append('\n').append(keptLine(section));
        feedback(source, message.toString(), true);
        return 1;
    }

    static int turnOff(CommandSourceStack source, String label, Runnable disable, String detail) {
        if (!applied(source, disable)) {
            return 0;
        }
        feedback(source, detail == null ? label + ": OFF" : label + ": OFF\n" + detail, true);
        return 1;
    }

    static LiteralArgumentBuilder<CommandSourceStack> intSetting(
            String literal,
            String argument,
            int minimum,
            int maximum,
            IntSupplier getter,
            IntConsumer setter,
            IntFunction<String> message,
            int... commonValues) {
        return intSetting(literal, argument, minimum, maximum, getter,
                (source, value) -> setter.accept(value), message, commonValues);
    }

    static LiteralArgumentBuilder<CommandSourceStack> intSetting(
            String literal,
            String argument,
            int minimum,
            int maximum,
            IntSupplier getter,
            SourceIntConsumer setter,
            IntFunction<String> message,
            int... commonValues) {
        return Commands.literal(literal)
                .executes(context -> {
                    feedback(context.getSource(), message.apply(getter.getAsInt()), false);
                    return 1;
                })
                .then(admin(Commands.argument(argument, IntegerArgumentType.integer(minimum, maximum))
                        .suggests(CommandSuggestions.integers(getter, commonValues))
                        .executes(context -> {
                            int value = IntegerArgumentType.getInteger(context, argument);
                            if (!applied(context.getSource(), () -> setter.accept(context.getSource(), value))) {
                                return 0;
                            }
                            feedback(context.getSource(), message.apply(value), true);
                            return 1;
                        })));
    }

    static LiteralArgumentBuilder<CommandSourceStack> doubleSetting(
            String literal,
            String argument,
            double minimum,
            double maximum,
            DoubleSupplier getter,
            DoubleConsumer setter,
            DoubleFunction<String> message,
            double... commonValues) {
        return Commands.literal(literal)
                .executes(context -> {
                    feedback(context.getSource(), message.apply(getter.getAsDouble()), false);
                    return 1;
                })
                .then(admin(Commands.argument(argument, DoubleArgumentType.doubleArg(minimum, maximum))
                        .suggests(CommandSuggestions.doubles(getter, commonValues))
                        .executes(context -> {
                            double value = DoubleArgumentType.getDouble(context, argument);
                            if (!applied(context.getSource(), () -> setter.accept(value))) {
                                return 0;
                            }
                            feedback(context.getSource(), message.apply(value), true);
                            return 1;
                        })));
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

    private static Component themed(String message) {
        MutableComponent result = Component.empty();
        String[] lines = message.split("\\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                result.append("\n");
            }
            result.append(themedLine(lines[i], i == 0));
        }
        return result;
    }

    private static Component themedLine(String line, boolean firstLine) {
        if (line.isEmpty()) {
            return Component.empty();
        }
        if (line.startsWith("WARNING")) {
            return Component.literal(line).withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        }
        if (firstLine && line.endsWith(":")) {
            return Component.literal(line).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        }
        if (line.startsWith("/")) {
            int descriptionStart = line.indexOf(" - ");
            if (descriptionStart < 0) {
                return Component.literal(line).withStyle(ChatFormatting.AQUA);
            }
            return Component.literal(line.substring(0, descriptionStart)).withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(line.substring(descriptionStart)).withStyle(ChatFormatting.GRAY));
        }

        int separator = line.indexOf(": ");
        if (separator > 0 && separator <= 48) {
            MutableComponent valueLine = Component.literal(line.substring(0, separator))
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY));
            valueLine.append(Component.literal(line.substring(separator + 2))
                    .withStyle(valueColor(line.substring(separator + 2))));
            return valueLine;
        }
        return Component.literal(line).withStyle(ChatFormatting.GRAY);
    }

    private static ChatFormatting valueColor(String value) {
        if (value.startsWith("ON") || value.startsWith("ENABLED")) {
            return ChatFormatting.GREEN;
        }
        if (value.startsWith("OFF") || value.startsWith("DISABLED") || value.startsWith("PAUSED")) {
            return ChatFormatting.RED;
        }
        if (value.startsWith("ACTIVE")) {
            return ChatFormatting.LIGHT_PURPLE;
        }
        return ChatFormatting.WHITE;
    }
}
