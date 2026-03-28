package com.rique.zombieapocalypse.commands;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
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

    private record NumericSetting(boolean integer, DoubleSupplier getter, DoubleConsumer setter) {
        double get() {
            return getter.getAsDouble();
        }

        void set(double value) {
            setter.accept(value);
        }
    }

    private static final Map<String, NumericSetting> NUMERIC_SETTINGS = createNumericSettings();
    private static final Map<String, ForgeConfigSpec.BooleanValue> BOOLEAN_SETTINGS = createBooleanSettings();

    private AttributeCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("zattr")
                        .requires(source -> source.hasPermission(2))
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
                                        .suggests((context, builder) -> suggestKeys(builder, NUMERIC_SETTINGS.keySet()))
                                        .executes(context -> {
                                            String key = StringArgumentType.getString(context, "key");
                                            NumericSetting setting = NUMERIC_SETTINGS.get(key);
                                            if (setting == null) {
                                                CommandUtil.feedback(context.getSource(), "Unknown numeric key: " + key, false);
                                                return 0;
                                            }

                                            double current = setting.get();
                                            String value = setting.integer()
                                                    ? Integer.toString((int) Math.round(current))
                                                    : CommandUtil.number(current);
                                            CommandUtil.feedback(context.getSource(), key + " = " + value, false);
                                            return 1;
                                        })))
                        .then(Commands.literal("set")
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestKeys(builder, NUMERIC_SETTINGS.keySet()))
                                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(-10000.0, 10000.0))
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

                                                    try {
                                                        setting.set(value);
                                                        double applied = setting.get();
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
                                                }))))
                        .then(Commands.literal("toggle")
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .suggests((context, builder) -> suggestKeys(builder, BOOLEAN_SETTINGS.keySet()))
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    String key = StringArgumentType.getString(context, "key");
                                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                                    ForgeConfigSpec.BooleanValue setting = BOOLEAN_SETTINGS.get(key);
                                                    if (setting == null) {
                                                        CommandUtil.feedback(context.getSource(),
                                                                "Unknown toggle key: " + key,
                                                                false);
                                                        return 0;
                                                    }

                                                    setting.set(enabled);
                                                    CommandUtil.feedback(context.getSource(),
                                                            "Set " + key + " = " + CommandUtil.onOff(enabled),
                                                            true);
                                                    return 1;
                                                })))));
    }

    private static CompletableFuture<Suggestions> suggestKeys(SuggestionsBuilder builder, Collection<String> keys) {
        return SharedSuggestionProvider.suggest(keys, builder);
    }

    private static String buildStatusMessage(CommandSourceStack source) {
        return "Attribute tuning command status:\n"
                + DifficultyManager.getScalingStatus(source.getLevel()) + "\n"
                + "Use /zattr keys for key groups, or /zattr keys all for all keys.";
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

        map.put("legacy.speedMultiplier", doubleSetting(Config.COMMON.maxSpeedBoost));
        map.put("legacy.healthBonus", intSetting(Config.COMMON.maxHealthBoost));

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
        map.put(prefix + ".multiplier", doubleSetting(multiplier));
        map.put(prefix + ".bonus", doubleSetting(bonus));
    }

    private static void addMultiplier(Map<String, NumericSetting> map, String prefix, ForgeConfigSpec.DoubleValue multiplier) {
        map.put(prefix + ".multiplier", doubleSetting(multiplier));
    }

    private static NumericSetting doubleSetting(ForgeConfigSpec.DoubleValue value) {
        return new NumericSetting(false, value::get, value::set);
    }

    private static NumericSetting intSetting(ForgeConfigSpec.IntValue value) {
        return new NumericSetting(true, () -> value.get(), v -> value.set((int) Math.round(v)));
    }
}
