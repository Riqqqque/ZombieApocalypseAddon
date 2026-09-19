package com.rique.zombieapocalypse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Animal-hunting engine: zombies hunt animals, eat dropped meat, heal, and
 * grow permanent bonus health. Well-fed zombies can persist, drop extra loot,
 * and promote to horde leaders. Horses killed by zombies can rise as zombie
 * horses, and idle zombies can ride them.
 *
 * Mechanics inspired by the MIT-licensed "Zombies Eat Animals" mod by Vomiter.
 *
 * Every mechanic is gated by {@code enableAnimalHunting}, which defaults off.
 * All version-divergent calls go through {@link ZombieHuntCompat}.
 */
public final class ZombieHunt {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final TagKey<Item> ZOMBIE_FOOD =
            TagKey.create(Registries.ITEM, id("zombie_food"));
    public static final TagKey<EntityType<?>> HUNT_TARGETS =
            TagKey.create(Registries.ENTITY_TYPE, id("zombie_hunt_target"));
    public static final TagKey<EntityType<?>> NEVER_HUNT_TARGETS =
            TagKey.create(Registries.ENTITY_TYPE, id("never_zombie_hunt_target"));

    private static final String HUNT_DAY_TAG = "zombieapocalypse.hunt_day";
    private static final String HUNT_COUNT_TAG = "zombieapocalypse.hunt_count";
    private static final String LAST_HUNT_TICK_TAG = "zombieapocalypse.last_hunt_tick";
    private static final String LAST_EAT_TICK_TAG = "zombieapocalypse.last_eat_tick";
    private static final String LEADER_PENDING_TAG = "zombieapocalypse.leader_pending";

    private static final long ONE_DAY_TICKS = 24000L;
    private static final int LEADER_CHECK_INTERVAL_TICKS = 20;
    private static final double MIN_LEADER_REINFORCEMENT = 0.5;
    private static final double MAX_LEADER_REINFORCEMENT = 0.75;
    private static final int ROTTEN_FLESH_RESISTANCE_TICKS = 20 * 30;

    private record DayCount(long day, int count) {
    }

    private static final Map<ResourceLocation, DayCount> LEVEL_HUNT_COUNTS = new HashMap<>();

    private record ConfiguredTargets(String includedRaw, String excludedRaw,
                                     Set<ResourceLocation> included, Set<ResourceLocation> excluded) {
    }

    private static volatile ConfiguredTargets configuredTargets =
            new ConfiguredTargets("", "", Set.of(), Set.of());

    private ZombieHunt() {
    }

    private static ResourceLocation id(String path) {
        return Objects.requireNonNull(
                ResourceLocation.tryParse(ZombieApocalypseAddon.MODID + ":" + path));
    }

    static boolean isEnabled() {
        return Config.COMMON.enableAnimalHunting.get();
    }

    /**
     * Installs the hunt, eat, and mount goals on a zombie. Goals are always
     * added so runtime config toggles affect already-loaded zombies; every
     * behavior is gated inside the goals. Safe to call on every join.
     */
    static void injectGoals(Zombie zombie) {
        if (zombie.goalSelector.getAvailableGoals().stream()
                .noneMatch(goal -> goal.getGoal() instanceof ZombieEatFoodGoal)) {
            zombie.goalSelector.addGoal(1, new ZombieEatFoodGoal(zombie));
        }
        if (zombie.targetSelector.getAvailableGoals().stream()
                .noneMatch(goal -> goal.getGoal() instanceof ZombieHuntGoal)) {
            zombie.targetSelector.addGoal(3, new ZombieHuntGoal(zombie));
        }
        if (zombie.goalSelector.getAvailableGoals().stream()
                .noneMatch(goal -> goal.getGoal() instanceof ZombieMountZombieHorseGoal)) {
            zombie.goalSelector.addGoal(4, new ZombieMountZombieHorseGoal(zombie));
        }
    }

    // ---- Hunt target validity ------------------------------------------------

    static boolean isHuntableTarget(Zombie hunter, LivingEntity candidate) {
        EntityType<?> type = candidate.getType();
        if (isExcludedTarget(type) || type.is(NEVER_HUNT_TARGETS)) {
            return false;
        }
        if (type.is(HUNT_TARGETS) || isIncludedTarget(type)) {
            return true;
        }
        if (!(candidate instanceof Animal animal)) {
            return false;
        }
        if (animal instanceof ZombieHorse && Config.COMMON.neverHuntZombieHorses.get()) {
            return false;
        }
        if (animal.isBaby() && !Config.COMMON.huntBabies.get()) {
            return false;
        }
        if (animal.isVehicle() && animal.hasPassenger(e -> e instanceof Zombie)) {
            return false;
        }
        if (animal instanceof Horse horse && Config.COMMON.zombifyHorses.get()) {
            return !horse.isTamed() || Config.COMMON.zombifyTamedHorses.get();
        }
        return true;
    }

