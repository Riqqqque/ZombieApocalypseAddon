# Zombie Apocalypse Addon

Turn Minecraft into a configurable zombie survival world where daytime is not automatically safe.

Zombie Apocalypse Addon adds custom zombie waves, hordes, blood moons, day-based difficulty, optional siege behavior, kill milestones, and broad zombie-mod compatibility. It works for a simple survival server out of the box, while still providing deep controls for apocalypse packs.

## Quick Info

- **Multiplayer:** install on the server only. Players can join without the mod.
- **Singleplayer:** install on the client because singleplayer runs a local server.
- **Current targets:** NeoForge 1.21.1, NeoForge 1.20.1, and Forge 1.20.1.
- **Commands:** anyone can read dashboards, help, stats, status pages, and current setting values. Changes require permission level 2.
- **Config:** `config/zombieapocalypseaddon-common.toml`.
- **Safe defaults:** block breaking, block placing, and zombie towering are off.

## Start Here

After installing the correct jar, join as an operator and run:

```mcfunction
/za
```

This opens a short dashboard showing the current day, custom waves, light protection, events, scaling, and optional world-pressure features.

For a fast setup, choose one gameplay preset:

```mcfunction
/za preset casual
/za preset standard
/za preset hardcore
```

- **Casual:** fewer zombies, day-10 daylight grace, no baby zombies, and torch-protected bases.
- **Standard:** recommended balance for most survival servers.
- **Hardcore:** faster waves, larger events, and quicker difficulty growth.

Presets turn daytime custom waves on and restore a safe spawn range, minimum distance, attempt count, and open-sky rule, so an old impossible distance setup cannot break the selected preset. They never enable block breaking, block placing, or towering, and they leave advanced attribute values alone.

Use `/za help` for short topic-based help instead of a wall of commands. Press Tab after `/za` to browse every system and get suggestions for states, values, players, attribute keys, and blocks.

## Main Features

- Custom zombie waves around survival players during day and night
- Permanent night-only custom spawning mode
- Optional sunlight immunity
- Optional torch and block-light spawn protection
- Configurable baby zombie chance, including complete disablement
- Natural and manual horde events
- Random and forced blood moons
- Morning day-counter announcements and manual day control
- Day-based health, speed, armor, and equipment scaling
- Advanced per-attribute, per-variant, biome, and dimension tuning
- Optional zombie block breaking with conservative safety rules
- Optional limited block placing for steps and one-block gaps
- Optional moving zombie stacks that climb defenses without changing blocks
- Biome-aware Husk and Drowned spawning
- Optional Nether and End custom spawning
- Death cooldown protection after a player dies
- Kill tracking and advancements at 250, 1,000, and 3,000 kills
- Configurable bonus drops, including gunpowder
- Spawn effects, debug logging, and live admin controls
- Broad compatibility with zombie and spawn-control mods

Mushroom-field safe zones remain active even if biome variant weighting is turned off. Night pressure only applies in dimensions with a real day/night cycle, so the Nether, End, and other fixed-time dimensions do not receive a permanent night boost. Bonus drops also respect the vanilla `doMobLoot` gamerule.

## What the Default Setup Does

The standard setup attempts two zombies with a 50% wave chance every six seconds. It limits nearby pressure per player, increases pressure at night, enables natural events and progression, and keeps destructive features disabled.

Player-built lights do not block the addon's custom waves by default. If you want torches and similar blocks to protect bases, use the Casual preset or set:

```mcfunction
/za spawn maxlight 7
```

This checks block light only. It does not disable daytime waves by itself; use `/za spawn daytime off` for that.

## Essential Commands

| Command | Purpose |
|---|---|
| `/za` | Show the main dashboard. |
| `/za preset <casual\|standard\|hardcore>` | Apply a safe gameplay preset. |
| `/za config` | Show the config path and beginner sections. |
| `/za help` or `/zhelp` | Show quick help and available topics. |
| `/za spawn` | Show important custom-spawn settings. |
| `/za spawn daytime <on\|off>` | Use `off` for permanent night-only custom waves. |
| `/za spawn status all` | Show every spawn-related toggle. |
| `/za events` | Show horde and blood moon status. |
| `/za scaling` | Show day-based difficulty progress. |
| `/za compatibility` | Show mixed-mod compatibility safeguards. |
| `/za cleanup uninstall` | Prepare a world before removing the mod. |

Every system is available below `/za`:

- `/za spawn` for spawning and main toggles
- `/za events` and `/za bloodmoon` for events
- `/za day` and `/za scaling` for progression
- `/za breaking`, `/za placing`, and `/za towering` for optional base pressure
- `/za stats` for kills and milestone reset
- `/za attributes` for advanced attributes
- `/za compatibility` for mixed-mod behavior
- `/za burn`, `/za kill`, and `/za cleanup` for utilities

Run any setting name without a value to read its current value. Use `on` and `off` for switches; `true` and `false` still work for existing command blocks and scripts. All original `/z...` command roots remain supported.

