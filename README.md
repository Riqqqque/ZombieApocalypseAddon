# Zombie Apocalypse Addon

[![Build](https://github.com/Riqqqque/ZombieApocalypseAddon/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/Riqqqque/ZombieApocalypseAddon/actions/workflows/build.yml)
[![Modrinth downloads](https://img.shields.io/modrinth/dt/gH2XNhDh?logo=modrinth&label=Modrinth)](https://modrinth.com/mod/apocolypseaddon)
[![CurseForge downloads](https://img.shields.io/curseforge/dt/1497600?logo=curseforge&label=CurseForge)](https://www.curseforge.com/minecraft/mc-mods/zombieapocalypseaddon)

Turn Minecraft into a configurable zombie survival world where daytime is not automatically safe.

The mod adds custom zombie waves, hordes, blood moons, day-based difficulty, optional siege behavior, kill milestones, and broad zombie-mod compatibility. It is designed for both simple survival servers and heavily tuned apocalypse packs.

[Download on Modrinth](https://modrinth.com/mod/apocolypseaddon/versions) | [Download on CurseForge](https://www.curseforge.com/minecraft/mc-mods/zombieapocalypseaddon/files) | [Read the wiki](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki) | [Report a problem](https://github.com/Riqqqque/ZombieApocalypseAddon/issues/new/choose)

## Quick Facts

- **Multiplayer:** install the mod on the server only. Players can join without it.
- **Singleplayer:** install it on the client because singleplayer runs a local server.
- **Permissions:** anyone can read dashboards, help, stats, status pages, and current setting values. Changes and admin actions require permission level 2.
- **Minecraft targets:** NeoForge 1.21.1, NeoForge 1.20.1, and Forge 1.20.1.
- **Config:** `config/zombieapocalypseaddon-common.toml`.
- **Safe defaults:** block breaking, block placing, zombie towering, and animal hunting are disabled.

## Choose the Correct File

| Minecraft | Loader | Mod version | File name |
|---|---|---:|---|
| 1.21.1 | NeoForge | 2.8.0 | `zombieapocalypseaddon-2.8.0.jar` |
| 1.20.1 | NeoForge | 1.11.0 | `zombieapocalypseaddon-neoforge-1.20.1-1.11.0.jar` |
| 1.20.1 | Forge | 1.11.0 | `zombieapocalypseaddon-forge-1.20.1-1.11.0.jar` |

Install only the file for your exact Minecraft version and loader. Forge and NeoForge files are not interchangeable.

## 60-Second Setup

1. Put the correct jar in the server `mods` folder.
2. Start the server and join as an operator.
3. Run `/za` to see the main dashboard.
4. Run `/za preset standard` for the recommended setup.
5. Use `/za help` whenever you need a short command topic.

That is enough for a normal server. You do not need to edit advanced attributes or compatibility settings.

## Common Tweaks

Most questions are one command away:

| I want to... | Run this |
|---|---|
| Keep zombies out of my torch-lit base | Nothing - lit areas are protected by default. `/za spawn maxlight -1` disables the protection if you ever want it off. |
| Stop custom spawns during the day | `/za spawn daytime off` (night waves and blood moons still work) |
| Have hordes arrive at dawn instead of nightfall | `/za events dusk off` (hordes arrive at dusk by default) |
| Make the first day dangerous too | `/za spawn daylightstart 0` (day 0 is safe in daylight by default) |
| Skip time without resetting the apocalypse day | `/time add 12000`, or `/za day set <day>`. Vanilla `/time set` resets the day counter to 0. |
| Make zombies jump in strength after every horde | `/za scaling steps 10` with `/za events interval 10` (strength rises on days 11, 21, 31...) |
| Get bigger waves as the days go on | `/za scaling extrazombies 4` (up to 4 extra zombies per wave at full strength, hordes included) |
| Let zombies break or build only during hordes and blood moons | `/za breaking events on` or `/za placing events on`, then set a high `startday` for when they may do it all the time. See [Siege Nights](#siege-nights). |
| Remove baby zombies | `/za spawn babychance 0` |
| Make it easier overall | `/za preset casual` or `/za spawn chance 0.25` |
| Make it harder overall | `/za preset hardcore` |
| Let zombies break stone and ores, not just wood | `/za breaking on` then `/za breaking toolblocks on` and `/za breaking hardness 50` |
| Let zombies break chests and machines too | `/za breaking containers on` (think twice - they really will) |
| Have zombies hunt animals and grow stronger | `/za hunt on` |
| Give zombies armor or weapons from another mod | Hold the item and run `/za gear add <tier>` (tier 1 is common, tier 4 is rare). `/za gear preview <tier>` shows the result. |
| Change how often zombie gear drops | `/za gear dropchance 0.25` (default `0.085`, the vanilla rate) |
| Check what is currently running | `/za` for the dashboard, `/za spawn` for spawn settings |
| Keep my settings for every new world | Nothing - they are stored once in the common config and apply to all worlds automatically. |

Every `on` command keeps your other settings. If one value would stop the feature from working, such as a 0% chance, `on` fixes just that value and tells you what it changed. `/za config reset <section>` puts a whole section back to its defaults when you want that. Details are in the sections below.

## Gameplay Presets

Presets replace the main spawning, event, and basic scaling settings in one step. They also restore a safe spawn range (30), minimum distance (12), attempt count (10), and the open-sky rule, so an old impossible distance setup cannot break a preset.

| Command | Best for |
|---|---|
| `/za preset casual` | Casual groups and new worlds: fewer zombies, a 10-day daylight grace, no baby zombies. |
| `/za preset standard` | Most survival servers. A fresh install already uses it. |
| `/za preset hardcore` | Tested challenge worlds: faster waves, bigger events, quicker difficulty growth. |

| | Casual | Standard (fresh install) | Hardcore |
|---|---|---|---|
| Wave checks | every 9 s, 25% chance | every 6 s, 50% chance | every 4 s, 75% chance |
| Zombies per wave / cap near each player | 1 / 30 | 2 / 50 | 3 / 80 |
| Daytime waves start on | day 10 | day 1 | day 0 |
| Lit areas | any light blocks waves | any light blocks waves | light is ignored |
| Night spawn chance | x1.25 | x1.5 | x2.0 |
| Baby zombies | 0% | 5% | 8% |
| Hordes (at dusk) | every 7 days, 25% chance, 6 min | every 5 days, 50% chance, 10 min | every 3 days, 75% chance, 12 min |
| Horde strength | x2.0 chance, 3 per wave | x3.0 chance, 5 per wave | x4.0 chance, 8 per wave |
| Blood moons | 5% of nights, x3.0, 2 per wave | 15% of nights, x5.0, 4 per wave | 25% of nights, x6.0, 6 per wave |
| Difficulty scaling | day 10 to 75 | day 3 to 50 | day 1 to 40 |
| Full-strength bonuses | +10% speed, +6 health, 15% armor, 10% weapon | +20% speed, +10 health, 30% armor, 20% weapon | +30% speed, +16 health, 45% armor, 35% weapon |

All three keep block breaking, block placing, towering, and animal hunting as you set them, and leave advanced attribute values alone. A preset replaces the settings in this table, so apply it first and tune afterward.

Use `/za status` afterward to confirm what is active.

## Default Experience

A fresh install uses the Standard preset:

- **First day:** daytime is safe on day 0 so players can gather gear. From day 1, waves can come day and night.
- **Waves:** every 6 seconds each survival player has a 50% chance of a wave of 2 zombies at least 12 blocks away within a 30-block range, up to 50 zombies around that player. Nights raise the chance by half.
- **Lit bases are safe:** custom waves need complete block darkness, so torches and lanterns protect an area.
- **No sunlight burning:** zombies walk around in daylight.
- **Hordes:** every 5th day there is a 50% chance of a horde at dusk. It lasts about the whole night, checks for waves every second at three times the chance, and sends 5 zombies per wave.
- **Blood moons:** 15% of nights. Five times the wave chance and 4 zombies per wave until dawn.
- **Getting harder:** from day 3 to day 50 zombies gain up to 20% speed and 10 health (5 hearts), and up to 30% wear armor and 20% carry weapons, with better tiers later on.
- **Variants:** husks 15%, drowned 10%, zombie villagers 5%, baby zombies 5%. Deserts favor husks, water favors drowned, and mushroom fields are safe.
- **After a death:** spawns near that player are halved for 30 seconds.
- **Extras:** kill tracking, milestones, spawn sounds and particles, and bonus drops are on.
- **Off until you turn them on:** block breaking, block placing, towering, animal hunting, and Nether and End spawning.

## Main Commands

Start with these:

| Command | Purpose |
|---|---|
| `/za` | Short dashboard for the current day, waves, events, scaling, and optional siege features. |
| `/za preset <casual\|standard\|hardcore>` | Apply a safe gameplay preset. |
| `/za config` | Show the config path, the beginner sections, and how to edit safely. |
| `/za config reset <section\|all>` | Put one config section, or the whole config, back to its defaults. |
| `/za help` or `/zhelp` | Show quick help. |
| `/za help <topic>` | Show focused help for one system. |
| `/za spawn` | Show the important custom-spawn settings. |
| `/za spawn daytime <on\|off>` | Use `off` for permanent night-only custom waves. |
| `/za events` | Show current horde and blood moon status. |
| `/za events dusk <on\|off>` | Hordes arrive at dusk (default) or, with `off`, at dawn. |
| `/za scaling` | Show current day-based difficulty progress. |
| `/za gear` | Show the zombie gear tiers, today's tier odds, and the drop chance. |

Help topics are `start`, `spawning`, `events`, `difficulty`, `gear`, `bases`, `hunt`, `admin`, `advanced`, and `all`.

### Command Families

| Command | Controls |
|---|---|
| `/za spawn` | Custom waves, variants, dimensions, effects, and main system toggles. |
| `/za events` and `/za bloodmoon` | Horde and blood moon events. |
| `/za day` and `/za scaling` | World day and difficulty progression. |
| `/za gear` | Armor and weapon tiers for scaled zombies, including modded items, and the gear drop chance. |
| `/za breaking` | Optional zombie block breaking. |
| `/za placing` | Optional limited bridges and steps. |
| `/za towering` | Optional moving zombie stacks for climbing defenses. |
| `/za hunt` | Optional animal hunting, meat eating, and zombie growth. |
| `/za stats` | Kill totals, cooldowns, and milestone reset. |
| `/za compatibility` | Mixed-mod compatibility safeguards. |
| `/za attributes` | Advanced attribute tuning. |
| `/za burn`, `/za kill`, `/za cleanup` | Sunlight, cleanup, and safe removal utilities. |

Press Tab after `/za` to see every command family. Tab completion suggests only the clear `on` and `off` states, plus current and common numeric values, attribute keys, online players, and registered block and item IDs. Existing `true` and `false` inputs still work in old scripts. Running a setting without a value shows its current value. The original `/z...` commands remain supported.

Feature `on` commands (`/za spawn on`, `/za events on`, `/za bloodmoon on`, `/za scaling on`, `/za attributes on`, `/za breaking on`, `/za placing on`, `/za towering on`, `/za hunt on`, `/za compatibility on`, and the `enabled on` forms) only turn the feature on. They also turn on a switch the feature depends on, such as custom waves for hordes, and fix a value that would leave the feature doing nothing, such as a 0% chance or a missing placing block. They report every extra change, and everything else you tuned stays as it is. Features with a start day wait for it; `dayone` turns the feature on and starts it today.

The [complete command reference](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Commands) includes every subcommand and range.

## Core Systems

### Custom Zombie Waves

The mod performs bounded spawn checks around living survival players. Administrators can control wave chance, timing, size, nearby cap, distance, position attempts, permanent daytime enablement, a temporary daylight start day, open-sky rules, and block-light protection. Turning off the open-sky rule also searches near the player's height for valid caves and covered spaces instead of only choosing the surface.

Use `/za spawn daytime off` for permanent night-only custom spawning. Normal night waves, blood moons, and dusk hordes (the default) continue; `/za events start` must be used at night. If you switched hordes to dawn with `/za events dusk off`, night-only mode pauses them. The Nether, End, and other fixed-time dimensions remain controlled by their own dimension toggles. Use `/za spawn daylightstart <day>` only when daytime waves should begin after a temporary grace period.

Use `/za spawn status all` only when you need every related toggle. The normal `/za spawn` output stays short.

### Light Protection

`maxBlockLightForSpawning` controls whether torches and similar blocks protect an area from the mod's custom waves:

- `0` (default): custom spawns require complete block darkness, so lit bases are safe.
- `7`: brighter areas block custom spawns, but dim spots still allow them.
- `-1`: ignore block light entirely, so waves can spawn anywhere.

This setting checks block light, not sunlight. It does not disable daytime waves by itself; use `/za spawn daytime off` for that.

### Hordes and Blood Moons

Hordes are scheduled high-pressure events. Blood moons are random night events. Each system has separate chance, timing, wave, and multiplier controls available in the config and through `/za events` or `/za bloodmoon`. Administrators can also start them manually. Scheduled hordes roll at dusk by default, so the horde arrives with nightfall and lasts about the night; `/za events dusk off` moves the roll to dawn. Events require custom waves; turning custom waves off cancels active or queued spawn pressure. Night-only mode keeps blood moons and nighttime manual hordes available, but pauses scheduled dawn hordes and blocks horde waves during daytime; dusk-start hordes still run in night-only mode. If both events overlap, their multipliers stack and the larger configured event wave size wins.

### Siege Nights

Hordes can act as siege nights that make the world harder each time. This setup sends a horde at nightfall every 10th day, lets zombies break and build only during those nights, makes them stronger and more numerous the morning after each one, and from day 50 lets them break and build all the time:

```mcfunction
/za events on
/za events interval 10
/za events chance 1
/za events dusk on
/za events duration 10
/za scaling steps 10
/za scaling extrazombies 4
/za breaking on
/za breaking startday 50
/za breaking events on
/za placing on
/za placing startday 50
/za placing events on
```

`on` commands keep your other settings, so the order does not matter. Horde duration is in minutes of play, and 10 lasts from dusk to about dawn. Blood moons count as event nights too, so zombies also break and build during them; use `/za bloodmoon off` if only the scheduled sieges should allow it. `/za towering events on` works the same way for zombie stacks.

### Difficulty Progression

Basic scaling can increase health, speed, armor, and weapon chances as the world gets older. `/za scaling` now exposes the start day, full-strength day, and each basic maximum with range-aware tab completion. Strength climbs a little every day by default; `/za scaling steps <days>` makes it rise in jumps instead, on the morning after every step day. `/za scaling extrazombies <count>` adds up to that many zombies to every custom wave, including horde and blood moon waves, as strength rises. The advanced attribute system can separately tune health, attack damage, movement speed, armor, follow range, and knockback resistance; `/za attributes get <key>` reports the exact allowed range for that key.

Most servers should use a preset or the basic `[scaling]` section and leave `[attributes]` unchanged.

### Zombie Gear

Scaled zombies draw armor and weapons from four tiers. Tier 1 is the most common on early days, and later days shift the odds toward tiers 3 and 4. Out of the box the tiers hold vanilla leather, chainmail, iron, and diamond gear, so nothing changes until you edit them. Items from any mod can join a tier:

| Command | Purpose |
|---|---|
| `/za gear` | Show every tier, today's tier odds, and the drop chance. |
| `/za gear list <tier>` | Show every item in a tier by slot, plus anything skipped. |
| `/za gear add <tier>` | Add the item in your main hand. |
| `/za gear add <tier> <item>` | Add an item by ID, with tab completion. |
| `/za gear remove <tier> <item>` | Remove an item from a tier. |
| `/za gear clear <tier>` | Empty a tier so you can build it from scratch. |
| `/za gear reset <tier\|all>` | Restore the vanilla items. |
| `/za gear dropchance <0.0-1.0>` | Chance each piece drops when a player kills the zombie. Default `0.085`, the vanilla rate. |
| `/za gear preview <tier>` | Spawn a zombie wearing one item per slot from that tier. |

Each item's slot is detected automatically: helmets go on the head, boots on the feet, and swords, axes, or other items in the main hand. Shields and offhand items are never used. Listing an item twice makes it twice as likely. IDs from mods that are not installed are skipped, and a tier with nothing usable falls back to its vanilla gear. If a tier has no item for a slot, zombies that roll that tier leave the slot empty. Only the item type is saved, so enchantments on a held item are not copied. The same lists are `tier1Gear` to `tier4Gear` in `[scaling]`.

Zombies can still spawn with vanilla's own random armor. After `/za compatibility equipment off`, the addon's gear replaces it when both use the same slot. That also applies to modded zombies that spawn with their own gear.

### Optional Base Pressure

All three systems are disabled by default:

- **Block breaking:** bounded attempts with hardness, safety, target, obstacle, drop, and `mobGriefing` controls.
- **Block placing:** limited solid blocks for one-block steps and gaps, with placement limits and protection hooks.
- **Towering:** crowded zombies form real moving passenger stacks when a raised target or genuinely blocked route makes climbing useful. Ordinary ground combat and normal player jumps are ignored. The default smart limit follows the target's block Y level plus one, then moves riders one at a time to collision-free floor positions when the target stays reachable on the ground. No blocks are changed.

Each feature has a start day, live commands, conservative defaults, and protection checks. `events on` (for example `/za breaking events on`) also lets a feature run during hordes and blood moons before its start day. `on` keeps your settings and waits for the start day; `dayone` turns the feature on and starts it today.

### Optional Animal Hunting

Disabled by default. Enable it with `/za hunt on` or `enableAnimalHunting` in `[animalhunting]`. Zombies hunt animals for food, then seek dropped meat from the `zombieapocalypseaddon:zombie_food` item tag to heal. Surplus nutrition becomes permanent bonus max health up to a configurable cap (higher on Hard). Fed zombies can persist, drop extra loot rolls scaled by the health they gained, and promote to leaders that call reinforcements and break doors.

Hunts are limited by a per-zombie cooldown and per-zombie and per-dimension daily kill caps. `alwaysHunting` removes the hunger motivation; `berserkerHunting` also ignores cooldowns and caps. Horses killed by zombies can rise as zombie horses keeping their name, owner, saddle, and gear (always on Hard, 50% on Normal), and idle zombies can seek out and ride zombie horses. Tamed horses are spared unless `zombifyTamedHorses` is on. Pack makers can force-include or exclude targets with the `zombie_hunt_target` and `never_zombie_hunt_target` entity tags or the `additionalHuntTargets`/`excludedHuntTargets` config lists.

Useful tower controls:

| Command | Purpose |
|---|---|
| `/za towering unlimited` | Turn on intentionally unrestricted towers: no count cap, no height cap, and no automatic collapse. Use carefully. |
| `/za towering stacksize <count>` | Set a count cap. `0` means no count cap and `1` prevents a stack. |
| `/za towering maxperplayer <count>` | Limit separate loaded towers targeting one player. `0` means unlimited. |
| `/za towering dynamic <on/off>` | Make the height limit follow the target's current block Y level. |
| `/za towering offset <blocks>` | Set extra block levels above the target in dynamic mode. Default is `1`. |
| `/za towering smartdismount <on/off>` | Gradually move riders to safe ground when the target remains reachable. |
| `/za towering jumping <on/off>` | Enable optional top-zombie jump attacks. This is off in the normal preset. |
| `/za towering pace <ticks>` | Set the delay between tower growth, safe dismounts, and jump attacks. |

The normal `/za towering on` preset has no fixed zombie-count cap because dynamic height already stops the stack at the useful level. Every rider position is checked using the correct Minecraft-version attachment rules before joining, and unsafe dismount spots are rejected instead of placing zombies inside blocks. `/za towering stacksize 0` removes only the count cap; `/za towering unlimited` also removes the height cap and smart collapse. Extremely tall passenger chains can hurt performance and should be enabled deliberately. The older `/za towering jumpcooldown` name remains an alias for `pace`.

### Variants, Biomes, and Dimensions

Custom waves can contain Zombies, Husks, Drowned, and Zombie Villagers. Biomes can influence variant chances, mushroom fields can remain safe even when biome variant weighting is disabled, and custom Nether or End spawning can be enabled separately. Night boost only applies in dimensions with a real day/night cycle, so fixed-time dimensions do not receive a permanent boost.

### Stats, Milestones, and Drops

The mod tracks zombie kills and awards milestones at 250, 1,000, and 3,000 kills. Optional bonus drops include bones, string, gunpowder, ender pearls, and phantom membranes. Bonus drops respect the vanilla `doMobLoot` gamerule.

## Mod Compatibility

Modded `Zombie` subclasses are recognized automatically. Generic entity tags and exact include/exclude lists support unusual infected mobs without hard dependencies.

Compatibility safeguards can:

- let external spawn-control mods reject custom spawns
- avoid duplicate AI or difficulty systems
- preserve modded weapons and armor
- respect per-zombie door-breaking permission
- expose modded zombies to caps, cleanup, stats, drops, and compatible scaling

Built-in handling covers Zombie Awareness, Zombie Horse Spawn, Mo' Zombies Wave, More Zombie Villagers, Zombies Reworked, Zombie Villagers From Spawner, Zombie Variants, Zombies+, Zombie Proof Doors, Undead Nights, The Hordes, Improved Mobs, In Control!, Bad Mobs, and Giant Spawn.

Run `/za compatibility status` to inspect the live safeguards. See the [compatibility guide](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Compatibility) for exact behavior.

## Config Guide

The generated config starts with a **START HERE** section and explains every value beside the setting.

Beginner sections:

- `[dayspawning]`
- `[variants]`
- `[horde]`
- `[bloodmoon]`
- `[scaling]`

Usually leave these sections unchanged:

- `[compatibility]`
- `[attributes]`
- `[attributes.variants]`
- `[attributes.contexts]`

Important rules:

- In-game commands save right away and never reset other settings. Prefer them over editing the file.
- File edits are picked up while the game runs, but editing with the server stopped is safest. If the loader ever resets settings while reading a half-saved file, the mod puts them back and tells online operators.
- `/za config reset <section>` restores defaults on purpose. The loader keeps older copies of the file as `.bak` files in the config folder.
- `20` ticks equals one second.
- Chances use `0.0` to `1.0`; for example, `0.25` means 25%.
- Change one performance-heavy setting at a time and test with multiple players.
- Keep a world backup before enabling destructive behavior.

See the [config guide](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Config-Guide) for examples, the [config reference](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Config-Reference) for every setting with its default and range, and the [FAQ](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/FAQ) for common questions.

## Server Use

- **Singleplayer and private servers for you and your friends:** go ahead, no permission needed.
- **Anything bigger** - public servers, community or network servers, or servers with donations, ranks, or paid perks: ask first. Send me a message on CurseForge or Modrinth with what the server is and how players get the mod.
- **Always:** players download the mod from its official Modrinth or CurseForge page. Do not re-host the jar or publish modified builds. Config changes, datapacks, and separate add-ons that depend on this mod without copying its code or assets are fine.

## Safe Removal

Before temporarily or permanently removing the mod, run:

```mcfunction
/zcleanup uninstall
```

This removes loaded zombie-class mobs, resets event state, and disables the mod's active gameplay systems. Stop the server after the command, then remove the jar. Existing entities cannot keep running this mod's code after the mod is absent, but mobs already changed by another mod or vanilla data may keep their own saved attributes.

See [Safe Uninstall](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Safe-Uninstall) for the complete procedure.

## Get Help

- Use the [troubleshooting guide](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Troubleshooting) for spawning, difficulty, base protection, and performance problems.
- Use the [config help form](https://github.com/Riqqqque/ZombieApocalypseAddon/issues/new?template=config_help.yml) when a setting is unclear.
- Use the [bug report form](https://github.com/Riqqqque/ZombieApocalypseAddon/issues/new?template=bug_report.yml) for repeatable problems. Include the loader, Minecraft version, mod version, config, mod list, and latest log.
- Use the [feature request form](https://github.com/Riqqqque/ZombieApocalypseAddon/issues/new?template=feature_request.yml) for new ideas.

Do not paste server addresses, access tokens, private player information, or other secrets into a public issue.

## Development

| Loader | Minecraft | Version |
|---|---:|---:|
| NeoForge | 1.21.1 | 2.8.0 |
| NeoForge | 1.20.1 | 1.11.0 |
| Forge | 1.20.1 | 1.11.0 |

Build all targets on Windows with:

```powershell
.\gradlew.bat build --console=plain
```

Release jars are collected in `build/modrinth`.

Java 21 is required for the aggregate build. The project compiles the Minecraft 1.20.1 targets with Java 17 toolchains. See [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

Maintainers should use the [release checklist](RELEASE_CHECKLIST.md) for every public build.

## More Help

- [Wiki home](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki)
- [Commands](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Commands)
- [Config guide](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Config-Guide)
- [Troubleshooting](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Troubleshooting)
- [Mod compatibility](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Compatibility)
- [Safe uninstall](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Safe-Uninstall)
- [Release notes](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Release-Notes)
- [Support and reporting](SUPPORT.md)

## License

Copyright Rique. All Rights Reserved. See [LICENSE](LICENSE) and [third-party notices](NEOFORGE_MDK_LICENSE.txt).
