package com.rique.zombieapocalypse;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;

public final class ZombieCompatibility {

    public static final TagKey<EntityType<?>> ZOMBIE_CLASS = entityTypeTag("zombie_class");
    public static final TagKey<EntityType<?>> EXTERNALLY_MANAGED_AI = entityTypeTag("externally_managed_ai");
    public static final TagKey<EntityType<?>> EXTERNALLY_MANAGED_DIFFICULTY = entityTypeTag("externally_managed_difficulty");

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<KnownMod> KNOWN_MODS = List.of(
            new KnownMod("zombieawareness", "Zombie Awareness"),
            new KnownMod("zombiehorsespawn", "Zombie Horse Spawn"),
            new KnownMod("mozombies_wave", "Mo' Zombies Wave"),
            new KnownMod("morezombievillagers", "More Zombie Villagers"),
            new KnownMod("zombies_reworked", "Zombies Reworked"),
            new KnownMod("zombievillagersfromspawner", "Zombie Villagers From Spawner"),
            new KnownMod("zombie_variants", "Zombie Variants"),
            new KnownMod("zombies_plus", "Zombies+"),
            new KnownMod("zombieproofdoors", "Zombie Proof Doors"),
            new KnownMod("undeadnights", "Undead Nights"),
            new KnownMod("hordes", "The Hordes"),
            new KnownMod("improvedmobs", "Improved Mobs"),
            new KnownMod("incontrol", "In Control!"),
            new KnownMod("badmobs", "Bad Mobs"),
            new KnownMod("giantspawn", "Giant Spawn"));

    private static volatile ConfiguredIds configuredIds = new ConfiguredIds("", "", Set.of(), Set.of());

    private ZombieCompatibility() {
    }

    public static boolean isZombieClass(Entity entity) {
        EntityType<?> type = entity.getType();
        if (isExcluded(type)) {
            return false;
        }
        if (vanillaZombieTypes().contains(type) || isIncluded(type) || isInTag(type, ZOMBIE_CLASS)) {
            return true;
        }
        if (entity instanceof Zombie) {
            return Config.COMMON.enableModdedZombieCompatibility.get();
        }
        return type == EntityType.GIANT && LoadedMods.isLoaded("giantspawn");
    }

    public static boolean isZombieClass(EntityType<?> type) {
        if (isExcluded(type)) {
            return false;
        }
        return vanillaZombieTypes().contains(type)
                || isIncluded(type)
                || isInTag(type, ZOMBIE_CLASS)
                || (type == EntityType.GIANT && LoadedMods.isLoaded("giantspawn"));
    }

    public static boolean shouldApplyDifficulty(Mob mob) {
        if (!isZombieClass(mob)) {
            return false;
        }

        boolean vanillaType = vanillaZombieTypes().contains(mob.getType());
        if (!vanillaType && !Config.COMMON.applyDifficultyToModdedZombies.get()) {
            return false;
        }

        return !Config.COMMON.respectExternalDifficulty.get() || !isExternallyManagedDifficulty(mob.getType());
    }

    public static boolean shouldUseAddonAi(Zombie zombie) {
        boolean vanillaType = vanillaZombieTypes().contains(zombie.getType());
        return shouldUseAddonAi(
                isZombieClass(zombie),
                vanillaType,
                Config.COMMON.applyAiFeaturesToModdedZombies.get(),
                Config.COMMON.respectExternalZombieAi.get(),
                isExternallyManagedAi(zombie.getType()));
    }

    public static boolean shouldRespectDoorBreakingAbility() {
        return Config.COMMON.respectZombieDoorBreakingAbility.get();
    }

    public static boolean isVanillaZombieType(EntityType<?> type) {
        return vanillaZombieTypes().contains(type);
    }

    public static List<String> loadedKnownMods() {
        return KNOWN_MODS.stream()
                .filter(mod -> LoadedMods.isLoaded(mod.id()))
                .map(KnownMod::name)
                .toList();
    }