Feature-level `on` commands load balanced working defaults automatically. You can enable spawning, events, blood moons, scaling, attributes, compatibility, block breaking, block placing, or towering without first repairing old chances, day gates, or dependent settings.

See the [full command reference](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Commands) for every subcommand and range.

## Custom Spawning

Administrators can control:

- wave chance and interval
- zombies attempted per wave
- nearby cap per player
- minimum and maximum spawn distance
- position attempts
- permanent daytime spawning toggle
- daylight start day
- open-sky requirement
- maximum allowed block light
- zombie variant and baby chances
- night pressure
- biome and dimension behavior

Spawn checks are bounded, staggered where appropriate, and avoid unloaded chunks. With open-sky spawning disabled, the mod searches near the player's height for valid caves and covered spaces instead of only choosing the surface.

For night-only custom spawning, run `/za spawn daytime off`. Normal night waves and blood moons continue. Scheduled dawn hordes pause, and manual hordes can be started at night. The Nether, End, and other fixed-time dimensions keep using their own dimension toggles. `daylightstart` remains available as a separate temporary grace period for servers that want daytime waves to begin later.

## Events and Progression

Hordes temporarily raise wave pressure on scheduled days. Blood moons create stronger night pressure. Both have separate chance, size, duration, interval, and multiplier settings available through `/za events` and `/za bloodmoon`, and both can be triggered manually. They require custom waves; turning custom waves off cancels active or queued spawn pressure. Night-only mode pauses scheduled dawn hordes and daytime horde waves without disabling night waves or blood moons. If both events overlap, their multipliers stack and the larger configured event wave size wins.

Basic day scaling is enough for most servers. `/za scaling` exposes the progression days and basic full-strength bonuses. Advanced profiles can separately tune health, attack damage, movement speed, armor, follow range, and knockback resistance for each variant and environment, with exact per-key ranges shown by `/za attributes get <key>`.

## Optional Base Pressure

Block breaking, block placing, and towering are independent and disabled by default. No-AI zombies are ignored, and a zombie can complete at most one of these actions during the same tick.

- Block breaking has day gating, hardness limits, protected block categories, target rules, drops, and `mobGriefing` support.
- Block placing uses a configurable solid block, per-zombie limits, loaded-chunk checks, protection events, and separate step/bridge controls.
- Towering grows the deepest nearby moving passenger stack toward a configurable maximum of 128 zombies. Brief AI target loss no longer collapses valid towers, separate towers can be capped per player, and top zombies jump off one at a time using a configurable cooldown. Jump attacks can also be disabled so zombies remain stacked. No blocks are placed or broken.

Administrators can use `on` for an immediate balanced preset, use detailed commands afterward for custom tuning, or use `off` at any time. The older `dayone` aliases remain supported.

Tower controls include `/za towering stacksize <2-128>`, `/za towering maxperplayer <count>` (`0` means unlimited), `/za towering jumping <on|off>`, and `/za towering jumpcooldown <ticks>`. The stack size is a maximum; a tower can stop earlier when it reaches its target or another safety condition.

## Mod Compatibility

The addon automatically recognizes modded `Zombie` subclasses and supports unusual infected entities through optional tags and exact include/exclude lists.

Compatibility safeguards can respect external spawn rules, avoid duplicate AI or difficulty systems, preserve modded equipment, and honor per-zombie door-breaking permission.

Built-in handling covers:

- Zombie Awareness
- Zombie Horse Spawn
- Mo' Zombies Wave
- More Zombie Villagers
- Zombies Reworked
- Zombie Villagers From Spawner
- Zombie Variants
- Zombies+
- Zombie Proof Doors
- Undead Nights
- The Hordes
- Improved Mobs
- In Control!
- Bad Mobs
- Giant Spawn

Other mods can work automatically when their infected mobs extend Minecraft's normal `Zombie` class. See the [compatibility guide](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Compatibility) for exact behavior and overrides.

## Config That Normal Servers Need

The generated file begins with a **START HERE** section. For a normal setup, focus on:

- `[dayspawning]`
- `[variants]`
- `[horde]`
- `[bloodmoon]`
- `[scaling]`

Leave `[compatibility]`, `[attributes]`, variant profiles, and context profiles at their defaults unless you specifically need deep tuning.

Every setting includes a plain-language description, examples, safe ranges, feature dependencies, and performance warnings where relevant. Remember that 20 ticks equals one second and chances use decimals from `0.0` to `1.0`.

See the [config guide](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Config-Guide) for practical examples.

## Safe Removal

Run this before temporarily or permanently removing the mod:

```mcfunction
/za cleanup uninstall
```

Then stop the server and remove the jar. The command removes loaded zombie-class mobs, resets event state, and disables active addon systems.

## Help and Documentation

- [Wiki home](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki)
- [Commands](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Commands)
- [Config guide](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Config-Guide)
- [Troubleshooting](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Troubleshooting)
- [Compatibility](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Compatibility)
- [Safe uninstall](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Safe-Uninstall)

If you want constant zombie pressure without forcing every server into the same difficulty, this mod is built for that.
