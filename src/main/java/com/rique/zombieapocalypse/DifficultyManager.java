package com.rique.zombieapocalypse;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Applies day-based scaling to zombie stats and equipment.
 */
public final class DifficultyManager {

    private static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
            ZombieApocalypseAddon.MODID, "difficulty_speed");
    private static final ResourceLocation HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(
            ZombieApocalypseAddon.MODID, "difficulty_health");

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
        if (!Config.COMMON.enableDifficultyScaling.get()) {
            return;
        }

        double factor = getScalingFactor(level);
        if (factor <= 0.0) {
            return;
        }

        RandomSource random = level.getRandom();

        double speedBoost = Config.COMMON.maxSpeedBoost.get() * factor;
        if (speedBoost > 0 && zombie.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            zombie.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER_ID);
            zombie.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(
                    new AttributeModifier(SPEED_MODIFIER_ID, speedBoost, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }

        int maxHealthBoost = Config.COMMON.maxHealthBoost.get();
        int healthBoost = (int) Math.round(maxHealthBoost * factor);
        if (healthBoost > 0 && zombie.getAttribute(Attributes.MAX_HEALTH) != null) {
            zombie.getAttribute(Attributes.MAX_HEALTH).removeModifier(HEALTH_MODIFIER_ID);
            zombie.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(
                    new AttributeModifier(HEALTH_MODIFIER_ID, healthBoost, AttributeModifier.Operation.ADD_VALUE));
            zombie.setHealth(zombie.getMaxHealth());
        }

        if (random.nextDouble() < Config.COMMON.maxArmorChance.get() * factor) {
            applyRandomArmor(zombie, random, factor);
        }

        if (random.nextDouble() < Config.COMMON.maxWeaponChance.get() * factor) {
            applyRandomWeapon(zombie, random, factor);
        }
    }

    private static void applyRandomArmor(Zombie zombie, RandomSource random, double factor) {
        ItemStack helmet = getRandomArmor(random, factor, "helmet");
        ItemStack chest = getRandomArmor(random, factor, "chestplate");
        ItemStack legs = getRandomArmor(random, factor, "leggings");
        ItemStack boots = getRandomArmor(random, factor, "boots");

        if (helmet != null && random.nextFloat() < 0.4F + (float) factor * 0.4F) {
            zombie.setItemSlot(EquipmentSlot.HEAD, helmet);
        }
        if (chest != null && random.nextFloat() < 0.3F + (float) factor * 0.4F) {
            zombie.setItemSlot(EquipmentSlot.CHEST, chest);
        }
        if (legs != null && random.nextFloat() < 0.2F + (float) factor * 0.4F) {
            zombie.setItemSlot(EquipmentSlot.LEGS, legs);
        }
        if (boots != null && random.nextFloat() < 0.3F + (float) factor * 0.4F) {
            zombie.setItemSlot(EquipmentSlot.FEET, boots);
        }
    }

    private static ItemStack getRandomArmor(RandomSource random, double factor, String type) {
        double roll = random.nextDouble();

        double leatherMax = 0.5 - factor * 0.3;
        double chainMax = leatherMax + 0.2;
        double ironMax = chainMax + 0.2 + factor * 0.1;

        return switch (type) {
            case "helmet" -> {
                if (roll < leatherMax) {
                    yield new ItemStack(Items.LEATHER_HELMET);
                } else if (roll < chainMax) {
                    yield new ItemStack(Items.CHAINMAIL_HELMET);
                } else if (roll < ironMax) {
                    yield new ItemStack(Items.IRON_HELMET);
                }
                yield new ItemStack(Items.DIAMOND_HELMET);
            }
            case "chestplate" -> {
                if (roll < leatherMax) {
                    yield new ItemStack(Items.LEATHER_CHESTPLATE);
                } else if (roll < chainMax) {
                    yield new ItemStack(Items.CHAINMAIL_CHESTPLATE);
                } else if (roll < ironMax) {
                    yield new ItemStack(Items.IRON_CHESTPLATE);
                }
                yield new ItemStack(Items.DIAMOND_CHESTPLATE);
            }
            case "leggings" -> {
                if (roll < leatherMax) {
                    yield new ItemStack(Items.LEATHER_LEGGINGS);
                } else if (roll < chainMax) {
                    yield new ItemStack(Items.CHAINMAIL_LEGGINGS);
                } else if (roll < ironMax) {
                    yield new ItemStack(Items.IRON_LEGGINGS);
                }
                yield new ItemStack(Items.DIAMOND_LEGGINGS);
            }
            case "boots" -> {
                if (roll < leatherMax) {
                    yield new ItemStack(Items.LEATHER_BOOTS);
                } else if (roll < chainMax) {
                    yield new ItemStack(Items.CHAINMAIL_BOOTS);
                } else if (roll < ironMax) {
                    yield new ItemStack(Items.IRON_BOOTS);
                }
                yield new ItemStack(Items.DIAMOND_BOOTS);
            }
            default -> null;
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
        if (!Config.COMMON.enableDifficultyScaling.get()) {
            return "Difficulty scaling: DISABLED";
        }

        long currentDay = getCurrentDay(level);
        double factor = getScalingFactor(level);
        int percentage = (int) Math.round(factor * 100.0);
        int healthBonus = (int) Math.round(Config.COMMON.maxHealthBoost.get() * factor);
        int speedBonusPercent = (int) Math.round(Config.COMMON.maxSpeedBoost.get() * factor * 100.0);

        return String.format(
                "Day %d | Scaling %d%% | Speed +%d%% | Health +%d",
                currentDay,
                percentage,
                speedBonusPercent,
                healthBonus);
    }
}
