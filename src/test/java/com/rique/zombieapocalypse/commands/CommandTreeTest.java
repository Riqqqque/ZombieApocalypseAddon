package com.rique.zombieapocalypse.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;

import com.rique.zombieapocalypse.ConfigLimits;

class CommandTreeTest {

    private static final Set<String> ROOT_COMMANDS = Set.of(
            "za", "zombieapocalypse", "zhelp", "zcompat", "zburn", "zkill", "zcleanup",
            "zday", "zhorde", "zbloodmoon", "zstats", "zscaling", "zdayspawn",
            "zblockbreak", "zblockplace", "ztower", "zattr");

    private static final Map<String, String> ZA_REDIRECTS = Map.ofEntries(
            Map.entry("spawn", "zdayspawn"),
            Map.entry("events", "zhorde"),
            Map.entry("bloodmoon", "zbloodmoon"),
            Map.entry("day", "zday"),
            Map.entry("scaling", "zscaling"),
            Map.entry("stats", "zstats"),
            Map.entry("breaking", "zblockbreak"),
            Map.entry("placing", "zblockplace"),
            Map.entry("towering", "ztower"),
            Map.entry("attributes", "zattr"),
            Map.entry("compatibility", "zcompat"),
            Map.entry("burn", "zburn"),
            Map.entry("kill", "zkill"),
            Map.entry("cleanup", "zcleanup"));

    private CommandDispatcher<CommandSourceStack> dispatcher;

    @BeforeEach
    void registerCommands() {
        dispatcher = new CommandDispatcher<>();
        CommandRegistrar.registerAll(dispatcher);
    }

    @Test
    void registersEveryDocumentedRoot() {
        Set<String> actual = dispatcher.getRoot().getChildren().stream()
                .map(CommandNode::getName)
                .collect(java.util.stream.Collectors.toSet());
        assertEquals(ROOT_COMMANDS, actual);
    }

    @Test
    void unifiedCommandRedirectsToEveryLegacyFamily() {
        CommandNode<CommandSourceStack> za = required(dispatcher.getRoot(), "za");
        for (Map.Entry<String, String> entry : ZA_REDIRECTS.entrySet()) {
            CommandNode<CommandSourceStack> alias = required(za, entry.getKey());
            CommandNode<CommandSourceStack> target = required(dispatcher.getRoot(), entry.getValue());
            assertSame(target, alias.getRedirect(), "/za " + entry.getKey());
            assertEquals(target.getRequirement().test(null), alias.getRequirement().test(null),
                    "/za " + entry.getKey() + " must preserve the target permission requirement");
        }

        assertSame(za, required(dispatcher.getRoot(), "zombieapocalypse").getRedirect());
    }

    @Test
    void readOnlyCommandsStayPublicAndMutationsStayAdminOnly() {
        assertPublic("zdayspawn");
        assertPublic("zday");
        assertPublic("zhorde");
        assertPublic("zstats");
        assertPublic("zscaling");
        assertPublic("zblockbreak");
        assertPublic("zblockplace");
        assertPublic("ztower");
        assertPublic("zattr");
        assertPublic("zcompat");
        assertPublic("zburn");
        assertPublic("zbloodmoon");
        assertPublic("zdayspawn", "chance");
        assertPublic("zdayspawn", "daytime");
        assertPublic("zblockplace", "block");
        assertPublic("zcompat", "modded");

        assertAdmin("zkill");
        assertAdmin("zcleanup");
        assertAdmin("zbloodmoon", "start");
        assertAdmin("zbloodmoon", "on");
        assertAdmin("zbloodmoon", "off");
        assertAdmin("zdayspawn", "on");
        assertAdmin("zdayspawn", "off");
        assertAdmin("zdayspawn", "daytime", "state");
        assertAdmin("zdayspawn", "chance", "chance");
        assertAdmin("zday", "set");
        assertAdmin("zhorde", "start");
        assertAdmin("zhorde", "stop");
        assertAdmin("zhorde", "interval", "days");
        assertAdmin("zhorde", "duration", "minutes");
        assertAdmin("zbloodmoon", "multiplier", "multiplier");
        assertAdmin("zscaling", "startday", "day");
        assertAdmin("zscaling", "maxday", "day");
        assertAdmin("zstats", "clear");
        assertAdmin("zblockbreak", "dayone");
        assertAdmin("zblockplace", "block", "id");
        assertAdmin("ztower", "enabled", "state");
        assertAdmin("ztower", "stacksize", "zombies");
        assertAdmin("ztower", "maxperplayer", "towers");
        assertAdmin("ztower", "jumping", "state");
        assertAdmin("ztower", "jumpcooldown", "ticks");
        assertAdmin("zattr", "set");
        assertAdmin("zattr", "toggle");
        assertAdmin("zburn", "state");
        assertAdmin("za", "preset", "casual");

        for (String command : List.of(
                "zdayspawn", "zhorde", "zbloodmoon", "zstats", "zscaling",
                "zblockbreak", "zblockplace", "ztower", "zattr", "zcompat")) {
            assertAdmin(command, "on");
            assertAdmin(command, "off");
        }
    }

