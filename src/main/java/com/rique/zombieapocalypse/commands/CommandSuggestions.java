package com.rique.zombieapocalypse.commands;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

final class CommandSuggestions {

    static final String[] TOGGLE = { "on", "off", "true", "false" };
    static final String[] DAYS = { "0", "1", "5", "10", "15", "30", "50", "100" };

    private CommandSuggestions() {
    }

    static SuggestionProvider<CommandSourceStack> fixed(String... values) {
        String[] copy = Arrays.copyOf(values, values.length);
        return (context, builder) -> suggest(builder, copy);
    }

    static SuggestionProvider<CommandSourceStack> integers(IntSupplier current, int... commonValues) {
        return (context, builder) -> {
            Set<String> values = new LinkedHashSet<>();
            values.add(Integer.toString(current.getAsInt()));
            for (int value : commonValues) {
                values.add(Integer.toString(value));
            }
            return SharedSuggestionProvider.suggest(values, builder);
        };
    }

    static SuggestionProvider<CommandSourceStack> doubles(DoubleSupplier current, double... commonValues) {
        return (context, builder) -> {
            Set<String> values = new LinkedHashSet<>();
            values.add(format(current.getAsDouble()));
            for (double value : commonValues) {
                values.add(format(value));
            }
            return SharedSuggestionProvider.suggest(values, builder);
        };
    }

    static CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder, String... values) {
        return SharedSuggestionProvider.suggest(values, builder);
    }

    private static String format(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
