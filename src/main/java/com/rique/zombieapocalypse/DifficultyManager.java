package com.rique.zombieapocalypse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * Applies day-based scaling and configurable attribute profiles to spawned
 * zombie-class mobs.
 */
public final class DifficultyManager {

    private enum AttributeKey {
        HEALTH,
        ATTACK,
        SPEED,
        ARMOR,
        FOLLOW_RANGE,
        KNOCKBACK_RESISTANCE
    }

    private enum SpawnVariant {
        ZOMBIE,
        HUSK,
        DROWNED,
        ZOMBIE_VILLAGER
    }

    private record SpawnContext(
            boolean desertBiome,
            boolean waterBiome,
            boolean mushroomBiome,
            boolean netherDimension,
            boolean endDimension) {
    }

    private enum ArmorTier {
        LEATHER, CHAINMAIL, IRON, DIAMOND
    }

    private static final SpawnContext NO_CONTEXT = new SpawnContext(false, false, false, false, false);

    private DifficultyManager() {
    }

    public static long getCurrentDay(ServerLevel level) {
        return level.getDayTime() / 24000L;
    }

    public static double getScalingFactor(ServerLevel level) {
        if (!Config.COMMON.enableDifficultyScaling.get()) {
            return 0.0;
        }

        return SpawnMath.computeLinearScale(
                getCurrentDay(level),
                Config.COMMON.scalingStartDay.get(),
                Config.COMMON.maxScalingDay.get());
    }

    public static void applyScaling(Zombie zombie, ServerLevel level) {
        applyScaling(zombie, level, zombie.blockPosition());
    }

    public static void applyScaling(Zombie zombie, ServerLevel level, BlockPos spawnPos) {
        double factor = getScalingFactor(level);
        applyConfiguredAttributes(zombie, level, spawnPos, factor);

        if (!Config.COMMON.enableDifficultyScaling.get() || factor <= 0.0) {
            return;
        }

        RandomSource random = level.getRandom();
        if (random.nextDouble() < Config.COMMON.maxArmorChance.get() * factor) {
            applyRandomArmor(zombie, random, factor);
        }

        if (random.nextDouble() < Config.COMMON.maxWeaponChance.get() * factor) {
            applyRandomWeapon(zombie, random, factor);
        }
    }