    private static boolean isIncludedTarget(EntityType<?> type) {
        return currentConfiguredTargets().included().contains(EntityType.getKey(type));
    }

    private static boolean isExcludedTarget(EntityType<?> type) {
        return currentConfiguredTargets().excluded().contains(EntityType.getKey(type));
    }

    private static ConfiguredTargets currentConfiguredTargets() {
        String includedRaw = Config.COMMON.additionalHuntTargets.get();
        String excludedRaw = Config.COMMON.excludedHuntTargets.get();
        ConfiguredTargets current = configuredTargets;
        if (Objects.equals(current.includedRaw(), includedRaw)
                && Objects.equals(current.excludedRaw(), excludedRaw)) {
            return current;
        }
        synchronized (ZombieHunt.class) {
            current = configuredTargets;
            if (Objects.equals(current.includedRaw(), includedRaw)
                    && Objects.equals(current.excludedRaw(), excludedRaw)) {
                return current;
            }
            configuredTargets = new ConfiguredTargets(
                    includedRaw, excludedRaw,
                    ZombieCompatibility.parseEntityTypeIds(includedRaw),
                    ZombieCompatibility.parseEntityTypeIds(excludedRaw));
            return configuredTargets;
        }
    }

    // ---- Hunt eligibility (motivation, cooldown, caps) -----------------------

    static boolean canSeekTarget(Zombie zombie, ServerLevel level) {
        if (!isEnabled() || zombie.isBaby() || zombie.isPassenger() || zombie.isVehicle()) {
            return false;
        }
        long day = DifficultyManager.getCurrentDay(level);
        if (day < Math.max(0, Config.COMMON.huntStartDay.get())) {
            return false;
        }
        if (Config.COMMON.berserkerHunting.get()) {
            return true;
        }
        if (!hasHuntMotivation(zombie)) {
            return false;
        }
        long gameTime = level.getGameTime();
        CompoundTag data = zombie.getPersistentData();
        long lastHunt = data.getLong(LAST_HUNT_TICK_TAG);
        if (lastHunt > 0L && gameTime - lastHunt < Config.COMMON.huntCooldownTicks.get()) {
            return false;
        }
        return isUnderCaps(zombie, level, day);
    }

    private static boolean hasHuntMotivation(Zombie zombie) {
        return Config.COMMON.alwaysHunting.get()
                || isNotMaxed(zombie)
                || Config.COMMON.zombifyHorses.get();
    }

    static boolean isNotMaxed(Zombie zombie) {
        if (zombie.getMaxHealth() - zombie.getHealth() > 2.0F) {
            return true;
        }
        return ZombieHuntCompat.getFeedingBoost(zombie) < maxBoostCap(zombie);
    }

    private static boolean isUnderCaps(Zombie zombie, ServerLevel level, long day) {
        int perZombieCap = Config.COMMON.huntCapPerZombiePerDay.get();
        if (perZombieCap > 0 && huntsToday(zombie, day) >= perZombieCap) {
            return false;
        }
        int perLevelCap = Config.COMMON.huntCapPerLevelPerDay.get();
        return perLevelCap <= 0 || levelHuntsToday(level, day) < perLevelCap;
    }

    static int huntsToday(Zombie zombie, long day) {
        CompoundTag data = zombie.getPersistentData();
        return data.getLong(HUNT_DAY_TAG) == day ? Math.max(0, data.getInt(HUNT_COUNT_TAG)) : 0;
    }

    private static int levelHuntsToday(ServerLevel level, long day) {
        DayCount count = LEVEL_HUNT_COUNTS.get(level.dimension().location());
        return count != null && count.day() == day ? count.count() : 0;
    }

    // ---- Kill handling --------------------------------------------------------

    static void onKilledByZombie(LivingEntity victim, Entity killer) {
        if (!isEnabled() || !(victim.level() instanceof ServerLevel level)) {
            return;
        }
        if (!(killer instanceof Zombie zombie) || !countsAsHuntKill(victim)) {
            return;
        }
        recordKill(zombie, level);
        if (victim instanceof Horse horse) {
            tryZombifyHorse(horse, zombie, level);
        }
    }

    private static boolean countsAsHuntKill(LivingEntity victim) {
        EntityType<?> type = victim.getType();
        if (isExcludedTarget(type) || type.is(NEVER_HUNT_TARGETS)) {
            return false;
        }
        if (type.is(HUNT_TARGETS) || isIncludedTarget(type)) {
            return true;
        }
        return victim instanceof Animal
                && !(victim instanceof ZombieHorse && Config.COMMON.neverHuntZombieHorses.get());
    }

