# Zombie Apocalypse Addon

Turn Minecraft into a configurable zombie survival world where daytime is not automatically safe.

Zombie Apocalypse Addon adds custom zombie waves, hordes, blood moons, day-based difficulty, optional siege behavior, kill milestones, and broad zombie-mod compatibility. It works for a simple survival server out of the box, while still providing deep controls for apocalypse packs.

## Quick Info

- **Multiplayer:** install on the server only. Players can join without the mod.
- **Singleplayer:** install on the client because singleplayer runs a local server.
- **Current targets:** NeoForge 1.21.1, NeoForge 1.20.1, and Forge 1.20.1.
- **Commands:** anyone can view `/za`, `/zhelp`, and `/zstats`; changing settings requires permission level 2.
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

Presets never enable block breaking, block placing, or towering. They also leave advanced attribute values alone.

Use `/za help` for short topic-based help instead of a wall of commands.

## Main Features

- Custom zombie waves around survival players during day and night
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
- Optional crowd-based zombie towering without block changes
- Biome-aware Husk and Drowned spawning
- Optional Nether and End custom spawning
- Death cooldown protection after a player dies
- Kill tracking and advancements at 250, 1,000, and 3,000 kills
- Configurable bonus drops, including gunpowder
- Spawn effects, debug logging, and live admin controls
- Broad compatibility with zombie and spawn-control mods

## What the Default Setup Does

The standard setup attempts two zombies with a 50% wave chance every six seconds. It limits nearby pressure per player, increases pressure at night, enables natural events and progression, and keeps destructive features disabled.

Player-built lights do not block the addon's custom waves by default. If you want torches and similar blocks to protect bases, use the Casual preset or set:

```mcfunction
/zdayspawn maxlight 7
```

This checks block light only. Sunlight remains ignored for custom daytime spawning.

## Essential Commands

| Command | Purpose |
|---|---|
| `/za` | Show the main dashboard. |
| `/za preset <casual\|standard\|hardcore>` | Apply a safe gameplay preset. |
| `/za config` | Show the config path and beginner sections. |
| `/za help` or `/zhelp` | Show quick help and available topics. |
| `/zdayspawn` | Show important custom-spawn settings. |
| `/zdayspawn status all` | Show every spawn-related toggle. |
| `/zhorde` | Show horde and blood moon status. |
| `/zscaling` | Show day-based difficulty progress. |
| `/zcompat` | Show mixed-mod compatibility safeguards. |
| `/zcleanup uninstall` | Prepare a world before removing the mod. |

Detailed command families remain available:

- `/zdayspawn` for spawning and main toggles
- `/zhorde` and `/zbloodmoon` for events
- `/zday` and `/zscaling` for progression
- `/zblockbreak`, `/zblockplace`, and `/ztower` for optional base pressure
- `/zstats` for kills and milestone reset
- `/zattr` for advanced attributes
- `/zcompat` for mixed-mod behavior
- `/zburn`, `/zkill`, and `/zcleanup` for utilities

Bare status commands now show their current settings. Optional siege commands accept simple `on` and `off` forms.

See the [full command reference](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Commands) for every subcommand and range.

## Custom Spawning

Administrators can control:

- wave chance and interval
- zombies attempted per wave
- nearby cap per player
- minimum and maximum spawn distance
- position attempts
- daylight start day
- open-sky requirement
- maximum allowed block light
- zombie variant and baby chances
- night pressure
- biome and dimension behavior

Spawn checks are bounded, staggered where appropriate, and avoid unloaded chunks.

## Events and Progression

Hordes temporarily raise wave pressure on scheduled days. Blood moons create stronger night pressure. Both have separate chance, size, duration, interval, and multiplier settings, and both can be triggered manually.

Basic day scaling is enough for most servers. Advanced profiles can separately tune health, attack damage, movement speed, armor, follow range, and knockback resistance for each variant and environment.

## Optional Base Pressure

Block breaking, block placing, and towering are independent and disabled by default.

- Block breaking has day gating, hardness limits, protected block categories, target rules, drops, and `mobGriefing` support.
- Block placing uses a configurable solid block, per-zombie limits, loaded-chunk checks, protection events, and separate step/bridge controls.
- Towering lets a blocked zombie crowd climb without placing or breaking anything.

Administrators can enable each feature immediately with `dayone`, use `on` while preserving its configured start day, or use `off` at any time.

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

Every setting includes a plain-language description, examples, safe ranges, and performance warnings where relevant. Remember that 20 ticks equals one second and chances use decimals from `0.0` to `1.0`.

See the [config guide](https://github.com/Riqqqque/ZombieApocalypseAddon/wiki/Config-Guide) for practical examples.

## Safe Removal

Run this before temporarily or permanently removing the mod:

```mcfunction
/zcleanup uninstall
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
