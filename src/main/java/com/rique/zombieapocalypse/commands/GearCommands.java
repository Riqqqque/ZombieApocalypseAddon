package com.rique.zombieapocalypse.commands;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.DifficultyManager;
import com.rique.zombieapocalypse.ZombieGear;

public final class GearCommands {

    private static final String[] TIERS = { "1", "2", "3", "4" };

    private GearCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zgear")
                .executes(context -> showStatus(context.getSource()))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource())))
                .then(Commands.literal("list")
                        .executes(context -> showStatus(context.getSource()))
                        .then(tierArgument()
                                .executes(context -> showTier(context.getSource(), tier(context)))))
                .then(CommandUtil.admin(Commands.literal("add")
                        .then(tierArgument()
                                .executes(context -> addHeldItem(context.getSource(), tier(context)))
                                .then(Commands.argument("item", ResourceLocationArgument.id())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                                items(context.getSource()).keySet(), builder))
                                        .executes(context -> addItem(
                                                context.getSource(),
                                                tier(context),
                                                ResourceLocationArgument.getId(context, "item").toString(),
                                                false))))))
                .then(CommandUtil.admin(Commands.literal("remove")
                        .then(tierArgument()
                                .then(Commands.argument("item", ResourceLocationArgument.id())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                listedIds(context), builder))
                                        .executes(context -> removeItem(
                                                context.getSource(),
                                                tier(context),
                                                ResourceLocationArgument.getId(context, "item").toString()))))))
                .then(CommandUtil.admin(Commands.literal("clear")
                        .then(tierArgument()
                                .executes(context -> clearTier(context.getSource(), tier(context))))))
                .then(CommandUtil.admin(Commands.literal("reset")
                        .then(Commands.literal("all")
                                .executes(context -> resetAll(context.getSource())))
                        .then(tierArgument()
                                .executes(context -> resetTier(context.getSource(), tier(context))))))
                .then(CommandUtil.doubleSetting("dropchance", "chance", 0.0, 1.0,
                        Config.COMMON.gearDropChance::get,
                        value -> Config.set(Config.COMMON.gearDropChance, value),
                        value -> "Gear drop chance: " + ZombieGear.formatChance(value) + " per item when a player kills the zombie",
                        0.0, 0.01, 0.05, ZombieGear.DEFAULT_DROP_CHANCE, 0.25, 0.5, 1.0))
                .then(CommandUtil.admin(Commands.literal("preview")
                        .then(tierArgument()
                                .executes(context -> preview(context.getSource(), tier(context)))))));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Integer> tierArgument() {
        return Commands.argument("tier", IntegerArgumentType.integer(1, ZombieGear.TIER_COUNT))
                .suggests(CommandSuggestions.fixed(TIERS));
    }

    private static int tier(CommandContext<CommandSourceStack> context) {
        return IntegerArgumentType.getInteger(context, "tier");
    }

    private static Set<String> listedIds(CommandContext<CommandSourceStack> context) {
        Set<String> ids = new LinkedHashSet<>();
        try {
            for (String entry : ZombieGear.parseList(ZombieGear.configuredList(tier(context)))) {
                String id = ZombieGear.normalizeId(entry);
                if (id != null) {
                    ids.add(id);
                }
            }
        } catch (IllegalArgumentException ignored) {
            // The tier argument is not parsed yet.
        }
        return ids;
    }

    private static int showStatus(CommandSourceStack source) {
        double factor = DifficultyManager.getScalingFactor(source.getServer().overworld());
        StringBuilder status = new StringBuilder("Zombie gear:\n");
        status.append("Difficulty scaling: ")
                .append(CommandUtil.onOff(Config.COMMON.enableDifficultyScaling.get()))
                .append(" (").append(CommandUtil.percent(factor)).append(" strength)\n");
        status.append("Chance today: armor ")
                .append(CommandUtil.percent(Config.COMMON.maxArmorChance.get() * factor))
                .append(" | weapon ")
                .append(CommandUtil.percent(Config.COMMON.maxWeaponChance.get() * factor))
                .append(" | drop ").append(ZombieGear.formatChance(ZombieGear.dropChance())).append(" per item\n");
        status.append("Tier odds today: armor ")
                .append(ZombieGear.describeOdds(ZombieGear.armorTierOdds(factor)))
                .append(" | weapon ")
                .append(ZombieGear.describeOdds(ZombieGear.weaponTierOdds(factor)))
                .append('\n');
        Registry<Item> items = items(source);
        for (int tier = 1; tier <= ZombieGear.TIER_COUNT; tier++) {
            status.append("Tier ").append(tier).append(": ").append(tierSummary(ZombieGear.tier(items, tier))).append('\n');
        }
        status.append("/za gear list <tier> - see every item in a tier\n");
        status.append("/za gear add <tier> [item] - add an item, or the one in your hand\n");
        status.append("/za gear preview <tier> - spawn a zombie wearing that tier");
        CommandUtil.feedback(source, status.toString(), false);
        return 1;
    }

    private static String tierSummary(ZombieGear.TierGear gear) {
        String summary = gear.vanillaFallback()
                ? "vanilla " + ZombieGear.vanillaName(gear.tier())
                : CommandUtil.count(gear.armorCount(), "armor piece") + ", "
                        + CommandUtil.count(gear.weaponCount(), "weapon");
        if (!gear.skipped().isEmpty()) {
            summary += " (" + gear.skipped().size() + " skipped)";
        }
        return summary;
    }

    private static int showTier(CommandSourceStack source, int tier) {
        Registry<Item> items = items(source);
        ZombieGear.TierGear gear = ZombieGear.tier(items, tier);
        StringBuilder details = new StringBuilder("Gear tier " + tier + ":\n");
        details.append("Source: ").append(gear.vanillaFallback()
                ? "vanilla gear (the list is empty or nothing in it is installed)"
                : "custom list").append('\n');
        for (EquipmentSlot slot : ZombieGear.GEAR_SLOTS) {
            details.append(slotName(slot)).append(": ").append(describePool(items, gear.pool(slot))).append('\n');
        }
        if (!gear.skipped().isEmpty()) {
            details.append("Skipped: ").append(String.join(", ", gear.skipped())).append('\n');
        }
        details.append("Empty slots stay empty when a zombie rolls this tier.");
        CommandUtil.feedback(source, details.toString(), false);
        return 1;
    }

    private static String describePool(Registry<Item> items, List<Item> pool) {
        if (pool.isEmpty()) {
            return "none";
        }
        Map<Item, Integer> counts = new LinkedHashMap<>();
        for (Item item : pool) {
            counts.merge(item, 1, Integer::sum);
        }
        StringBuilder text = new StringBuilder();
        counts.forEach((item, count) -> {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(items.getKey(item));
            if (count > 1) {
                text.append(" x").append(count);
            }
        });
        return text.toString();
    }

    private static int addHeldItem(CommandSourceStack source, int tier) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            CommandUtil.failure(source, "Name the item from the console: /za gear add " + tier + " <item>");
            return 0;
        }
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            CommandUtil.failure(source,
                    "Hold the armor or weapon you want to add in your main hand, or name it: /za gear add "
                            + tier + " <item>");
            return 0;
        }
        return addItem(source, tier, String.valueOf(items(source).getKey(held.getItem())), held.isEnchanted());
    }

    private static int addItem(CommandSourceStack source, int tier, String input, boolean enchanted) {
        Registry<Item> items = items(source);
        ZombieGear.Lookup lookup = ZombieGear.lookup(items, input);
        if (lookup.problem() != null) {
            CommandUtil.failure(source, lookup.id() + " can't be used as zombie gear: " + lookup.problem() + '.');
            return 0;
        }

        boolean wasFallback = ZombieGear.tier(items, tier).vanillaFallback();
        String updated = ZombieGear.withAdded(ZombieGear.configuredList(tier), lookup.id());
        ZombieGear.setConfiguredList(tier, updated);
        int copies = ZombieGear.countEntries(updated, lookup.id());
        ZombieGear.TierGear gear = ZombieGear.tier(items, tier);

        StringBuilder message = new StringBuilder("Added ")
                .append(lookup.id()).append(" to tier ").append(tier)
                .append(" (").append(slotName(lookup.slot()).toLowerCase(java.util.Locale.ROOT)).append(").");
        if (copies > 1) {
            message.append(" It is now listed ").append(copies).append(" times, so it is picked more often.");
        }
        message.append("\nTier ").append(tier).append(" now has ")
                .append(CommandUtil.count(gear.armorCount(), "armor piece")).append(" and ")
                .append(CommandUtil.count(gear.weaponCount(), "weapon")).append('.');
        if (wasFallback) {
            message.append("\nThis tier was using vanilla gear. It now uses only its own list; /za gear reset ")
                    .append(tier).append(" brings the vanilla items back.");
        }
        if (enchanted) {
            message.append("\nOnly the item type is saved. Enchantments and other item data are not copied.");
        }
        CommandUtil.feedback(source, message.toString(), true);
        return 1;
    }

    private static int removeItem(CommandSourceStack source, int tier, String input) {
        String id = ZombieGear.normalizeId(input);
        String current = ZombieGear.configuredList(tier);
        int copies = id == null ? 0 : ZombieGear.countEntries(current, id);
        if (copies == 0) {
            CommandUtil.failure(source, input + " is not in tier " + tier + ". /za gear list " + tier + " shows its items.");
            return 0;
        }

        String updated = ZombieGear.withRemoved(current, id);
        ZombieGear.setConfiguredList(tier, updated);
        StringBuilder message = new StringBuilder("Removed ").append(id).append(" from tier ").append(tier);
        if (copies > 1) {
            message.append(" (").append(copies).append(" copies)");
        }
        message.append('.');
        if (ZombieGear.parseList(updated).isEmpty()) {
            message.append("\nThe list is now empty, so tier ").append(tier).append(" uses vanilla ")
                    .append(ZombieGear.vanillaName(tier)).append(" again.");
        }
        CommandUtil.feedback(source, message.toString(), true);
        return 1;
    }

    private static int clearTier(CommandSourceStack source, int tier) {
        ZombieGear.setConfiguredList(tier, "");
        CommandUtil.feedback(source,
                "Tier " + tier + " list cleared. It uses vanilla " + ZombieGear.vanillaName(tier)
                        + " until you add items with /za gear add " + tier + '.',
                true);
        return 1;
    }

    private static int resetTier(CommandSourceStack source, int tier) {
        ZombieGear.setConfiguredList(tier, ZombieGear.defaultList(tier));
        CommandUtil.feedback(source,
                "Tier " + tier + " restored to vanilla " + ZombieGear.vanillaName(tier) + '.', true);
        return 1;
    }

    private static int resetAll(CommandSourceStack source) {
        Config.edit(() -> {
            for (int tier = 1; tier <= ZombieGear.TIER_COUNT; tier++) {
                ZombieGear.setConfiguredList(tier, ZombieGear.defaultList(tier));
            }
        });
        CommandUtil.feedback(source, "All gear tiers restored to their vanilla items.", true);
        return 1;
    }

    private static int preview(CommandSourceStack source, int tier) {
        ServerLevel level = source.getLevel();
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            CommandUtil.failure(source, "Zombies despawn on Peaceful. Raise the difficulty to preview gear.");
            return 0;
        }
        Zombie zombie = EntityType.ZOMBIE.create(level);
        if (zombie == null) {
            CommandUtil.failure(source, "A zombie could not be created here.");
            return 0;
        }

        Vec3 origin = source.getPosition();
        float yaw = source.getRotation().y;
        double radians = Math.toRadians(yaw);
        zombie.moveTo(origin.x - Math.sin(radians) * 2.0, origin.y, origin.z + Math.cos(radians) * 2.0,
                yaw + 180.0F, 0.0F);
        if (!level.noCollision(zombie)) {
            zombie.moveTo(origin.x, origin.y, origin.z, yaw + 180.0F, 0.0F);
        }
        if (!level.addFreshEntity(zombie)) {
            CommandUtil.failure(source, "The preview zombie could not be spawned here.");
            return 0;
        }

        int equipped = ZombieGear.equipFullTier(zombie, tier, level.getRandom());
        CommandUtil.feedback(source,
                "Spawned a tier " + tier + " preview zombie with " + equipped + " of "
                        + ZombieGear.GEAR_SLOTS.size() + " gear slots filled.\n"
                        + "It is a normal hostile zombie. Real spawns roll each slot separately, so most carry less.",
                true);
        return 1;
    }

    private static Registry<Item> items(CommandSourceStack source) {
        return source.registryAccess().registryOrThrow(Registries.ITEM);
    }

    private static String slotName(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> "Head";
            case CHEST -> "Chest";
            case LEGS -> "Legs";
            case FEET -> "Feet";
            case MAINHAND -> "Hand";
            default -> slot.getName();
        };
    }
}