    private static void recordKill(Zombie zombie, ServerLevel level) {
        long day = DifficultyManager.getCurrentDay(level);
        CompoundTag data = zombie.getPersistentData();
        int count = data.getLong(HUNT_DAY_TAG) == day ? Math.max(0, data.getInt(HUNT_COUNT_TAG)) : 0;
        data.putLong(HUNT_DAY_TAG, day);
        data.putInt(HUNT_COUNT_TAG, count + 1);
        data.putLong(LAST_HUNT_TICK_TAG, level.getGameTime());

        ResourceLocation dimension = level.dimension().location();
        DayCount current = LEVEL_HUNT_COUNTS.get(dimension);
        int total = current != null && current.day() == day ? current.count() : 0;
        LEVEL_HUNT_COUNTS.put(dimension, new DayCount(day, total + 1));
    }

    // ---- Eating and growth ----------------------------------------------------

    static boolean canSeekFood(Zombie zombie) {
        if (!isEnabled() || !Config.COMMON.eatDroppedFood.get()
                || zombie.isBaby() || zombie.isPassenger() || zombie.isVehicle()) {
            return false;
        }
        long gameTime = zombie.level().getGameTime();
        long lastEat = zombie.getPersistentData().getLong(LAST_EAT_TICK_TAG);
        if (lastEat > 0L && gameTime - lastEat < Config.COMMON.eatCooldownTicks.get()) {
            return false;
        }
        return isNotMaxed(zombie);
    }

    static boolean isCombatLocked(Zombie zombie) {
        LivingEntity attacker = zombie.getLastHurtByMob();
        if (attacker != null
                && attacker.isAlive()
                && zombie.tickCount - zombie.getLastHurtByMobTimestamp()
                        < Config.COMMON.foodCombatLockTicks.get()) {
            return true;
        }
        LivingEntity target = zombie.getTarget();
        if (target != null && target.isAlive()) {
            double lockDistance = Math.max(0.0, Config.COMMON.foodCombatLockDistance.get());
            return zombie.distanceToSqr(target) <= lockDistance * lockDistance;
        }
        return false;
    }

    static boolean isEdibleFood(ItemStack stack, Zombie zombie) {
        return !stack.isEmpty()
                && stack.is(ZOMBIE_FOOD)
                && ZombieHuntCompat.isFoodItem(stack, zombie);
    }

