package com.rique.zombieapocalypse;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Loader-facing plumbing for the animal-hunting feature.
 *
 * This is an adapter file: the Forge 1.20.1 build keeps its own copy because
 * attribute modifiers, food data, loot tables, and conversion hooks all differ
 * between 1.20.1 and 1.21.1. Keep this file free of gameplay decisions.
 */
public final class ZombieHuntCompat {

    static final ResourceLocation FEEDING_BOOST_ID =
            ResourceLocation.fromNamespaceAndPath(ZombieApocalypseAddon.MODID, "feeding_health_boost");
    static final ResourceLocation LEADER_BONUS_ID =
            ResourceLocation.fromNamespaceAndPath(ZombieApocalypseAddon.MODID, "fed_leader_bonus");

    private ZombieHuntCompat() {
    }

    public static boolean isFoodItem(ItemStack stack, LivingEntity eater) {
        return stack.has(DataComponents.FOOD);
    }

    public static int nutritionOf(ItemStack stack, LivingEntity eater) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        return food == null ? 0 : food.nutrition();
    }

    public static double getFeedingBoost(LivingEntity entity) {
        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return 0.0;
        }
        AttributeModifier modifier = maxHealth.getModifier(FEEDING_BOOST_ID);
        return modifier == null ? 0.0 : modifier.amount();
    }

    public static void setFeedingBoost(LivingEntity entity, double amount) {
        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }
        maxHealth.removeModifier(FEEDING_BOOST_ID);
        if (amount > 0.0) {
            maxHealth.addPermanentModifier(
                    new AttributeModifier(FEEDING_BOOST_ID, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    public static boolean hasLeaderBonus(Mob mob) {
        AttributeInstance reinforcement = mob.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
        return reinforcement != null && reinforcement.getModifier(LEADER_BONUS_ID) != null;
    }

    public static void applyLeaderBonus(Mob mob, double amount) {
        AttributeInstance reinforcement = mob.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
        if (reinforcement == null || reinforcement.getModifier(LEADER_BONUS_ID) != null) {
            return;
        }
        reinforcement.addPermanentModifier(
                new AttributeModifier(LEADER_BONUS_ID, amount, AttributeModifier.Operation.ADD_VALUE));
        if (mob instanceof Zombie zombie) {
            zombie.setCanBreakDoors(true);
        }
    }

    public static boolean canConvertToZombieHorse(LivingEntity entity) {
        return EventHooks.canLivingConvert(entity, EntityType.ZOMBIE_HORSE, timer -> {
        });
    }

    public static void onConverted(LivingEntity before, LivingEntity after) {
        EventHooks.onLivingConvert(before, after);
    }

    public static void equipSaddle(ZombieHorse zombieHorse) {
        zombieHorse.equipSaddle(
                new ItemStack(net.minecraft.world.item.Items.SADDLE),
                net.minecraft.sounds.SoundSource.NEUTRAL);
    }

    public static void copyHorseGear(Horse horse, ZombieHorse zombieHorse) {
        ItemStack armor = horse.getItemBySlot(EquipmentSlot.BODY);
        if (!armor.isEmpty()) {
            zombieHorse.spawnAtLocation(armor.copy());
        }
    }

    public static void spawnBonusLoot(LivingEntity zombie, ServerLevel level, DamageSource source, int rolls) {
        if (rolls <= 0) {
            return;
        }
        LootTable table = level.getServer().reloadableRegistries().getLootTable(zombie.getLootTable());
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, zombie)
                .withParameter(LootContextParams.ORIGIN, zombie.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, source.getEntity())
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity())
                .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER,
                        source.getEntity() instanceof net.minecraft.world.entity.player.Player player ? player : null)
                .create(LootContextParamSets.ENTITY);
        for (int i = 0; i < rolls; i++) {
            for (ItemStack stack : table.getRandomItems(params)) {
                if (!stack.isEmpty()) {
                    ItemEntity drop = new ItemEntity(level, zombie.getX(), zombie.getY(), zombie.getZ(), stack);
                    drop.setDefaultPickUpDelay();
                    level.addFreshEntity(drop);
                }
            }
        }
    }
}
