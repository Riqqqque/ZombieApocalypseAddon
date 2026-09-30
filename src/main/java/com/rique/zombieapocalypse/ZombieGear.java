package com.rique.zombieapocalypse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.UseAnim;

/**
 * Chooses the armor and weapons that day scaling gives zombie-class mobs.
 * Each tier is a configurable item list, so gear from other mods can join the
 * normal progression. Slots are detected from the item itself.
 */
public final class ZombieGear {

    public static final int TIER_COUNT = 4;
    public static final double DEFAULT_DROP_CHANCE = 0.085;

    public static final String DEFAULT_TIER_1 = "minecraft:leather_helmet, minecraft:leather_chestplate, "
            + "minecraft:leather_leggings, minecraft:leather_boots, minecraft:wooden_sword";
    public static final String DEFAULT_TIER_2 = "minecraft:chainmail_helmet, minecraft:chainmail_chestplate, "
            + "minecraft:chainmail_leggings, minecraft:chainmail_boots, minecraft:stone_sword";
    public static final String DEFAULT_TIER_3 = "minecraft:iron_helmet, minecraft:iron_chestplate, "
            + "minecraft:iron_leggings, minecraft:iron_boots, minecraft:iron_sword";
    public static final String DEFAULT_TIER_4 = "minecraft:diamond_helmet, minecraft:diamond_chestplate, "
            + "minecraft:diamond_leggings, minecraft:diamond_boots, minecraft:diamond_sword";

    /** Every slot the addon fills, in display order. Shields and the offhand are never used. */
    public static final List<EquipmentSlot> GEAR_SLOTS = List.of(
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.MAINHAND);

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
    private static final float[] ARMOR_SLOT_BASE_CHANCES = { 0.4F, 0.3F, 0.2F, 0.3F };

    private static volatile Snapshot snapshot;

    private ZombieGear() {
    }

    /**
     * Resolved gear for one tier. Pools keep duplicate entries so listing an
     * item twice doubles its weight.
     */
    public record TierGear(
            int tier,
            Map<EquipmentSlot, List<Item>> pools,
            List<String> skipped,
            boolean vanillaFallback) {

        public List<Item> pool(EquipmentSlot slot) {
            return pools.getOrDefault(slot, List.of());
        }

        public int armorCount() {
            int count = 0;
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                count += pool(slot).size();
            }
            return count;
        }