    static void eatFoodItem(Zombie zombie, ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        int nutrition = ZombieHuntCompat.nutritionOf(stack, zombie);
        int recovery = Math.max(0, nutrition * Math.max(0, Config.COMMON.healthPerNutrition.get()));

        if (stack.is(Items.ROTTEN_FLESH) && Config.COMMON.rottenFleshGivesResistance.get()) {
            zombie.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    ROTTEN_FLESH_RESISTANCE_TICKS,
                    zombie.getRandom().nextInt(3)));
        }

        recoverAndBoostHealth(zombie, recovery);
        zombie.getPersistentData().putLong(LAST_EAT_TICK_TAG, zombie.level().getGameTime());

        if (Config.COMMON.persistentAfterEating.get()) {
            zombie.setPersistenceRequired();
        }

        Level level = zombie.level();
        level.playSound(null, zombie.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 1.0F, 1.0F);
        level.playSound(null, zombie.blockPosition(), SoundEvents.ZOMBIE_AMBIENT, SoundSource.HOSTILE, 0.2F, 0.8F);

        stack.shrink(1);
        if (stack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(stack);
        }
    }

    static void recoverAndBoostHealth(Zombie zombie, int recovery) {
        if (recovery <= 0 || !isNotMaxed(zombie)) {
            return;
        }

        float deficit = zombie.getMaxHealth() - zombie.getHealth();
        if (deficit > 0.0F) {
            float healed = Math.min(deficit, recovery);
            zombie.heal(healed);
            recovery -= (int) healed;
        }
        if (recovery <= 0) {
            return;
        }

        double cap = maxBoostCap(zombie);
        double currentBoost = ZombieHuntCompat.getFeedingBoost(zombie);
        double newBoost = Math.min(cap, currentBoost + recovery);
        if (newBoost <= currentBoost) {
            return;
        }
        ZombieHuntCompat.setFeedingBoost(zombie, newBoost);
        zombie.heal((float) (newBoost - currentBoost));

        if (Config.COMMON.fedZombiesBecomeLeaders.get()
                && newBoost >= Math.max(0, Config.COMMON.feedingMaxHealthBoost.get())) {
            zombie.getPersistentData().putBoolean(LEADER_PENDING_TAG, true);
        }
    }

    private static double maxBoostCap(Zombie zombie) {
        double cap = Math.max(0, Config.COMMON.feedingMaxHealthBoost.get());
        if (zombie.level().getDifficulty() == Difficulty.HARD) {
            cap += Math.max(0, Config.COMMON.feedingMaxHealthBoostHardBonus.get());
        }
        return cap;
    }

    // ---- Leader promotion -----------------------------------------------------

    static void tick(Zombie zombie, long gameTime) {
        if ((gameTime + zombie.getId()) % LEADER_CHECK_INTERVAL_TICKS != 0L) {
            return;
        }
        CompoundTag data = zombie.getPersistentData();
        if (!data.getBoolean(LEADER_PENDING_TAG)) {
            return;
        }
        data.remove(LEADER_PENDING_TAG);
        if (!isEnabled() || !Config.COMMON.fedZombiesBecomeLeaders.get()
                || ZombieHuntCompat.hasLeaderBonus(zombie)) {
            return;
        }
        double amount = MIN_LEADER_REINFORCEMENT
                + zombie.getRandom().nextDouble() * (MAX_LEADER_REINFORCEMENT - MIN_LEADER_REINFORCEMENT);
        ZombieHuntCompat.applyLeaderBonus(zombie, amount);
        if (Config.COMMON.enableDebugLogging.get()) {
            LOGGER.info("[ZombieApocalypse] Well-fed zombie promoted to leader: {}",
                    zombie.getType().getDescriptionId());
        }
    }

    // ---- Horse zombification ---------------------------------------------------

    private static void tryZombifyHorse(Horse horse, Zombie killer, ServerLevel level) {
        if (!Config.COMMON.zombifyHorses.get()) {
            return;
        }
        if (horse.isTamed() && !Config.COMMON.zombifyTamedHorses.get()) {
            return;
        }
        Difficulty difficulty = level.getDifficulty();
        if (difficulty != Difficulty.NORMAL && difficulty != Difficulty.HARD) {
            return;
        }
        if (difficulty != Difficulty.HARD && level.getRandom().nextBoolean()) {
            return;
        }
        if (!ZombieHuntCompat.canConvertToZombieHorse(horse)) {
            return;
        }

        double jumpStrength = horse.getAttributeBaseValue(Attributes.JUMP_STRENGTH);
        double moveSpeed = horse.getAttributeBaseValue(Attributes.MOVEMENT_SPEED);
        boolean tamed = horse.isTamed();
        boolean saddled = horse.isSaddled();
        UUID owner = horse.getOwnerUUID();
        Component name = horse.getCustomName();
        boolean nameVisible = horse.isCustomNameVisible();
        boolean baby = horse.isBaby();

        ZombieHorse zombieHorse = horse.convertTo(EntityType.ZOMBIE_HORSE, false);
        if (zombieHorse == null) {
            return;
        }

        var jumpAttr = zombieHorse.getAttribute(Attributes.JUMP_STRENGTH);
        if (jumpAttr != null) {
            jumpAttr.setBaseValue(jumpStrength);
        }
        var speedAttr = zombieHorse.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(moveSpeed);
        }
        if (tamed) {
            zombieHorse.setTamed(true);
            zombieHorse.setOwnerUUID(owner);
        }
        if (name != null) {
            zombieHorse.setCustomName(name);
            zombieHorse.setCustomNameVisible(nameVisible);
        }
        if (baby) {
            zombieHorse.setBaby(true);
        }
        if (saddled) {
            ZombieHuntCompat.equipSaddle(zombieHorse);
        }
        ZombieHuntCompat.copyHorseGear(horse, zombieHorse);
        ZombieHuntCompat.onConverted(horse, zombieHorse);

        if (!killer.isSilent()) {
            level.playSound(null, zombieHorse.blockPosition(),
                    SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
        if (Config.COMMON.enableDebugLogging.get()) {
            LOGGER.info("[ZombieApocalypse] Horse zombified at {}", zombieHorse.blockPosition());
        }
    }

    // ---- Bonus loot ------------------------------------------------------------

    static int bonusLootRolls(Zombie zombie) {
        if (!isEnabled() || !Config.COMMON.fedZombiesDropExtraLoot.get()) {
            return 0;
        }
        double boost = ZombieHuntCompat.getFeedingBoost(zombie);
        if (boost <= 0.0) {
            return 0;
        }
        double base = zombie.getAttributeBaseValue(Attributes.MAX_HEALTH);
        double ratio = Math.max(0.1, Config.COMMON.extraLootHealthRatio.get());
        return bonusRollCount(boost, base, ratio);
    }

    static int bonusRollCount(double boost, double baseMaxHealth, double ratio) {
        if (baseMaxHealth <= 0.0) {
            return 0;
        }
        return (int) (boost / (baseMaxHealth * ratio));
    }

    static void clearRuntimeState() {
        LEVEL_HUNT_COUNTS.clear();
    }
}
