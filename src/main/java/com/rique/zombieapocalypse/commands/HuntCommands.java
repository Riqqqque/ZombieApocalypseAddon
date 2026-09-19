package com.rique.zombieapocalypse.commands;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;

import com.rique.zombieapocalypse.Config;
import com.rique.zombieapocalypse.ConfigLimits;
import com.rique.zombieapocalypse.DifficultyManager;

public final class HuntCommands {

    private HuntCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("zhunt")
                .executes(context -> showStatus(context.getSource()))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource())))
                .then(CommandUtil.admin(Commands.literal("on")
                        .executes(context -> setEnabled(context.getSource(), true))))
                .then(CommandUtil.admin(Commands.literal("off")
                        .executes(context -> setEnabled(context.getSource(), false))))
                .then(CommandUtil.toggleSetting("enabled", Config.COMMON.enableAnimalHunting::get,
                        value -> Config.set(Config.COMMON.enableAnimalHunting, value), "Animal hunting"))
                .then(CommandUtil.toggleSetting("babies", Config.COMMON.huntBabies::get,
                        value -> Config.set(Config.COMMON.huntBabies, value), "Hunt baby animals"))
                .then(CommandUtil.toggleSetting("always", Config.COMMON.alwaysHunting::get,
                        value -> Config.set(Config.COMMON.alwaysHunting, value), "Always hunting"))
                .then(CommandUtil.toggleSetting("berserker", Config.COMMON.berserkerHunting::get,
                        value -> Config.set(Config.COMMON.berserkerHunting, value), "Berserker hunting"))
                .then(CommandUtil.intSetting("startday", "day", 0, ConfigLimits.MAX_APOCALYPSE_DAY,
                        Config.COMMON.huntStartDay::get,
                        value -> Config.set(Config.COMMON.huntStartDay, value),
                        value -> value <= 0
                                ? "Hunting allowed from world start."
                                : "Hunting starts on day " + value,
                        0, 1, 5, 10, 20))
                .then(CommandUtil.intSetting("cooldown", "ticks", 0, 72000,
                        Config.COMMON.huntCooldownTicks::get,
                        value -> Config.set(Config.COMMON.huntCooldownTicks, value),
                        value -> "Hunt cooldown: " + CommandUtil.ticks(value),
                        0, 200, 600, 1200, 2400))
                .then(CommandUtil.intSetting("daycap", "kills", 0, 100000,
                        Config.COMMON.huntCapPerZombiePerDay::get,
                        value -> Config.set(Config.COMMON.huntCapPerZombiePerDay, value),
                        value -> value <= 0
                                ? "Per-zombie daily hunt cap removed."
                                : "Hunts per zombie per day: " + value,
                        0, 3, 10, 25, 50))
                .then(CommandUtil.intSetting("totalcap", "kills", 0, 100000,
                        Config.COMMON.huntCapPerLevelPerDay::get,
                        value -> Config.set(Config.COMMON.huntCapPerLevelPerDay, value),
                        value -> value <= 0
                                ? "Dimension-wide daily hunt cap removed."
                                : "Total hunts per dimension per day: " + value,
                        0, 10, 20, 50, 100))
                .then(CommandUtil.doubleSetting("followrange", "multiplier", 0.0, 64.0,
                        Config.COMMON.huntFollowDistanceFactor::get,
                        value -> Config.set(Config.COMMON.huntFollowDistanceFactor, value),
                        value -> "Hunt follow range: " + CommandUtil.multiplier(value) + " normal",
                        0.5, 1.0, 1.5, 2.0, 4.0))
                .then(CommandUtil.toggleSetting("eat", Config.COMMON.eatDroppedFood::get,
                        value -> Config.set(Config.COMMON.eatDroppedFood, value), "Eat dropped food"))
                .then(CommandUtil.intSetting("eatcooldown", "ticks", 0, 72000,
                        Config.COMMON.eatCooldownTicks::get,
                        value -> Config.set(Config.COMMON.eatCooldownTicks, value),
                        value -> "Eat cooldown: " + CommandUtil.ticks(value),
                        0, 20, 40, 100, 200))
                .then(CommandUtil.intSetting("recovery", "half-hearts", 0, 40,
                        Config.COMMON.healthPerNutrition::get,
                        value -> Config.set(Config.COMMON.healthPerNutrition, value),
                        value -> "Health restored per nutrition: " + value,
                        0, 1, 2, 4))
                .then(CommandUtil.intSetting("boostcap", "half-hearts", 0, 1024,
                        Config.COMMON.feedingMaxHealthBoost::get,
                        value -> Config.set(Config.COMMON.feedingMaxHealthBoost, value),
                        value -> "Feeding health cap: +" + value + " half-hearts",
                        0, 10, 20, 40, 100))
                .then(CommandUtil.intSetting("hardbonus", "half-hearts", 0, 1024,
                        Config.COMMON.feedingMaxHealthBoostHardBonus::get,
                        value -> Config.set(Config.COMMON.feedingMaxHealthBoostHardBonus, value),
                        value -> "Hard-mode bonus cap: +" + value + " half-hearts",
                        0, 10, 20, 40))
                .then(CommandUtil.toggleSetting("fleshresist", Config.COMMON.rottenFleshGivesResistance::get,
                        value -> Config.set(Config.COMMON.rottenFleshGivesResistance, value),
                        "Rotten flesh resistance"))
                .then(CommandUtil.intSetting("lockticks", "ticks", 0, 24000,
                        Config.COMMON.foodCombatLockTicks::get,
                        value -> Config.set(Config.COMMON.foodCombatLockTicks, value),
                        value -> "Food combat lock: " + CommandUtil.ticks(value),
                        0, 40, 100, 200))
                .then(CommandUtil.doubleSetting("lockdist", "blocks", 0.0, 64.0,
                        Config.COMMON.foodCombatLockDistance::get,
                        value -> Config.set(Config.COMMON.foodCombatLockDistance, value),
                        value -> "Food combat lock distance: " + value + " blocks",
                        0.0, 4.0, 8.0, 16.0))
                .then(CommandUtil.toggleSetting("persistent", Config.COMMON.persistentAfterEating::get,
                        value -> Config.set(Config.COMMON.persistentAfterEating, value),
                        "Persistence after eating"))
                .then(CommandUtil.toggleSetting("loot", Config.COMMON.fedZombiesDropExtraLoot::get,
                        value -> Config.set(Config.COMMON.fedZombiesDropExtraLoot, value), "Bonus loot"))
                .then(CommandUtil.doubleSetting("lootratio", "ratio", 0.1, 64.0,
                        Config.COMMON.extraLootHealthRatio::get,
                        value -> Config.set(Config.COMMON.extraLootHealthRatio, value),
                        value -> "Extra loot: one roll per " + value + "x base health gained",
                        0.5, 1.0, 2.0, 4.0))
                .then(CommandUtil.toggleSetting("leaders", Config.COMMON.fedZombiesBecomeLeaders::get,
                        value -> Config.set(Config.COMMON.fedZombiesBecomeLeaders, value),
                        "Fed zombies become leaders"))
                .then(CommandUtil.toggleSetting("zombifyhorses", Config.COMMON.zombifyHorses::get,
                        value -> Config.set(Config.COMMON.zombifyHorses, value), "Zombify horses"))
                .then(CommandUtil.toggleSetting("zombifytamed", Config.COMMON.zombifyTamedHorses::get,
                        value -> Config.set(Config.COMMON.zombifyTamedHorses, value),
                        "Zombify tamed horses"))
                .then(CommandUtil.toggleSetting("ridehorses", Config.COMMON.rideZombieHorses::get,
                        value -> Config.set(Config.COMMON.rideZombieHorses, value),
                        "Ride zombie horses"))
                .then(CommandUtil.toggleSetting("sparehorses", Config.COMMON.neverHuntZombieHorses::get,
                        value -> Config.set(Config.COMMON.neverHuntZombieHorses, value),
                        "Never hunt zombie horses")));
    }

    private static int setEnabled(CommandSourceStack source, boolean enabled) {
        Config.set(Config.COMMON.enableAnimalHunting, enabled);
        CommandUtil.feedback(source,
                enabled
                        ? "Animal hunting: ON. Zombies will hunt animals, eat dropped meat, and grow stronger."
                        : "Animal hunting: OFF. Existing hunting goals go idle; bonuses already gained persist.",
                true);
        return 1;
    }

    private static int showStatus(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        long day = DifficultyManager.getCurrentDay(level);
        boolean enabled = Config.COMMON.enableAnimalHunting.get();
        boolean scheduled = day >= Math.max(0, Config.COMMON.huntStartDay.get());

        StringBuilder status = new StringBuilder("Animal hunting settings:\n");
        status.append("Enabled: ").append(CommandUtil.onOff(enabled))
                .append(enabled && !scheduled
                        ? " (starts day " + Config.COMMON.huntStartDay.get() + ", current day " + day + ")"
                        : "")
                .append('\n');
        status.append("Mode: ").append(Config.COMMON.berserkerHunting.get() ? "BERSERKER"
                : Config.COMMON.alwaysHunting.get() ? "always hunting" : "hunt while hungry/growing")
                .append('\n');
        status.append("Limits: ").append(CommandUtil.ticks(Config.COMMON.huntCooldownTicks.get()))
                .append(" cooldown | ").append(Config.COMMON.huntCapPerZombiePerDay.get())
                .append(" kills/zombie/day | ").append(Config.COMMON.huntCapPerLevelPerDay.get())
                .append(" kills/dimension/day\n");
        status.append("Targets: babies ").append(CommandUtil.onOff(Config.COMMON.huntBabies.get()))
                .append(" | follow range ").append(CommandUtil.multiplier(Config.COMMON.huntFollowDistanceFactor.get()))
                .append(" | zombie horses ").append(Config.COMMON.neverHuntZombieHorses.get() ? "spared" : "huntable")
                .append('\n');
        status.append("Eating: ").append(CommandUtil.onOff(Config.COMMON.eatDroppedFood.get()))
                .append(" | +").append(Config.COMMON.healthPerNutrition.get())
                .append(" hp/nutrition | cap +").append(Config.COMMON.feedingMaxHealthBoost.get())
                .append(" (").append(Config.COMMON.feedingMaxHealthBoostHardBonus.get())
                .append(" extra on Hard)\n");
        status.append("Growth: persistent ").append(CommandUtil.onOff(Config.COMMON.persistentAfterEating.get()))
                .append(" | extra loot ").append(CommandUtil.onOff(Config.COMMON.fedZombiesDropExtraLoot.get()))
                .append(" | leaders ").append(CommandUtil.onOff(Config.COMMON.fedZombiesBecomeLeaders.get()))
                .append('\n');
        status.append("Horses: zombify ").append(CommandUtil.onOff(Config.COMMON.zombifyHorses.get()))
                .append(" | tamed too ").append(CommandUtil.onOff(Config.COMMON.zombifyTamedHorses.get()))
                .append(" | riding ").append(CommandUtil.onOff(Config.COMMON.rideZombieHorses.get()));
        CommandUtil.feedback(source, status.toString(), false);
        return 1;
    }
}
