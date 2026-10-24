package com.rique.zombieapocalypse.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.common.ModConfigSpec;

import com.rique.zombieapocalypse.Config;

/**
 * Writes build/docs/command-tree.txt and build/docs/Config-Reference.md so the wiki can be
 * checked against, and generated from, the real command tree and config spec.
 */
class DocsGeneratorTest {

    @Test
    void dumpCommandTree() throws IOException {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        CommandRegistrar.registerAll(dispatcher);
        List<String> lines = new ArrayList<>();
        for (CommandNode<CommandSourceStack> root : dispatcher.getRoot().getChildren()) {
            if (root.getRedirect() != null && !root.getName().equals("zombieapocalypse")) {
                continue;
            }
            walk(root, "/" + root.getName(), lines, 0);
        }
        Path out = Path.of("build", "docs");
        Files.createDirectories(out);
        Files.write(out.resolve("command-tree.txt"), lines);
    }

    @Test
    void dumpConfigReference() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("# Config Reference");
        lines.add("");
        lines.add("Every setting in `config/zombieapocalypseaddon-common.toml`, with its default, the values it accepts, and"
                + " what it does. This page is generated from the mod itself, so it always matches the current version.");
        lines.add("");
        lines.add("- Change settings in game with `/za` commands; they save right away and never reset other settings.");
        lines.add("- Chances use decimals: `0.25` is 25%. `20` ticks is one second.");
        lines.add("- `/za config reset <section>` puts a section back to these defaults.");
        lines.add("");
        section(Config.COMMON_SPEC.getSpec(), new ArrayList<>(), lines);
        Path out = Path.of("build", "docs");
        Files.createDirectories(out);
        Files.write(out.resolve("Config-Reference.md"), lines);
    }

    private static void section(UnmodifiableConfig spec, List<String> path, List<String> lines) {
        List<String[]> rows = new ArrayList<>();
        List<String> children = new ArrayList<>();
        for (UnmodifiableConfig.Entry entry : spec.entrySet()) {
            Object value = entry.getRawValue();
            if (value instanceof UnmodifiableConfig) {
                children.add(entry.getKey());
            } else if (value instanceof ModConfigSpec.ValueSpec valueSpec) {
                rows.add(new String[] {
                        "`" + entry.getKey() + "`",
                        "`" + format(valueSpec.getDefault()) + "`",
                        allowed(valueSpec),
                        cell(valueSpec.getComment())});
            }
        }

        if (!path.isEmpty()) {
            lines.add((path.size() == 1 ? "## " : "### ") + "[" + String.join(".", path) + "]");
            lines.add("");
            String comment = Config.COMMON_SPEC.getLevelComment(path);
            if (comment != null) {
                for (String line : comment.split("\n")) {
                    String text = line.strip();
                    if (text.isEmpty() || text.chars().allMatch(c -> c == '=' || c == '-' || c == ' ')) {
                        continue;
                    }
                    if (text.startsWith("-----")) {
                        text = text.replace("-", "").strip();
                    }
                    lines.add(text);
                    lines.add("");
                }
            }
        }
        if (!rows.isEmpty()) {
            lines.add("| Setting | Default | Allowed | What it does |");
            lines.add("|---|---|---|---|");
            for (String[] row : rows) {
                lines.add("| " + String.join(" | ", row) + " |");
            }
            lines.add("");
        }
        for (String child : children) {
            List<String> childPath = new ArrayList<>(path);
            childPath.add(child);
            section(spec.get(List.of(child)), childPath, lines);
        }
    }

    private static String allowed(ModConfigSpec.ValueSpec spec) {
        Object def = spec.getDefault();
        if (def instanceof Boolean) {
            return "`true` / `false`";
        }
        ModConfigSpec.Range<?> range = spec.getRange();
        if (range != null) {
            return "`" + format(range.getMin()) + "` to `" + format(range.getMax()) + "`";
        }
        if (def instanceof String) {
            return "text";
        }
        return "";
    }

    private static String format(Object value) {
        if (value instanceof Double number) {
            if (number == Math.rint(number) && Math.abs(number) < 1e9) {
                return String.format(Locale.ROOT, "%.1f", number);
            }
            return String.valueOf(number);
        }
        if (value instanceof String text) {
            return text.isEmpty() ? "\"\"" : text;
        }
        return String.valueOf(value);
    }

    private static String cell(String comment) {
        if (comment == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (String line : comment.split("\n")) {
            String text = line.strip();
            if (text.isEmpty() || text.startsWith("Range:") || text.startsWith("Allowed Values:") || text.startsWith("Default:")) {
                continue;
            }
            parts.add(text.replace("|", "\\|"));
        }
        return String.join(" ", parts);
    }

    private static void walk(CommandNode<CommandSourceStack> node, String path, List<String> lines, int depth) {
        if (depth > 12) {
            return;
        }
        boolean admin = !node.getRequirement().test(null);
        String marker = (node.getCommand() != null ? "RUN " : "    ") + (admin ? "OP " : "   ");
        if (node.getRedirect() != null) {
            lines.add(marker + path + "  -> /" + node.getRedirect().getName());
            return;
        }
        lines.add(marker + path);
        for (CommandNode<CommandSourceStack> child : node.getChildren()) {
            String part = child instanceof ArgumentCommandNode<?, ?> argument
                    ? "<" + argument.getName() + ":" + argument.getType().getClass().getSimpleName().replace("ArgumentType", "") + ">"
                    : child.getName();
            walk(child, path + " " + part, lines, depth + 1);
        }
    }
}