    private static void applyConfiguredAttributes(Zombie zombie, ServerLevel level, BlockPos spawnPos, double difficultyFactor) {
        boolean customEnabled = Config.COMMON.enableAttributeModifiers.get();
        double legacyFactor = Config.COMMON.enableDifficultyScaling.get() ? difficultyFactor : 0.0;
        if (!customEnabled && legacyFactor <= 0.0) {
            return;
        }

        if (!customEnabled) {
            applyAttribute(
                    zombie,
                    Attributes.MAX_HEALTH,
                    AttributeKey.HEALTH,
                    SpawnVariant.ZOMBIE,
                    NO_CONTEXT,
                    false,
                    false,
                    false,
                    0.0,
                    legacyFactor,
                    0.0,
                    Config.COMMON.maxHealthBoost.get(),
                    1.0,
                    Double.MAX_VALUE);

            applyAttribute(
                    zombie,
                    Attributes.MOVEMENT_SPEED,
                    AttributeKey.SPEED,
                    SpawnVariant.ZOMBIE,
                    NO_CONTEXT,
                    false,
                    false,
                    false,
                    0.0,
                    legacyFactor,
                    Config.COMMON.maxSpeedBoost.get(),
                    0.0,
                    0.01,
                    Double.MAX_VALUE);

            zombie.setHealth(zombie.getMaxHealth());
            return;
        }

        boolean variantProfilesEnabled = Config.COMMON.enableVariantAttributeProfiles.get();
        boolean contextMultipliersEnabled = Config.COMMON.enableBiomeDimensionAttributeMultipliers.get();

        double attributeScaleFactor = Config.COMMON.enableDifficultyScaling.get() && Config.COMMON.scaleAttributesWithDifficulty.get()
                ? difficultyFactor
                : 0.0;

        SpawnVariant variant = resolveVariant(zombie);
        SpawnContext context = resolveSpawnContext(level, spawnPos);

        applyAttribute(
                zombie,
                Attributes.MAX_HEALTH,
                AttributeKey.HEALTH,
                variant,
                context,
                customEnabled,
                variantProfilesEnabled,
                contextMultipliersEnabled,
                attributeScaleFactor,
                legacyFactor,
                0.0,
                Config.COMMON.maxHealthBoost.get(),
                1.0,
                Double.MAX_VALUE);

        applyAttribute(
                zombie,
                Attributes.ATTACK_DAMAGE,
                AttributeKey.ATTACK,
                variant,
                context,
                customEnabled,
                variantProfilesEnabled,
                contextMultipliersEnabled,
                attributeScaleFactor,
                legacyFactor,
                0.0,
                0.0,
                0.0,
                Double.MAX_VALUE);

        applyAttribute(
                zombie,
                Attributes.MOVEMENT_SPEED,
                AttributeKey.SPEED,
                variant,
                context,
                customEnabled,
                variantProfilesEnabled,
                contextMultipliersEnabled,
                attributeScaleFactor,
                legacyFactor,
                Config.COMMON.maxSpeedBoost.get(),
                0.0,
                0.01,
                Double.MAX_VALUE);

        applyAttribute(
                zombie,
                Attributes.ARMOR,
                AttributeKey.ARMOR,
                variant,
                context,
                customEnabled,
                variantProfilesEnabled,
                contextMultipliersEnabled,
                attributeScaleFactor,
                legacyFactor,
                0.0,
                0.0,
                0.0,
                Double.MAX_VALUE);

        applyAttribute(
                zombie,
                Attributes.FOLLOW_RANGE,
                AttributeKey.FOLLOW_RANGE,
                variant,
                context,
                customEnabled,
                variantProfilesEnabled,
                contextMultipliersEnabled,
                attributeScaleFactor,
                legacyFactor,
                0.0,
                0.0,
                1.0,
                Double.MAX_VALUE);

        applyAttribute(
                zombie,
                Attributes.KNOCKBACK_RESISTANCE,
                AttributeKey.KNOCKBACK_RESISTANCE,
                variant,
                context,
                customEnabled,
                variantProfilesEnabled,
                contextMultipliersEnabled,
                attributeScaleFactor,
                legacyFactor,
                0.0,
                0.0,
                0.0,
                1.0);

        zombie.setHealth(zombie.getMaxHealth());
    }

    private static void applyAttribute(
            Zombie zombie,
            Holder<Attribute> attribute,
            AttributeKey key,
            SpawnVariant variant,
            SpawnContext context,
            boolean customEnabled,
            boolean variantProfilesEnabled,
            boolean contextMultipliersEnabled,
            double attributeScaleFactor,
            double legacyFactor,
            double legacyScaleMultiplier,
            double legacyScaleBonus,
            double minValue,
            double maxValue) {
        AttributeInstance instance = zombie.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        double value = instance.getBaseValue();
        double scaleFactor = safeFactor(attributeScaleFactor);
        double legacyScaleFactor = safeFactor(legacyFactor);

        if (customEnabled) {
            value = applyModifier(value, getBaseMultiplier(key), getBaseBonus(key));

            if (variantProfilesEnabled) {
                value = applyModifier(value, getVariantMultiplier(variant, key), getVariantBonus(variant, key));
            }

            if (contextMultipliersEnabled) {
                value *= safeMultiplier(getContextMultiplier(context, key));
            }

            value *= 1.0 + (safeValue(getScaleMultiplier(key)) * scaleFactor);
            value += safeValue(getScaleBonus(key)) * scaleFactor;
        }

        value *= 1.0 + (safeValue(legacyScaleMultiplier) * legacyScaleFactor);
        value += safeValue(legacyScaleBonus) * legacyScaleFactor;

        if (!Double.isFinite(value)) {
            return;
        }

        value = Math.max(minValue, Math.min(maxValue, value));
        instance.setBaseValue(value);
    }

    private static double applyModifier(double value, double multiplier, double bonus) {
        return (value * safeMultiplier(multiplier)) + safeValue(bonus);
    }

    private static SpawnVariant resolveVariant(Zombie zombie) {
        EntityType<?> type = zombie.getType();
        if (type == EntityType.HUSK) {
            return SpawnVariant.HUSK;
        }
        if (type == EntityType.DROWNED) {
            return SpawnVariant.DROWNED;
        }
        if (type == EntityType.ZOMBIE_VILLAGER) {
            return SpawnVariant.ZOMBIE_VILLAGER;
        }
        return SpawnVariant.ZOMBIE;
    }

    private static SpawnContext resolveSpawnContext(ServerLevel level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);

