package com.rique.zombieapocalypse.commands;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraftforge.common.ForgeConfigSpec;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.DifficultyManager;

public final class AttributeCommands {

    private record NumericSetting(
            boolean integer,
            double minimum,
            double maximum,
            DoubleSupplier getter,
            DoubleConsumer setter) {
        double get() {
            return getter.getAsDouble();
        }

        void set(double value) {
            setter.accept(value);
        }

        double appliedValue(double value) {
            return integer ? Math.rint(value) : value;
        }

        boolean accepts(double value) {
            return Double.isFinite(value) && value >= minimum && value <= maximum;
        }
    }

    private static final Map<String, NumericSetting> NUMERIC_SETTINGS = createNumericSettings();
    private static final Map<String, ForgeConfigSpec.BooleanValue> BOOLEAN_SETTINGS = createBooleanSettings();
    private static final Set<String> READABLE_KEYS = createReadableKeys();

    private AttributeCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("zattr")
                        .executes(context -> {
                            CommandUtil.feedback(context.getSource(), buildStatusMessage(context.getSource()), false);
                            return 1;
                        })
                        .then(CommandUtil.admin(Commands.literal("on")
                                .executes(context -> setEnabled(context.getSource(), true))))
                        .then(CommandUtil.admin(Commands.literal("off")
                                .executes(context -> setEnabled(context.getSource(), false))))
                        .then(Commands.literal("status")
                                .executes(context -> {
                                    CommandUtil.feedback(context.getSource(), buildStatusMessage(context.getSource()), false);
                                    return 1;
                                }))
                        .then(Commands.literal("keys")
                                .executes(context -> {
                                    CommandUtil.feedback(context.getSource(), buildKeySummary(), false);
                                    return 1;
                                })
                                .then(Commands.literal("all")
                                        .executes(context -> {
                                            CommandUtil.feedback(context.getSource(), buildAllKeysMessage(), false);
                                            return 1;
                                        })))
                        .then(Commands.literal("get")
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestKeys(builder, READABLE_KEYS))
                                        .executes(context -> showValue(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "key")))))
                        .then(CommandUtil.admin(Commands.literal("set")
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestKeys(builder, NUMERIC_SETTINGS.keySet()))
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(-10000.0, 10000.0))
                                                .suggests(AttributeCommands::suggestCurrentNumericValue)
                                                .executes(context -> {
                                                    String key = StringArgumentType.getString(context, "key");
                                                    double value = DoubleArgumentType.getDouble(context, "value");
                                                    NumericSetting setting = NUMERIC_SETTINGS.get(key);
                                                    if (setting == null) {
                                                        CommandUtil.feedback(context.getSource(),
                                                                "Unknown numeric key: " + key,
                                                                false);
                                                        return 0;
                                                    }

                                                    double applied = setting.appliedValue(value);
                                                    if (!setting.accepts(applied)) {
                                                        CommandUtil.failure(context.getSource(),
                                                                "Allowed range for " + key + ": "
                                                                        + formatNumeric(setting, setting.minimum()) + " to "
                                                                        + formatNumeric(setting, setting.maximum()));
                                                        return 0;
                                                    }

                                                    try {
                                                        setting.set(applied);
                                                        String appliedText = setting.integer()
                                                                ? Integer.toString((int) Math.round(applied))
                                                                : CommandUtil.number(applied);
                                                        String roundedNote = "";
                                                        if (setting.integer() && Math.abs(value - Math.rint(value)) > 1.0E-9) {
                                                            roundedNote = " (rounded to nearest integer)";
                                                        }
                                                        CommandUtil.feedback(context.getSource(),
                                                                "Set " + key + " = " + appliedText + roundedNote,
                                                                true);
                                                        return 1;
                                                    } catch (RuntimeException ex) {
                                                        CommandUtil.feedback(context.getSource(),
                                                                "Failed to set " + key + ": " + ex.getMessage(),
                                                                false);
                                                        return 0;
                                                    }
                                                })))))
                        .then(CommandUtil.admin(Commands.literal("toggle")
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestKeys(builder, BOOLEAN_SETTINGS.keySet()))
                                        .then(CommandUtil.toggleArgument("state")
                                                .executes(context -> {
                                                    String key = StringArgumentType.getString(context, "key");
                                                    boolean enabled = CommandUtil.getToggle(context, "state");
                                                    ForgeConfigSpec.BooleanValue setting = BOOLEAN_SETTINGS.get(key);
                                                    if (setting == null) {
                                                        CommandUtil.feedback(context.getSource(),
                                                                "Unknown toggle key: " + key,
                                                                false);
                                                        return 0;
                                                    }
                                                    setBooleanSetting(key, setting, enabled);
                                                    CommandUtil.feedback(context.getSource(),
                                                            "Set " + key + " = " + CommandUtil.onOff(enabled),
                                                            true);
                                                    return 1;
                                                }))))));
    }

    private static int setEnabled(CommandSourceStack source, boolean enabled) {
        if (enabled) {
            FeaturePresets.enableAttributes();
            CommandUtil.feedback(source,
                    "Advanced attributes: ON\nBalanced health, attack, armor, follow-range, and knockback scaling preset loaded. Speed scaling stays conservative.",
                    true);
        } else {
            Config.set(Config.COMMON.enableAttributeModifiers, false);
            CommandUtil.feedback(source, "Advanced attributes: OFF", true);
        }
        return 1;
    }

    private static void setBooleanSetting(
            String key,
            ForgeConfigSpec.BooleanValue setting,
            boolean enabled) {
        if (enabled && BOOLEAN_SETTINGS.containsKey(key)) {
            FeaturePresets.enableAttributes();
        }
        Config.set(setting, enabled);
    }

    private static CompletableFuture<Suggestions> suggestKeys(SuggestionsBuilder builder, Collection<String> keys) {
        return SharedSuggestionProvider.suggest(keys, builder);
    }

    private static Set<String> createReadableKeys() {
        LinkedHashSet<String> keys = new LinkedHashSet<>(NUMERIC_SETTINGS.keySet());
        keys.addAll(BOOLEAN_SETTINGS.keySet());
        return Set.copyOf(keys);
    }

    private static int showValue(CommandSourceStack source, String key) {
        NumericSetting numeric = NUMERIC_SETTINGS.get(key);
        if (numeric != null) {
            double current = numeric.get();
            CommandUtil.feedback(source,
                    key + " = " + formatNumeric(numeric, current)
                            + " (allowed " + formatNumeric(numeric, numeric.minimum())
                            + " to " + formatNumeric(numeric, numeric.maximum()) + ')',
                    false);
            return 1;
        }

        ForgeConfigSpec.BooleanValue toggle = BOOLEAN_SETTINGS.get(key);
        if (toggle != null) {
            CommandUtil.feedback(source, key + " = " + CommandUtil.onOff(toggle.get()), false);
            return 1;
        }

        CommandUtil.feedback(source, "Unknown attribute key: " + key, false);
        return 0;
    }

    private static CompletableFuture<Suggestions> suggestCurrentNumericValue(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {
        NumericSetting setting = NUMERIC_SETTINGS.get(StringArgumentType.getString(context, "key"));
        if (setting == null) {
            return Suggestions.empty();
        }
        double current = setting.get();
        return CommandSuggestions.suggest(
                builder,
                formatNumeric(setting, current),
                formatNumeric(setting, setting.minimum()),
                formatNumeric(setting, setting.maximum()),
                "0",
                "1");
    }

    private static String buildStatusMessage(CommandSourceStack source) {
        return "Attribute tuning command status:\n"
                + DifficultyManager.getScalingStatus(source.getLevel()) + "\n"
                + "Use /za attributes keys for key groups, or /za attributes keys all for all keys.";
    }

    private static String buildKeySummary() {
        return "zattr numeric key groups:\n"
                + "base.<health|attack|speed|armor|follow|knockback>.<multiplier|bonus>\n"
                + "scale.<health|attack|speed|armor|follow|knockback>.<multiplier|bonus>\n"
                + "variant.<zombie|husk|drowned|villager>.<health|attack|speed|armor|follow|knockback>.<multiplier|bonus>\n"
                + "context.<desert|water|mushroom|nether|end>.<health|attack|speed|armor|follow|knockback>.multiplier\n"
                + "legacy.<speedMultiplier|healthBonus>\n"
                + "Numeric keys: " + NUMERIC_SETTINGS.size() + " | Toggle keys: " + BOOLEAN_SETTINGS.size();
    }

    private static String buildAllKeysMessage() {
        StringBuilder sb = new StringBuilder("All numeric zattr keys:\n");
        for (String key : NUMERIC_SETTINGS.keySet()) {
            sb.append(key).append('\n');
        }
        sb.append("Toggle keys:\n");
        for (String key : BOOLEAN_SETTINGS.keySet()) {
            sb.append(key).append('\n');
        }
        return sb.toString();
    }

    private static Map<String, NumericSetting> createNumericSettings() {
        Map<String, NumericSetting> map = new LinkedHashMap<>();

        addPair(map, "base.health", Config.COMMON.baseHealthMultiplier, Config.COMMON.baseHealthBonus);
        addPair(map, "base.attack", Config.COMMON.baseAttackMultiplier, Config.COMMON.baseAttackBonus);
        addPair(map, "base.speed", Config.COMMON.baseSpeedMultiplier, Config.COMMON.baseSpeedBonus);
        addPair(map, "base.armor", Config.COMMON.baseArmorMultiplier, Config.COMMON.baseArmorBonus);
        addPair(map, "base.follow", Config.COMMON.baseFollowRangeMultiplier, Config.COMMON.baseFollowRangeBonus);
        addPair(map, "base.knockback", Config.COMMON.baseKnockbackResistanceMultiplier,
                Config.COMMON.baseKnockbackResistanceBonus);

        addPair(map, "scale.health", Config.COMMON.maxHealthScaleMultiplier, Config.COMMON.maxHealthScaleBonus);
        addPair(map, "scale.attack", Config.COMMON.maxAttackScaleMultiplier, Config.COMMON.maxAttackScaleBonus);
        addPair(map, "scale.speed", Config.COMMON.maxSpeedScaleMultiplier, Config.COMMON.maxSpeedScaleBonus);
        addPair(map, "scale.armor", Config.COMMON.maxArmorScaleMultiplier, Config.COMMON.maxArmorScaleBonus);
        addPair(map, "scale.follow", Config.COMMON.maxFollowRangeScaleMultiplier, Config.COMMON.maxFollowRangeScaleBonus);
        addPair(map, "scale.knockback", Config.COMMON.maxKnockbackResistanceScaleMultiplier,
                Config.COMMON.maxKnockbackResistanceScaleBonus);

        addPair(map, "variant.zombie.health", Config.COMMON.zombieHealthMultiplier, Config.COMMON.zombieHealthBonus);
        addPair(map, "variant.zombie.attack", Config.COMMON.zombieAttackMultiplier, Config.COMMON.zombieAttackBonus);
        addPair(map, "variant.zombie.speed", Config.COMMON.zombieSpeedMultiplier, Config.COMMON.zombieSpeedBonus);
        addPair(map, "variant.zombie.armor", Config.COMMON.zombieArmorMultiplier, Config.COMMON.zombieArmorBonus);
        addPair(map, "variant.zombie.follow", Config.COMMON.zombieFollowRangeMultiplier,
                Config.COMMON.zombieFollowRangeBonus);
        addPair(map, "variant.zombie.knockback", Config.COMMON.zombieKnockbackResistanceMultiplier,
                Config.COMMON.zombieKnockbackResistanceBonus);

        addPair(map, "variant.husk.health", Config.COMMON.huskHealthMultiplier, Config.COMMON.huskHealthBonus);
        addPair(map, "variant.husk.attack", Config.COMMON.huskAttackMultiplier, Config.COMMON.huskAttackBonus);
        addPair(map, "variant.husk.speed", Config.COMMON.huskSpeedMultiplier, Config.COMMON.huskSpeedBonus);
        addPair(map, "variant.husk.armor", Config.COMMON.huskArmorMultiplier, Config.COMMON.huskArmorBonus);
        addPair(map, "variant.husk.follow", Config.COMMON.huskFollowRangeMultiplier, Config.COMMON.huskFollowRangeBonus);
        addPair(map, "variant.husk.knockback", Config.COMMON.huskKnockbackResistanceMultiplier,
                Config.COMMON.huskKnockbackResistanceBonus);

        addPair(map, "variant.drowned.health", Config.COMMON.drownedHealthMultiplier, Config.COMMON.drownedHealthBonus);
        addPair(map, "variant.drowned.attack", Config.COMMON.drownedAttackMultiplier, Config.COMMON.drownedAttackBonus);
        addPair(map, "variant.drowned.speed", Config.COMMON.drownedSpeedMultiplier, Config.COMMON.drownedSpeedBonus);
        addPair(map, "variant.drowned.armor", Config.COMMON.drownedArmorMultiplier, Config.COMMON.drownedArmorBonus);
        addPair(map, "variant.drowned.follow", Config.COMMON.drownedFollowRangeMultiplier,
                Config.COMMON.drownedFollowRangeBonus);
        addPair(map, "variant.drowned.knockback", Config.COMMON.drownedKnockbackResistanceMultiplier,
                Config.COMMON.drownedKnockbackResistanceBonus);

        addPair(map, "variant.villager.health", Config.COMMON.zombieVillagerHealthMultiplier,
                Config.COMMON.zombieVillagerHealthBonus);
        addPair(map, "variant.villager.attack", Config.COMMON.zombieVillagerAttackMultiplier,
                Config.COMMON.zombieVillagerAttackBonus);
        addPair(map, "variant.villager.speed", Config.COMMON.zombieVillagerSpeedMultiplier,
                Config.COMMON.zombieVillagerSpeedBonus);
        addPair(map, "variant.villager.armor", Config.COMMON.zombieVillagerArmorMultiplier,
                Config.COMMON.zombieVillagerArmorBonus);
        addPair(map, "variant.villager.follow", Config.COMMON.zombieVillagerFollowRangeMultiplier,
                Config.COMMON.zombieVillagerFollowRangeBonus);
        addPair(map, "variant.villager.knockback", Config.COMMON.zombieVillagerKnockbackResistanceMultiplier,
                Config.COMMON.zombieVillagerKnockbackResistanceBonus);

        addMultiplier(map, "context.desert.health", Config.COMMON.desertHealthMultiplier);
        addMultiplier(map, "context.desert.attack", Config.COMMON.desertAttackMultiplier);
        addMultiplier(map, "context.desert.speed", Config.COMMON.desertSpeedMultiplier);
        addMultiplier(map, "context.desert.armor", Config.COMMON.desertArmorMultiplier);
        addMultiplier(map, "context.desert.follow", Config.COMMON.desertFollowRangeMultiplier);
        addMultiplier(map, "context.desert.knockback", Config.COMMON.desertKnockbackResistanceMultiplier);

        addMultiplier(map, "context.water.health", Config.COMMON.waterHealthMultiplier);
        addMultiplier(map, "context.water.attack", Config.COMMON.waterAttackMultiplier);
        addMultiplier(map, "context.water.speed", Config.COMMON.waterSpeedMultiplier);
        addMultiplier(map, "context.water.armor", Config.COMMON.waterArmorMultiplier);
        addMultiplier(map, "context.water.follow", Config.COMMON.waterFollowRangeMultiplier);
        addMultiplier(map, "context.water.knockback", Config.COMMON.waterKnockbackResistanceMultiplier);

        addMultiplier(map, "context.mushroom.health", Config.COMMON.mushroomHealthMultiplier);
        addMultiplier(map, "context.mushroom.attack", Config.COMMON.mushroomAttackMultiplier);
        addMultiplier(map, "context.mushroom.speed", Config.COMMON.mushroomSpeedMultiplier);
        addMultiplier(map, "context.mushroom.armor", Config.COMMON.mushroomArmorMultiplier);
        addMultiplier(map, "context.mushroom.follow", Config.COMMON.mushroomFollowRangeMultiplier);
        addMultiplier(map, "context.mushroom.knockback", Config.COMMON.mushroomKnockbackResistanceMultiplier);

        addMultiplier(map, "context.nether.health", Config.COMMON.netherHealthMultiplier);
        addMultiplier(map, "context.nether.attack", Config.COMMON.netherAttackMultiplier);
        addMultiplier(map, "context.nether.speed", Config.COMMON.netherSpeedMultiplier);
        addMultiplier(map, "context.nether.armor", Config.COMMON.netherArmorMultiplier);
        addMultiplier(map, "context.nether.follow", Config.COMMON.netherFollowRangeMultiplier);
        addMultiplier(map, "context.nether.knockback", Config.COMMON.netherKnockbackResistanceMultiplier);

        addMultiplier(map, "context.end.health", Config.COMMON.endHealthMultiplier);
        addMultiplier(map, "context.end.attack", Config.COMMON.endAttackMultiplier);
        addMultiplier(map, "context.end.speed", Config.COMMON.endSpeedMultiplier);
        addMultiplier(map, "context.end.armor", Config.COMMON.endArmorMultiplier);
        addMultiplier(map, "context.end.follow", Config.COMMON.endFollowRangeMultiplier);
        addMultiplier(map, "context.end.knockback", Config.COMMON.endKnockbackResistanceMultiplier);

        map.put("legacy.speedMultiplier", doubleSetting(Config.COMMON.maxSpeedBoost, 0.0, 1.0));
        map.put("legacy.healthBonus", intSetting(Config.COMMON.maxHealthBoost, 0, 40));

        return map;
    }

    private static Map<String, ForgeConfigSpec.BooleanValue> createBooleanSettings() {
        Map<String, ForgeConfigSpec.BooleanValue> map = new LinkedHashMap<>();
        map.put("attributes.enabled", Config.COMMON.enableAttributeModifiers);
        map.put("attributes.scaling", Config.COMMON.scaleAttributesWithDifficulty);
        map.put("attributes.variantProfiles", Config.COMMON.enableVariantAttributeProfiles);
        map.put("attributes.contextProfiles", Config.COMMON.enableBiomeDimensionAttributeMultipliers);
        return map;
    }

    private static void addPair(
            Map<String, NumericSetting> map,
            String prefix,
            ForgeConfigSpec.DoubleValue multiplier,
            ForgeConfigSpec.DoubleValue bonus) {
        map.put(prefix + ".multiplier", doubleSetting(multiplier, 0.0, 10.0));
        double bonusLimit = bonusLimit(prefix);
        map.put(prefix + ".bonus", doubleSetting(bonus, -bonusLimit, bonusLimit));
    }

    private static void addMultiplier(Map<String, NumericSetting> map, String prefix, ForgeConfigSpec.DoubleValue multiplier) {
        map.put(prefix + ".multiplier", doubleSetting(multiplier, 0.0, 10.0));
    }

    private static NumericSetting doubleSetting(ForgeConfigSpec.DoubleValue value, double minimum, double maximum) {
        return new NumericSetting(false, minimum, maximum, value::get, newValue -> Config.set(value, newValue));
    }

    private static NumericSetting intSetting(ForgeConfigSpec.IntValue value, int minimum, int maximum) {
        return new NumericSetting(true, minimum, maximum, value::get, v -> Config.set(value, (int) Math.round(v)));
    }

    private static double bonusLimit(String keyPrefix) {
        if (keyPrefix.endsWith(".health")) {
            return 200.0;
        }
        if (keyPrefix.endsWith(".attack")) {
            return 50.0;
        }
        if (keyPrefix.endsWith(".armor")) {
            return 30.0;
        }
        if (keyPrefix.endsWith(".follow")) {
            return 100.0;
        }
        return 1.0;
    }

    private static String formatNumeric(NumericSetting setting, double value) {
        return setting.integer()
                ? Integer.toString((int) Math.round(value))
                : CommandUtil.number(value);
    }
}
