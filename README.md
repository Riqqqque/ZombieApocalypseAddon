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
- **Safe defaults:** block breaking, block placing, and zombie towering are disabled.

## Choose the Correct File

| Minecraft | Loader | Mod version | File name |
|---|---|---:|---|
| 1.21.1 | NeoForge | 2.2.33 | `zombieapocalypseaddon-2.2.33.jar` |
| 1.20.1 | NeoForge | 1.5.27 | `zombieapocalypseaddon-neoforge-1.20.1-1.5.27.jar` |
| 1.20.1 | Forge | 1.5.27 | `zombieapocalypseaddon-forge-1.20.1-1.5.27.jar` |

Install only the file for your exact Minecraft version and loader. Forge and NeoForge files are not interchangeable.

## 60-Second Setup

1. Put the correct jar in the server `mods` folder.
2. Start the server and join as an operator.
3. Run `/za` to see the main dashboard.
4. Run `/za preset standard` for the recommended setup.
5. Use `/za help` whenever you need a short command topic.

That is enough for a normal server. You do not need to edit advanced attributes or compatibility settings.

## Gameplay Presets

Presets turn daytime custom waves on and change the main spawning, event, and basic scaling settings. They also restore a safe spawn range, minimum distance, attempt count, and open-sky rule so an old impossible distance setup cannot break a preset. They never enable block breaking, block placing, towering, or overwrite advanced attribute values.

| Command | Best for | Main behavior |
|---|---|---|
| `/za preset casual` | Casual groups and new worlds | Fewer zombies, day-10 daylight grace, no baby zombies, and torch-protected bases. |
| `/za preset standard` | Most survival servers | Recommended default pressure and progression. |
| `/za preset hardcore` | Tested challenge worlds | Faster waves, larger events, and quicker difficulty growth. |

Use `/za status` afterward to confirm what is active.

## Default Experience

With the standard defaults:

- Custom zombie waves can spawn near survival players during day and night.
- Zombies ignore sunlight burning.
- Normal waves attempt two zombies with a 50% chance every six seconds.
- Nearby custom pressure is capped per player.
- Nights receive extra spawn pressure.
- Hordes and blood moons can happen naturally.
- Difficulty begins scaling after a short grace period.
- Death cooldowns reduce immediate repeat pressure after a player dies.
- Zombie variants, kill tracking, milestones, effects, and bonus drops are enabled.
- Player-built lights do not block custom waves unless light protection is enabled.
- World-damaging features remain off until an administrator enables them.

## Main Commands

Start with these:

| Command | Purpose |
|---|---|
| `/za` | Short dashboard for the current day, waves, events, scaling, and optional siege features. |
| `/za preset <casual\|standard\|hardcore>` | Apply a safe gameplay preset. |
| `/za config` | Show the config path and beginner sections. |
| `/za help` or `/zhelp` | Show quick help. |
| `/za help <topic>` | Show focused help for one system. |
| `/za spawn` | Show the important custom-spawn settings. |
| `/za spawn daytime <on\|off>` | Use `off` for permanent night-only custom waves. |
| `/za events` | Show current horde and blood moon status. |
| `/za scaling` | Show current day-based difficulty progress. |

Help topics are `start`, `spawning`, `events`, `difficulty`, `bases`, `admin`, `advanced`, and `all`.

### Command Families

| Command | Controls |
|---|---|
| `/za spawn` | Custom waves, variants, dimensions, effects, and main system toggles. |
| `/za events` and `/za bloodmoon` | Horde and blood moon events. |
| `/za day` and `/za scaling` | World day and difficulty progression. |
| `/za breaking` | Optional zombie block breaking. |
| `/za placing` | Optional limited bridges and steps. |
| `/za towering` | Optional crowd-based zombie climbing. |
| `/za stats` | Kill totals, cooldowns, and milestone reset. |
| `/za compatibility` | Mixed-mod compatibility safeguards. |
| `/za attributes` | Advanced attribute tuning. |
| `/za burn`, `/za kill`, `/za cleanup` | Sunlight, cleanup, and safe removal utilities. |

Press Tab after `/za` to see every command family. Tab completion also suggests on/off states, current and common numeric values, attribute keys, online players, and registered block IDs. Running a setting without a value shows its current value. The original `/z...` commands remain fully supported for existing servers and command blocks.

The [complete command reference](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Commands) includes every subcommand and range.

## Core Systems

### Custom Zombie Waves

The mod performs bounded spawn checks around living survival players. Administrators can control wave chance, timing, size, nearby cap, distance, position attempts, permanent daytime enablement, a temporary daylight start day, open-sky rules, and block-light protection. Turning off the open-sky rule also searches near the player's height for valid caves and covered spaces instead of only choosing the surface.

Use `/za spawn daytime off` for permanent night-only custom spawning. Normal night waves and blood moons continue. Scheduled dawn hordes pause, and `/za events start` must be used at night. The Nether, End, and other fixed-time dimensions remain controlled by their own dimension toggles. Use `/za spawn daylightstart <day>` only when daytime waves should begin after a temporary grace period.

Use `/za spawn status all` only when you need every related toggle. The normal `/za spawn` output stays short.

### Light Protection

`maxBlockLightForSpawning` controls whether torches and similar blocks protect an area from the mod's custom waves:

- `-1`: ignore block light. This is the default apocalypse behavior.
- `0`: custom spawns require complete block darkness.
- `7`: brighter areas block custom spawns.

This setting checks block light, not sunlight. It does not disable daytime waves by itself; use `/za spawn daytime off` for that.

### Hordes and Blood Moons

Hordes are scheduled high-pressure events. Blood moons are random night events. Each system has separate chance, timing, wave, and multiplier controls. Administrators can also start them manually. Events require custom waves; turning custom waves off cancels active or queued spawn pressure. Night-only mode keeps blood moons and nighttime manual hordes available, but pauses scheduled dawn hordes and blocks horde waves during daytime. If both events overlap, their multipliers stack and the larger configured event wave size wins.

### Difficulty Progression

Basic scaling can increase health, speed, armor, and weapon chances as the world gets older. The advanced attribute system can separately tune health, attack damage, movement speed, armor, follow range, and knockback resistance.

Most servers should use a preset or the basic `[scaling]` section and leave `[attributes]` unchanged.

### Optional Base Pressure

All three systems are disabled by default:

- **Block breaking:** bounded attempts with hardness, safety, target, obstacle, drop, and `mobGriefing` controls.
- **Block placing:** limited solid blocks for one-block steps and gaps, with placement limits and protection hooks.
- **Towering:** crowded zombies can climb over each other without changing blocks.

Each feature has a start day, live commands, conservative defaults, and protection checks.

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

- Stop the server before manual config edits.
- `20` ticks equals one second.
- Chances use `0.0` to `1.0`; for example, `0.25` means 25%.
- Change one performance-heavy setting at a time and test with multiple players.
- Keep a world backup before enabling destructive behavior.

See the [config guide](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Config-Guide) for examples and safe ranges.

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
| NeoForge | 1.21.1 | 2.2.33 |
| NeoForge | 1.20.1 | 1.5.27 |
| Forge | 1.20.1 | 1.5.27 |

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