        boolean desertBiome = ConfigValidator.isDesertStyleBiome(biome);
        boolean waterBiome = ConfigValidator.isWaterStyleBiome(biome);
        boolean mushroomBiome = biome.is(Biomes.MUSHROOM_FIELDS);
        boolean netherDimension = level.dimension() == Level.NETHER;
        boolean endDimension = level.dimension() == Level.END;

        return new SpawnContext(desertBiome, waterBiome, mushroomBiome, netherDimension, endDimension);
    }

    private static double getBaseMultiplier(AttributeKey key) {
        return switch (key) {
            case HEALTH -> Config.COMMON.baseHealthMultiplier.get();
            case ATTACK -> Config.COMMON.baseAttackMultiplier.get();
            case SPEED -> Config.COMMON.baseSpeedMultiplier.get();
            case ARMOR -> Config.COMMON.baseArmorMultiplier.get();
            case FOLLOW_RANGE -> Config.COMMON.baseFollowRangeMultiplier.get();
            case KNOCKBACK_RESISTANCE -> Config.COMMON.baseKnockbackResistanceMultiplier.get();
        };
    }

    private static double getBaseBonus(AttributeKey key) {
        return switch (key) {
            case HEALTH -> Config.COMMON.baseHealthBonus.get();
            case ATTACK -> Config.COMMON.baseAttackBonus.get();
            case SPEED -> Config.COMMON.baseSpeedBonus.get();
            case ARMOR -> Config.COMMON.baseArmorBonus.get();
            case FOLLOW_RANGE -> Config.COMMON.baseFollowRangeBonus.get();
            case KNOCKBACK_RESISTANCE -> Config.COMMON.baseKnockbackResistanceBonus.get();
        };
    }

    private static double getScaleMultiplier(AttributeKey key) {
        return switch (key) {
            case HEALTH -> Config.COMMON.maxHealthScaleMultiplier.get();
            case ATTACK -> Config.COMMON.maxAttackScaleMultiplier.get();
            case SPEED -> Config.COMMON.maxSpeedScaleMultiplier.get();
            case ARMOR -> Config.COMMON.maxArmorScaleMultiplier.get();
            case FOLLOW_RANGE -> Config.COMMON.maxFollowRangeScaleMultiplier.get();
            case KNOCKBACK_RESISTANCE -> Config.COMMON.maxKnockbackResistanceScaleMultiplier.get();
        };
    }

    private static double getScaleBonus(AttributeKey key) {
        return switch (key) {
            case HEALTH -> Config.COMMON.maxHealthScaleBonus.get();
            case ATTACK -> Config.COMMON.maxAttackScaleBonus.get();
            case SPEED -> Config.COMMON.maxSpeedScaleBonus.get();
            case ARMOR -> Config.COMMON.maxArmorScaleBonus.get();
            case FOLLOW_RANGE -> Config.COMMON.maxFollowRangeScaleBonus.get();
            case KNOCKBACK_RESISTANCE -> Config.COMMON.maxKnockbackResistanceScaleBonus.get();
        };
    }

    private static double getVariantMultiplier(SpawnVariant variant, AttributeKey key) {
        return switch (variant) {
            case ZOMBIE -> switch (key) {
                case HEALTH -> Config.COMMON.zombieHealthMultiplier.get();
                case ATTACK -> Config.COMMON.zombieAttackMultiplier.get();
                case SPEED -> Config.COMMON.zombieSpeedMultiplier.get();
                case ARMOR -> Config.COMMON.zombieArmorMultiplier.get();
                case FOLLOW_RANGE -> Config.COMMON.zombieFollowRangeMultiplier.get();
                case KNOCKBACK_RESISTANCE -> Config.COMMON.zombieKnockbackResistanceMultiplier.get();
            };
            case HUSK -> switch (key) {
                case HEALTH -> Config.COMMON.huskHealthMultiplier.get();
                case ATTACK -> Config.COMMON.huskAttackMultiplier.get();
                case SPEED -> Config.COMMON.huskSpeedMultiplier.get();
                case ARMOR -> Config.COMMON.huskArmorMultiplier.get();
                case FOLLOW_RANGE -> Config.COMMON.huskFollowRangeMultiplier.get();
                case KNOCKBACK_RESISTANCE -> Config.COMMON.huskKnockbackResistanceMultiplier.get();
            };
            case DROWNED -> switch (key) {
                case HEALTH -> Config.COMMON.drownedHealthMultiplier.get();
                case ATTACK -> Config.COMMON.drownedAttackMultiplier.get();
                case SPEED -> Config.COMMON.drownedSpeedMultiplier.get();
                case ARMOR -> Config.COMMON.drownedArmorMultiplier.get();
                case FOLLOW_RANGE -> Config.COMMON.drownedFollowRangeMultiplier.get();
                case KNOCKBACK_RESISTANCE -> Config.COMMON.drownedKnockbackResistanceMultiplier.get();
            };
            case ZOMBIE_VILLAGER -> switch (key) {
                case HEALTH -> Config.COMMON.zombieVillagerHealthMultiplier.get();
                case ATTACK -> Config.COMMON.zombieVillagerAttackMultiplier.get();
                case SPEED -> Config.COMMON.zombieVillagerSpeedMultiplier.get();
                case ARMOR -> Config.COMMON.zombieVillagerArmorMultiplier.get();
                case FOLLOW_RANGE -> Config.COMMON.zombieVillagerFollowRangeMultiplier.get();
                case KNOCKBACK_RESISTANCE -> Config.COMMON.zombieVillagerKnockbackResistanceMultiplier.get();
            };
        };
    }

    private static double getVariantBonus(SpawnVariant variant, AttributeKey key) {
        return switch (variant) {
            case ZOMBIE -> switch (key) {
                case HEALTH -> Config.COMMON.zombieHealthBonus.get();
                case ATTACK -> Config.COMMON.zombieAttackBonus.get();
                case SPEED -> Config.COMMON.zombieSpeedBonus.get();
                case ARMOR -> Config.COMMON.zombieArmorBonus.get();
                case FOLLOW_RANGE -> Config.COMMON.zombieFollowRangeBonus.get();
                case KNOCKBACK_RESISTANCE -> Config.COMMON.zombieKnockbackResistanceBonus.get();
            };
            case HUSK -> switch (key) {
                case HEALTH -> Config.COMMON.huskHealthBonus.get();
                case ATTACK -> Config.COMMON.huskAttackBonus.get();
                case SPEED -> Config.COMMON.huskSpeedBonus.get();
                case ARMOR -> Config.COMMON.huskArmorBonus.get();
                case FOLLOW_RANGE -> Config.COMMON.huskFollowRangeBonus.get();
                case KNOCKBACK_RESISTANCE -> Config.COMMON.huskKnockbackResistanceBonus.get();
            };
            case DROWNED -> switch (key) {
                case HEALTH -> Config.COMMON.drownedHealthBonus.get();
                case ATTACK -> Config.COMMON.drownedAttackBonus.get();
                case SPEED -> Config.COMMON.drownedSpeedBonus.get();
                case ARMOR -> Config.COMMON.drownedArmorBonus.get();
                case FOLLOW_RANGE -> Config.COMMON.drownedFollowRangeBonus.get();
                case KNOCKBACK_RESISTANCE -> Config.COMMON.drownedKnockbackResistanceBonus.get();
            };
            case ZOMBIE_VILLAGER -> switch (key) {
                case HEALTH -> Config.COMMON.zombieVillagerHealthBonus.get();
                case ATTACK -> Config.COMMON.zombieVillagerAttackBonus.get();
                case SPEED -> Config.COMMON.zombieVillagerSpeedBonus.get();
                case ARMOR -> Config.COMMON.zombieVillagerArmorBonus.get();
                case FOLLOW_RANGE -> Config.COMMON.zombieVillagerFollowRangeBonus.get();
                case KNOCKBACK_RESISTANCE -> Config.COMMON.zombieVillagerKnockbackResistanceBonus.get();
            };
        };
    }

    private static double getContextMultiplier(SpawnContext context, AttributeKey key) {
        double multiplier = 1.0;

        if (context.desertBiome()) {
            multiplier *= switch (key) {
                case HEALTH -> Config.COMMON.desertHealthMultiplier.get();
                case ATTACK -> Config.COMMON.desertAttackMultiplier.get();
                case SPEED -> Config.COMMON.desertSpeedMultiplier.get();
                case ARMOR -> Config.COMMON.desertArmorMultiplier.get();
                case FOLLOW_RANGE -> Config.COMMON.desertFollowRangeMultiplier.get();
                case KNOCKBACK_RESISTANCE -> Config.COMMON.desertKnockbackResistanceMultiplier.get();
            };
        }

        if (context.waterBiome()) {
            multiplier *= switch (key) {
                case HEALTH -> Config.COMMON.waterHealthMultiplier.get();
                case ATTACK -> Config.COMMON.waterAttackMultiplier.get();
                case SPEED -> Config.COMMON.waterSpeedMultiplier.get();
                case ARMOR -> Config.COMMON.waterArmorMultiplier.get();
                case FOLLOW_RANGE -> Config.COMMON.waterFollowRangeMultiplier.get();
                case KNOCKBACK_RESISTANCE -> Config.COMMON.waterKnockbackResistanceMultiplier.get();
            };
        }

        if (context.mushroomBiome()) {
            multiplier *= switch (key) {
                case HEALTH -> Config.COMMON.mushroomHealthMultiplier.get();
                case ATTACK -> Config.COMMON.mushroomAttackMultiplier.get();
                case SPEED -> Config.COMMON.mushroomSpeedMultiplier.get();
                case ARMOR -> Config.COMMON.mushroomArmorMultiplier.get();
                case FOLLOW_RANGE -> Config.COMMON.mushroomFollowRangeMultiplier.get();
                case KNOCKBACK_RESISTANCE -> Config.COMMON.mushroomKnockbackResistanceMultiplier.get();
            };
        }

        if (context.netherDimension()) {
            multiplier *= switch (key) {
                case HEALTH -> Config.COMMON.netherHealthMultiplier.get();
                case ATTACK -> Config.COMMON.netherAttackMultiplier.get();
                case SPEED -> Config.COMMON.netherSpeedMultiplier.get();
                case ARMOR -> Config.COMMON.netherArmorMultiplier.get();
                case FOLLOW_RANGE -> Config.COMMON.netherFollowRangeMultiplier.get();
                case KNOCKBACK_RESISTANCE -> Config.COMMON.netherKnockbackResistanceMultiplier.get();
            };
        }

        if (context.endDimension()) {
            multiplier *= switch (key) {
                case HEALTH -> Config.COMMON.endHealthMultiplier.get();
                case ATTACK -> Config.COMMON.endAttackMultiplier.get();
                case SPEED -> Config.COMMON.endSpeedMultiplier.get();
                case ARMOR -> Config.COMMON.endArmorMultiplier.get();
                case FOLLOW_RANGE -> Config.COMMON.endFollowRangeMultiplier.get();
                case KNOCKBACK_RESISTANCE -> Config.COMMON.endKnockbackResistanceMultiplier.get();
            };
        }

        return multiplier;
    }

    private static double safeFactor(double value) {
        if (!Double.isFinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static double safeMultiplier(double value) {
        if (!Double.isFinite(value)) {
            return 1.0;
        }
        return Math.max(0.0, value);
    }

    private static double safeValue(double value) {
        if (!Double.isFinite(value)) {
            return 0.0;
        }
        return value;
    }

    private static void applyRandomArmor(Zombie zombie, RandomSource random, double factor) {
        if (random.nextFloat() < 0.4F + (float) factor * 0.4F) {
            zombie.setItemSlot(EquipmentSlot.HEAD, getRandomArmor(random, factor, EquipmentSlot.HEAD));
        }
        if (random.nextFloat() < 0.3F + (float) factor * 0.4F) {
            zombie.setItemSlot(EquipmentSlot.CHEST, getRandomArmor(random, factor, EquipmentSlot.CHEST));
        }
        if (random.nextFloat() < 0.2F + (float) factor * 0.4F) {
            zombie.setItemSlot(EquipmentSlot.LEGS, getRandomArmor(random, factor, EquipmentSlot.LEGS));
        }
        if (random.nextFloat() < 0.3F + (float) factor * 0.4F) {
            zombie.setItemSlot(EquipmentSlot.FEET, getRandomArmor(random, factor, EquipmentSlot.FEET));
        }
    }

    private static ItemStack getRandomArmor(RandomSource random, double factor, EquipmentSlot slot) {
        double roll = random.nextDouble();

        double leatherMax = 0.5 - factor * 0.3;
        double chainMax = leatherMax + 0.2;
        double ironMax = chainMax + 0.2 + factor * 0.1;

        ArmorTier tier;
        if (roll < leatherMax) {
            tier = ArmorTier.LEATHER;
        } else if (roll < chainMax) {
            tier = ArmorTier.CHAINMAIL;
        } else if (roll < ironMax) {
            tier = ArmorTier.IRON;
        } else {
            tier = ArmorTier.DIAMOND;
        }

        return new ItemStack(getArmorItem(tier, slot));
    }

    private static Item getArmorItem(ArmorTier tier, EquipmentSlot slot) {
        return switch (tier) {
            case LEATHER -> switch (slot) {
                case HEAD -> Items.LEATHER_HELMET;
                case CHEST -> Items.LEATHER_CHESTPLATE;
                case LEGS -> Items.LEATHER_LEGGINGS;
                case FEET -> Items.LEATHER_BOOTS;
                default -> throw new IllegalArgumentException("Invalid armor slot: " + slot);
            };
            case CHAINMAIL -> switch (slot) {
                case HEAD -> Items.CHAINMAIL_HELMET;
                case CHEST -> Items.CHAINMAIL_CHESTPLATE;
                case LEGS -> Items.CHAINMAIL_LEGGINGS;
                case FEET -> Items.CHAINMAIL_BOOTS;
                default -> throw new IllegalArgumentException("Invalid armor slot: " + slot);
            };
            case IRON -> switch (slot) {
                case HEAD -> Items.IRON_HELMET;
                case CHEST -> Items.IRON_CHESTPLATE;
                case LEGS -> Items.IRON_LEGGINGS;
                case FEET -> Items.IRON_BOOTS;
                default -> throw new IllegalArgumentException("Invalid armor slot: " + slot);
            };
            case DIAMOND -> switch (slot) {
                case HEAD -> Items.DIAMOND_HELMET;
                case CHEST -> Items.DIAMOND_CHESTPLATE;
                case LEGS -> Items.DIAMOND_LEGGINGS;
                case FEET -> Items.DIAMOND_BOOTS;
                default -> throw new IllegalArgumentException("Invalid armor slot: " + slot);
            };
        };
    }

    private static void applyRandomWeapon(Zombie zombie, RandomSource random, double factor) {
        double roll = random.nextDouble();

        double woodenMax = 0.4 - factor * 0.2;
        double stoneMax = woodenMax + 0.25;
        double ironMax = stoneMax + 0.2 + factor * 0.1;

        ItemStack weapon;
        if (roll < woodenMax) {
            weapon = new ItemStack(Items.WOODEN_SWORD);
        } else if (roll < stoneMax) {
            weapon = new ItemStack(Items.STONE_SWORD);
        } else if (roll < ironMax) {
            weapon = new ItemStack(Items.IRON_SWORD);
        } else {
            weapon = new ItemStack(Items.DIAMOND_SWORD);
        }

        zombie.setItemSlot(EquipmentSlot.MAINHAND, weapon);
    }

    public static String getScalingStatus(ServerLevel level) {
        long currentDay = getCurrentDay(level);
        double factor = getScalingFactor(level);
        int percentage = (int) Math.round(factor * 100.0);
        int healthBonus = (int) Math.round(Config.COMMON.maxHealthBoost.get() * factor);
        int speedBonusPercent = (int) Math.round(Config.COMMON.maxSpeedBoost.get() * factor * 100.0);
        String scalingEnabled = Config.COMMON.enableDifficultyScaling.get() ? "ENABLED" : "DISABLED";
        String attrs = Config.COMMON.enableAttributeModifiers.get() ? "ON" : "OFF";
        String attrScaling = Config.COMMON.scaleAttributesWithDifficulty.get() ? "ON" : "OFF";
        String variantProfiles = Config.COMMON.enableVariantAttributeProfiles.get() ? "ON" : "OFF";
        String contextProfiles = Config.COMMON.enableBiomeDimensionAttributeMultipliers.get() ? "ON" : "OFF";

        return String.format(
                "Day %d | Difficulty scaling %s (%d%%) | Legacy Speed +%d%% | Legacy Health +%d | Attr %s | Attr scaling %s | Variant profiles %s | Context profiles %s | Base H x%.2f ATK x%.2f SPD x%.2f",
                currentDay,
                scalingEnabled,
                percentage,
                speedBonusPercent,
                healthBonus,
                attrs,
                attrScaling,
                variantProfiles,
                contextProfiles,
                Config.COMMON.baseHealthMultiplier.get(),
                Config.COMMON.baseAttackMultiplier.get(),
                Config.COMMON.baseSpeedMultiplier.get());
    }
}