        public int weaponCount() {
            return pool(EquipmentSlot.MAINHAND).size();
        }
    }

    /** Result of checking one configured or typed item ID. */
    public record Lookup(String id, Item item, EquipmentSlot slot, String problem) {

        static Lookup failed(String id, String problem) {
            return new Lookup(id, null, null, problem);
        }
    }

    private record Snapshot(String[] rawLists, List<TierGear> tiers) {
    }

    static void applyArmor(Mob zombie, RandomSource random, double factor) {
        boolean preserveExisting = Config.COMMON.preserveExistingZombieEquipment.get();
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            EquipmentSlot slot = ARMOR_SLOTS[i];
            if (shouldEquipSlot(zombie.getItemBySlot(slot).isEmpty(), preserveExisting)
                    && random.nextFloat() < ARMOR_SLOT_BASE_CHANCES[i] + (float) factor * 0.4F) {
                equip(zombie, items(zombie), slot, armorTier(random.nextDouble(), factor), random);
            }
        }
    }

    static void applyWeapon(Mob zombie, RandomSource random, double factor) {
        if (shouldEquipSlot(
                zombie.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty(),
                Config.COMMON.preserveExistingZombieEquipment.get())) {
            equip(zombie, items(zombie), EquipmentSlot.MAINHAND, weaponTier(random.nextDouble(), factor), random);
        }
    }

    /**
     * Dresses a zombie in one item per slot from a tier, clearing slots the
     * tier has nothing for. Used by the preview command.
     */
    public static int equipFullTier(Mob zombie, int tier, RandomSource random) {
        Registry<Item> items = items(zombie);
        int equipped = 0;
        for (EquipmentSlot slot : GEAR_SLOTS) {
            if (equip(zombie, items, slot, tier, random)) {
                equipped++;
            } else {
                zombie.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
        return equipped;
    }

    private static boolean equip(Mob zombie, Registry<Item> items, EquipmentSlot slot, int tier, RandomSource random) {
        List<Item> pool = tier(items, tier).pool(slot);
        if (pool.isEmpty()) {
            return false;
        }
        zombie.setItemSlot(slot, new ItemStack(pool.get(random.nextInt(pool.size()))));
        zombie.setDropChance(slot, (float) dropChance());
        return true;
    }

    private static Registry<Item> items(Mob zombie) {
        return zombie.level().registryAccess().registryOrThrow(Registries.ITEM);
    }

    static boolean shouldEquipSlot(boolean slotEmpty, boolean preserveExisting) {
        return slotEmpty || !preserveExisting;
    }

    public static double dropChance() {
        return SpawnMath.clampProbability(Config.COMMON.gearDropChance.get());
    }

    static int armorTier(double roll, double factor) {
        double tier1Max = 0.5 - factor * 0.3;
        double tier2Max = tier1Max + 0.2;
        double tier3Max = tier2Max + 0.2 + factor * 0.1;
        return pickTier(roll, tier1Max, tier2Max, tier3Max);
    }

    static int weaponTier(double roll, double factor) {
        double tier1Max = 0.4 - factor * 0.2;
        double tier2Max = tier1Max + 0.25;
        double tier3Max = tier2Max + 0.2 + factor * 0.1;
        return pickTier(roll, tier1Max, tier2Max, tier3Max);
    }

    private static int pickTier(double roll, double tier1Max, double tier2Max, double tier3Max) {
        if (roll < tier1Max) {
            return 1;
        }
        if (roll < tier2Max) {
            return 2;
        }
        return roll < tier3Max ? 3 : 4;
    }

    /** Chance of each tier (index 0 = tier 1) for one armor piece at the given scaling factor. */
    public static double[] armorTierOdds(double factor) {
        return tierOdds(0.5 - factor * 0.3, 0.2, 0.2 + factor * 0.1);
    }

    /** Chance of each tier (index 0 = tier 1) for a weapon at the given scaling factor. */
    public static double[] weaponTierOdds(double factor) {
        return tierOdds(0.4 - factor * 0.2, 0.25, 0.2 + factor * 0.1);
    }

    private static double[] tierOdds(double tier1, double tier2, double tier3) {
        return new double[] { tier1, tier2, tier3, 1.0 - tier1 - tier2 - tier3 };
    }

    public static TierGear tier(Registry<Item> items, int tier) {
        checkTier(tier);
        return currentSnapshot(items).tiers().get(tier - 1);
    }

    public static String configuredList(int tier) {
        String value = switch (tier) {
            case 1 -> Config.COMMON.tier1Gear.get();
            case 2 -> Config.COMMON.tier2Gear.get();
            case 3 -> Config.COMMON.tier3Gear.get();
            case 4 -> Config.COMMON.tier4Gear.get();
            default -> throw invalidTier(tier);
        };
        return value == null ? "" : value;
    }

    public static void setConfiguredList(int tier, String value) {
        switch (tier) {
            case 1 -> Config.set(Config.COMMON.tier1Gear, value);
            case 2 -> Config.set(Config.COMMON.tier2Gear, value);
            case 3 -> Config.set(Config.COMMON.tier3Gear, value);
            case 4 -> Config.set(Config.COMMON.tier4Gear, value);
            default -> throw invalidTier(tier);
        }
    }

    public static String defaultList(int tier) {
        return switch (tier) {
            case 1 -> DEFAULT_TIER_1;
            case 2 -> DEFAULT_TIER_2;
            case 3 -> DEFAULT_TIER_3;
            case 4 -> DEFAULT_TIER_4;
            default -> throw invalidTier(tier);
        };
    }

    public static String vanillaName(int tier) {
        return switch (tier) {
            case 1 -> "leather armor and a wooden sword";
            case 2 -> "chainmail armor and a stone sword";
            case 3 -> "iron armor and an iron sword";
            case 4 -> "diamond armor and a diamond sword";
            default -> throw invalidTier(tier);
        };
    }

    /** Checks whether an item ID can be used as zombie gear. */
    public static Lookup lookup(Registry<Item> items, String entry) {
        String id = normalizeId(entry);
        if (id == null) {
            return Lookup.failed(entry == null ? "" : entry.trim(), "not a valid item ID");
        }
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null || !items.containsKey(location)) {
            return Lookup.failed(id, "not installed");
        }
        Item item = items.get(location);
        if (item == Items.AIR) {
            return Lookup.failed(id, "not an item");
        }
        EquipmentSlot slot = gearSlot(new ItemStack(item));
        if (slot == null) {
            return Lookup.failed(id, "shields, offhand items, and animal armor are not used");
        }
        return new Lookup(id, item, slot, null);
    }

    /**
     * Returns the slot a zombie would wear an item in, or null when the addon
     * should never hand it out (shields, offhand items, animal armor).
     */
    public static EquipmentSlot gearSlot(ItemStack stack) {
        if (stack.isEmpty()
                || stack.getItem() instanceof ShieldItem
                || stack.getUseAnimation() == UseAnim.BLOCK) {
            return null;
        }
        EquipmentSlot slot = stack.getEquipmentSlot();
        if (slot == null) {
            Equipable equipable = Equipable.get(stack);
            slot = equipable != null ? equipable.getEquipmentSlot() : EquipmentSlot.MAINHAND;
        }
        return switch (slot) {
            case HEAD, CHEST, LEGS, FEET, MAINHAND -> slot;
            default -> null;
        };
    }

    /** Splits a configured list into trimmed, lowercase entries, keeping order and duplicates. */
    public static List<String> parseList(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        List<String> entries = new ArrayList<>();
        for (String part : raw.split(",")) {
            String entry = part.trim().toLowerCase(Locale.ROOT);
            if (!entry.isEmpty()) {
                entries.add(entry);
            }
        }
        return Collections.unmodifiableList(entries);
    }

    /** Returns the full namespaced ID for an entry, or null if it is not a valid ID. */
    public static String normalizeId(String entry) {
        if (entry == null) {
            return null;
        }
        String trimmed = entry.trim().toLowerCase(Locale.ROOT);
        if (trimmed.isEmpty()) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(trimmed);
        return id == null ? null : id.toString();
    }

    public static String withAdded(String raw, String id) {
        List<String> entries = new ArrayList<>(parseList(raw));
        entries.add(id);
        return String.join(", ", entries);
    }

    public static String withRemoved(String raw, String id) {
        List<String> kept = new ArrayList<>();
        for (String entry : parseList(raw)) {
            if (!id.equals(normalizeId(entry))) {
                kept.add(entry);
            }
        }
        return String.join(", ", kept);
    }

    public static int countEntries(String raw, String id) {
        int count = 0;
        for (String entry : parseList(raw)) {
            if (id.equals(normalizeId(entry))) {
                count++;
            }
        }
        return count;
    }

    private static Snapshot currentSnapshot(Registry<Item> items) {
        Snapshot current = snapshot;
        if (current != null && matchesConfig(current)) {
            return current;
        }

        synchronized (ZombieGear.class) {
            current = snapshot;
            if (current != null && matchesConfig(current)) {
                return current;
            }

            String[] rawLists = new String[TIER_COUNT];
            List<TierGear> tiers = new ArrayList<>(TIER_COUNT);
            for (int tier = 1; tier <= TIER_COUNT; tier++) {
                rawLists[tier - 1] = configuredList(tier);
                tiers.add(resolveTier(items, tier, rawLists[tier - 1]));
            }
            current = new Snapshot(rawLists, List.copyOf(tiers));
            snapshot = current;
            return current;
        }
    }

    private static boolean matchesConfig(Snapshot current) {
        for (int tier = 1; tier <= TIER_COUNT; tier++) {
            if (!current.rawLists()[tier - 1].equals(configuredList(tier))) {
                return false;
            }
        }
        return true;
    }

    private static TierGear resolveTier(Registry<Item> items, int tier, String raw) {
        Map<EquipmentSlot, List<Item>> pools = emptyPools();
        List<String> skipped = new ArrayList<>();
        for (String entry : parseList(raw)) {
            Lookup lookup = lookup(items, entry);
            if (lookup.problem() != null) {
                skipped.add(lookup.id() + " (" + lookup.problem() + ")");
                continue;
            }
            pools.get(lookup.slot()).add(lookup.item());
        }

        boolean fallback = pools.values().stream().allMatch(List::isEmpty);
        if (fallback) {
            pools = emptyPools();
            for (String entry : parseList(defaultList(tier))) {
                Lookup lookup = lookup(items, entry);
                if (lookup.problem() == null) {
                    pools.get(lookup.slot()).add(lookup.item());
                }
            }
        }

        if (!skipped.isEmpty() && Config.COMMON.enableDebugLogging.get()) {
            LOGGER.warn("[ZombieApocalypse] tier{}Gear skipped {}", tier, String.join(", ", skipped));
        }

        Map<EquipmentSlot, List<Item>> frozen = new EnumMap<>(EquipmentSlot.class);
        pools.forEach((slot, pool) -> frozen.put(slot, List.copyOf(pool)));
        return new TierGear(tier, Collections.unmodifiableMap(frozen), List.copyOf(skipped), fallback);
    }

    private static Map<EquipmentSlot, List<Item>> emptyPools() {
        Map<EquipmentSlot, List<Item>> pools = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : GEAR_SLOTS) {
            pools.put(slot, new ArrayList<>());
        }
        return pools;
    }

    private static void checkTier(int tier) {
        if (tier < 1 || tier > TIER_COUNT) {
            throw invalidTier(tier);
        }
    }

    private static IllegalArgumentException invalidTier(int tier) {
        return new IllegalArgumentException("Gear tier must be 1-" + TIER_COUNT + ", got " + tier);
    }

    /** Formats a 0-1 chance with at most one decimal place, such as "8.5%" or "25%". */
    public static String formatChance(double chance) {
        return BigDecimal.valueOf(chance * 100.0)
                .setScale(1, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString() + "%";
    }

    /** Formats tier odds as "50%/20%/20%/10%". */
    public static String describeOdds(double[] odds) {
        String[] parts = new String[odds.length];
        for (int i = 0; i < odds.length; i++) {
            parts[i] = Math.round(odds[i] * 100.0) + "%";
        }
        return String.join("/", parts);
    }
}