    static Set<ResourceLocation> parseEntityTypeIds(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        Set<ResourceLocation> result = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(entry -> !entry.isEmpty())
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return Collections.unmodifiableSet(result);
    }

    static boolean shouldUseAddonAi(
            boolean zombieClass,
            boolean vanillaType,
            boolean moddedAiEnabled,
            boolean respectExternalAi,
            boolean externallyManaged) {
        return zombieClass
                && (vanillaType || moddedAiEnabled)
                && (!respectExternalAi || !externallyManaged);
    }

    private static boolean isExternallyManagedAi(EntityType<?> type) {
        return isInTag(type, EXTERNALLY_MANAGED_AI)
                || LoadedMods.isLoaded("zombies_reworked")
                || LoadedMods.isLoaded("improvedmobs");
    }

    private static boolean isExternallyManagedDifficulty(EntityType<?> type) {
        return isInTag(type, EXTERNALLY_MANAGED_DIFFICULTY)
                || LoadedMods.isLoaded("improvedmobs");
    }

    private static boolean isIncluded(EntityType<?> type) {
        return currentConfiguredIds().included().contains(EntityType.getKey(type));
    }

    private static boolean isExcluded(EntityType<?> type) {
        return currentConfiguredIds().excluded().contains(EntityType.getKey(type));
    }

    private static ConfiguredIds currentConfiguredIds() {
        String includedRaw = Config.COMMON.additionalZombieEntityTypes.get();
        String excludedRaw = Config.COMMON.excludedZombieEntityTypes.get();
        ConfiguredIds current = configuredIds;
        if (Objects.equals(current.includedRaw(), includedRaw) && Objects.equals(current.excludedRaw(), excludedRaw)) {
            return current;
        }

        synchronized (ZombieCompatibility.class) {
            current = configuredIds;
            if (Objects.equals(current.includedRaw(), includedRaw) && Objects.equals(current.excludedRaw(), excludedRaw)) {
                return current;
            }

            Set<ResourceLocation> included = parseEntityTypeIds(includedRaw);
            Set<ResourceLocation> excluded = parseEntityTypeIds(excludedRaw);
            configuredIds = new ConfiguredIds(includedRaw, excludedRaw, included, excluded);
            logInvalidEntries("additionalZombieEntityTypes", includedRaw, included);
            logInvalidEntries("excludedZombieEntityTypes", excludedRaw, excluded);
            return configuredIds;
        }
    }

    private static void logInvalidEntries(String setting, String rawValue, Set<ResourceLocation> parsed) {
        if (!Config.COMMON.enableDebugLogging.get() || rawValue == null || rawValue.isBlank()) {
            return;
        }

        long nonEmptyEntries = Arrays.stream(rawValue.split(","))
                .map(String::trim)
                .filter(entry -> !entry.isEmpty())
                .count();
        if (parsed.size() != nonEmptyEntries) {
            LOGGER.warn("[ZombieApocalypse] {} contains an invalid entity ID: {}", setting, rawValue);
        }
    }

    private static boolean isInTag(EntityType<?> type, TagKey<EntityType<?>> tag) {
        return type.is(tag);
    }

    private static TagKey<EntityType<?>> entityTypeTag(String path) {
        ResourceLocation id = Objects.requireNonNull(ResourceLocation.tryParse(ZombieApocalypseAddon.MODID + ":" + path));
        return TagKey.create(Registries.ENTITY_TYPE, id);
    }

    private static Set<EntityType<?>> vanillaZombieTypes() {
        return VanillaTypes.TYPES;
    }

    private static final class VanillaTypes {
        private static final Set<EntityType<?>> TYPES = Set.of(
                EntityType.ZOMBIE,
                EntityType.HUSK,
                EntityType.DROWNED,
                EntityType.ZOMBIE_VILLAGER);

        private VanillaTypes() {
        }
    }

    private record KnownMod(String id, String name) {
    }

    private record ConfiguredIds(
            String includedRaw,
            String excludedRaw,
            Set<ResourceLocation> included,
            Set<ResourceLocation> excluded) {
    }

}
