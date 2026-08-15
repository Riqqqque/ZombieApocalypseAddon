package com.rique.zombieapocalypse.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

class CommandSuggestionsTest {

    @Test
    void toggleSuggestionsUseReadableStatesAndKeepLegacyValues() throws Exception {
        List<String> suggestions = CommandSuggestions.fixed(CommandSuggestions.TOGGLE)
                .getSuggestions(null, new SuggestionsBuilder("", 0))
                .get()
                .getList()
                .stream()
                .map(Suggestion::getText)
                .toList();

        assertEquals(List.of("false", "off", "on", "true"), suggestions);
    }

    @Test
    void numericSuggestionsIncludeCurrentValueWithoutDuplicates() throws Exception {
        List<String> integers = CommandSuggestions.integers(() -> 25, 10, 25, 50)
                .getSuggestions(null, new SuggestionsBuilder("", 0))
                .get()
                .getList()
                .stream()
                .map(Suggestion::getText)
                .toList();
        List<String> doubles = CommandSuggestions.doubles(() -> 0.25, 0.0, 0.25, 0.5)
                .getSuggestions(null, new SuggestionsBuilder("", 0))
                .get()
                .getList()
                .stream()
                .map(Suggestion::getText)
                .toList();

        assertEquals(List.of("10", "25", "50"), integers);
        assertEquals(List.of("0", "0.25", "0.5"), doubles);
    }
}