    @Test
    void towerAndDayArgumentsExposeTheReviewedRanges() {
        assertIntegerRange(1, 72_000, "ztower", "interval", "ticks");
        assertIntegerRange(2, ConfigLimits.MAX_TOWER_STACK_SIZE, "ztower", "stacksize", "zombies");
        assertIntegerRange(0, ConfigLimits.MAX_TOWERS_PER_PLAYER, "ztower", "maxperplayer", "towers");
        assertIntegerRange(1, 1_200, "ztower", "jumpcooldown", "ticks");
        assertIntegerRange(0, ConfigLimits.MAX_APOCALYPSE_DAY, "ztower", "startday", "day");
        assertIntegerRange(0, ConfigLimits.MAX_APOCALYPSE_DAY, "zblockbreak", "startday", "day");
        assertIntegerRange(0, ConfigLimits.MAX_APOCALYPSE_DAY, "zblockplace", "startday", "day");
        assertIntegerRange(0, ConfigLimits.MAX_APOCALYPSE_DAY, "zdayspawn", "daylightstart", "day");
        assertIntegerRange(1, ConfigLimits.MAX_APOCALYPSE_DAY, "zhorde", "interval", "days");
        assertIntegerRange(1, 10_080, "zhorde", "duration", "minutes");
        assertIntegerRange(0, ConfigLimits.MAX_APOCALYPSE_DAY, "zscaling", "startday", "day");
        assertIntegerRange(1, ConfigLimits.MAX_APOCALYPSE_DAY, "zscaling", "maxday", "day");
    }

    @Test
    void everyTypedArgumentProvidesTabCompletion() {
        int argumentCount = 0;
        for (CommandNode<CommandSourceStack> node : allNodes(dispatcher.getRoot())) {
            if (!(node instanceof ArgumentCommandNode<CommandSourceStack, ?> argument)) {
                continue;
            }
            argumentCount++;
            boolean hasBuiltInPlayerSuggestions = argument.getType() instanceof EntityArgument;
            assertTrue(hasBuiltInPlayerSuggestions || argument.getCustomSuggestions() != null,
                    () -> "Missing Tab completion for argument: " + argument.getName());
        }
        assertTrue(argumentCount > 0);
    }

    private void assertPublic(String... path) {
        assertTrue(find(path).getRequirement().test(null), String.join(" ", path));
    }

    private void assertAdmin(String... path) {
        assertFalse(find(path).getRequirement().test(null), String.join(" ", path));
    }

    private void assertIntegerRange(int minimum, int maximum, String... path) {
        CommandNode<CommandSourceStack> node = find(path);
        assertTrue(node instanceof ArgumentCommandNode<?, ?>, String.join(" ", path));
        Object type = ((ArgumentCommandNode<?, ?>) node).getType();
        assertTrue(type instanceof IntegerArgumentType, String.join(" ", path));
        IntegerArgumentType integer = (IntegerArgumentType) type;
        assertEquals(minimum, integer.getMinimum(), String.join(" ", path));
        assertEquals(maximum, integer.getMaximum(), String.join(" ", path));
    }

    private CommandNode<CommandSourceStack> find(String... path) {
        CommandNode<CommandSourceStack> node = dispatcher.getRoot();
        for (String part : path) {
            node = required(node, part);
        }
        return node;
    }

    private static CommandNode<CommandSourceStack> required(CommandNode<CommandSourceStack> parent, String child) {
        CommandNode<CommandSourceStack> result = parent.getChild(child);
        assertNotNull(result, () -> "Missing command node " + child + " below " + parent.getName());
        return result;
    }

    private static List<CommandNode<CommandSourceStack>> allNodes(CommandNode<CommandSourceStack> root) {
        List<CommandNode<CommandSourceStack>> nodes = new java.util.ArrayList<>();
        collect(root, nodes);
        return nodes;
    }

    private static void collect(
            CommandNode<CommandSourceStack> node,
            List<CommandNode<CommandSourceStack>> nodes) {
        for (CommandNode<CommandSourceStack> child : node.getChildren()) {
            nodes.add(child);
            collect(child, nodes);
        }
    }

}
