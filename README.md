# Zombie Apocalypse Addon

Turn your Minecraft world into a nonstop survival scenario.

Server-side only: install it on the server. Players do not need the mod installed on their clients to join.

This mod removes the "safe daytime" feeling by spawning zombie-type mobs around players all day and night. As in-game days pass, those mobs get more dangerous through scaling and configurable attribute systems.

If you want tension at all times instead of a peaceful daytime loop, this mod is built for that.

---

## What This Mod Changes

- Zombies can be configured to ignore sunlight burning (enabled by default).
- Zombie-class mobs spawn around survival players day and night.
- Torch/lantern/glowstone-style light can optionally block custom spawns.
- Horde events can occur naturally or be started manually.
- Blood moon nights can occur randomly or be forced manually.
- Difficulty scaling increases pressure over time.
- Early-game daytime spawning can be delayed for a configurable number of days.
- Optional morning day-counter titles can show the current day at dawn.
- Advanced attribute modifiers let you tune enemy stats deeply.
- Biome and dimension context profiles let stats change by location.
- Most behavior is configurable live with commands.
- Config file includes extensive comments and safe ranges.
- Server-only install support for multiplayer servers.

This is not "just more zombies." It is a complete configurable zombie-pressure system.

---

## Core Gameplay Systems

## 1) Constant Threat Spawning
Custom spawning can run 24/7 near players.
You control:
- how often spawn checks happen
- chance of a spawn wave
- how many mobs per wave
- per-player nearby cap
- how far from players mobs can spawn
- how many spawn position attempts are made per mob

Result: day and night are both dangerous, and pressure can be tuned for casual servers or hardcore worlds.

## 2) Horde Events
Hordes are timed high-pressure events:
- increased spawn intensity
- larger wave sizes
- configurable duration and frequency
- optional on-screen alerts for players

Admins can force-start or stop hordes with commands.

## 3) Blood Moon Nights
Every night can roll a blood moon chance:
- stronger spawn pressure than normal nights
- stacks with other multipliers
- can be forced by command now/tonight

## 4) Difficulty Scaling Over Days
The mod can scale enemy pressure as days pass:
- legacy scaling fields for movement speed and health boost
- full attribute scaling through the new attribute system
- configurable start day and max day for progression curve

This creates a "race against time" survival feeling.

## 5) Variant and Biome Spawn Logic
Spawn composition can change by biome:
- deserts/badlands: more husks
- water biomes: more drowned
- mushroom fields can be safe zones (configurable)

Optional custom spawning in Nether and End is supported.

## 6) Death Cooldown
After a player death, spawn pressure can be reduced temporarily to prevent repeated instant deaths and frustration loops.

## 7) Stats, Effects, and Drops
- kill tracking
- kill milestones with advancements at 250, 1000, and 3000 zombie kills
- cooldown tracking
- optional atmospheric spawn particles/sounds
- optional extra drops
- optional debug logging for troubleshooting

---

## Advanced Attribute System (Deep Explanation)

This mod now includes a layered attribute pipeline for zombie-class mobs when they enter the world:

- Health
- Attack damage
- Movement speed
- Armor
- Follow range
- Knockback resistance

### Attribute Layer Order
For each spawned mob, attribute value flow is:

1. start from vanilla base value
2. apply `base` profile (global for all zombie-class mobs)
3. apply `variant` profile (zombie/husk/drowned/zombie villager) if enabled
4. apply `context` multipliers (biome/dimension) if enabled
5. apply attribute day-scaling (if enabled)
6. apply legacy scaling compatibility values (health/speed)
7. clamp to safe min/max values

### Why This Matters
You can now tune difficulty in a controlled way:
- "All mobs are 15% faster globally"
- "Husks hit harder than regular zombies"
- "Drowned are tankier in water biomes"
- "Nether spawns are faster and more aggressive"
- "Late-game scaling increases follow range so mobs detect players sooner"

---

## Biome and Dimension Context Profiles

Context profiles multiply attributes based on spawn location.

Contexts:
- `desert`
- `water`
- `mushroom`
- `nether`
- `end`

If multiple contexts apply, multipliers stack multiplicatively.

Example:
- Desert speed multiplier = `1.10`
- Nether speed multiplier = `1.20`
- Spawn in Nether desert-like context => combined speed multiplier is about `1.32`

---

## Commands (Full Reference)

All commands require OP level 2.

## General
| Command | What it does |
|---|---|
| `/zhelp` | Shows built-in command help. |
| `/zburn <true\|false>` | Controls zombie daylight burning. `true` means they burn in daylight, `false` means they do not. |
| `/zday [status\|set <day>]` | Shows or sets the world day counter. |
| `/zkill` | Removes all zombie-class entities from loaded levels. |

## Events
| Command | What it does |
|---|---|
| `/zhorde start` | Starts a horde event immediately. |
| `/zhorde stop` | Stops the active horde event. |
| `/zhorde status` | Shows horde/blood-moon state and active spawn multiplier. |
| `/zbloodmoon` | Forces blood moon now if night, or queues it for tonight if day. |

## Stats
| Command | What it does |
|---|---|
| `/zstats` | Shows your own stats if run by player; server summary if run from console. |
| `/zstats <player>` | Shows stats for a specific player. |
| `/zstats all` | Shows server totals. |
| `/zstats clear` | Clears tracked stats, milestone progress, and cooldown state. |

## Scaling
| Command | What it does |
|---|---|
| `/zscaling status` | Shows current scaling day/factor and attribute system status. |

## Spawn Settings (`/zdayspawn`)
| Command | What it does |
|---|---|
| `/zdayspawn status` | Prints full spawn/settings summary. |
| `/zdayspawn enabled <true\|false>` | Master toggle for custom spawning. |
| `/zdayspawn chance <0.0-1.0>` | Spawn-wave chance per spawn interval. |
| `/zdayspawn babychance <0.0-1.0>` | Chance for baby zombie spawns. `0.0` disables new baby zombie-class spawns. |
| `/zdayspawn interval <ticks>` | Normal spawn interval. |
| `/zdayspawn eventinterval <ticks>` | Spawn interval during horde/blood moon. |
| `/zdayspawn amount <1-50>` | Zombies attempted per wave. |
| `/zdayspawn attempts <1-40>` | Spawn position attempts per zombie. |
| `/zdayspawn max <1-500>` | Max nearby zombie-class mobs per player. |
| `/zdayspawn range <16-128>` | Horizontal spawn range around player. |
| `/zdayspawn mindist <8-64>` | Minimum spawn distance from player. |
| `/zdayspawn daylightstart <0-3650>` | Disable custom daytime spawning until this day counter. |
| `/zdayspawn maxlight <-1-15>` | Maximum block light for custom spawns. `-1` ignores light. |
| `/zdayspawn daycounter <true\|false>` | Toggle the morning on-screen day counter. |
| `/zdayspawn sky <true\|false>` | Overworld spawn requires open sky. |
| `/zdayspawn variants <true\|false>` | Enable variant spawning logic. |
| `/zdayspawn nightboost <true\|false>` | Enable night spawn chance boost. |
| `/zdayspawn horde <true\|false>` | Enable horde system. |
| `/zdayspawn hordechance <0.0-1.0>` | Chance scheduled horde day starts a horde. |
| `/zdayspawn bloodmoon <true\|false>` | Enable random blood moon system. |
| `/zdayspawn scaling <true\|false>` | Enable day-based scaling. |
| `/zdayspawn attributes <true\|false>` | Enable advanced attribute modifier system. |
| `/zdayspawn attributescaling <true\|false>` | Let attribute values scale by day factor. |
| `/zdayspawn variantprofiles <true\|false>` | Enable per-variant attribute profiles. |
| `/zdayspawn contextprofiles <true\|false>` | Enable biome/dimension context multipliers. |
| `/zdayspawn biomes <true\|false>` | Enable biome-based variant chance modifiers. |
| `/zdayspawn nether <true\|false>` | Allow custom spawning in Nether. |
| `/zdayspawn end <true\|false>` | Allow custom spawning in End. |
| `/zdayspawn cooldown <true\|false>` | Enable death cooldown system. |
| `/zdayspawn effects <true\|false>` | Enable spawn particles/sounds. |
| `/zdayspawn debug <true\|false>` | Enable debug logging. |

## Live Attribute Commands (`/zattr`)
| Command | What it does |
|---|---|
| `/zattr status` | Shows attribute/scaling status summary. |
| `/zattr keys` | Shows key-group format guide. |
| `/zattr keys all` | Prints all available numeric and toggle keys. |
| `/zattr get <key>` | Reads one numeric key value. |
| `/zattr set <key> <value>` | Live-updates one numeric key value. |
| `/zattr toggle <key> <true\|false>` | Live-updates a supported boolean toggle key. |

---

## Uncommon Terms (Glossary)

| Term | Meaning |
|---|---|
| **Zombie-class** | Zombie, Husk, Drowned, Zombie Villager. |
| **Key** | The exact setting identifier used by `/zattr get` and `/zattr set`. Example: `base.health.multiplier`. |
| **Toggle key** | A boolean key used by `/zattr toggle`. Example: `attributes.enabled`. |
| **Multiplier** | A percentage-like scale factor. `1.0` = unchanged, `1.25` = +25%, `0.8` = -20%. |
| **Bonus** | Flat amount added/subtracted after multiplier. |
| **Context profile** | Attribute multipliers that apply in specific biome/dimension contexts. |
| **Variant profile** | Attribute profile for one mob variant (zombie/husk/drowned/villager). |
| **Scaling factor** | Day progression value from 0.0 to 1.0 based on your scaling start/max days. |
| **Legacy scaling** | Older compatibility scaling fields (`legacy.speedMultiplier`, `legacy.healthBonus`). |
| **Spawn interval** | Time between spawn checks. 20 ticks = 1 second. |
| **Spawn attempts** | Number of location tries per mob to find valid spawn points. |
| **Per-player cap** | Max nearby zombie-class mobs around each player before new spawns are skipped. |

---

## `/zattr` Key Format Guide

General shape:
- `group.subject.attribute.mode`

Examples:
- `base.health.multiplier`
- `variant.husk.attack.bonus`
- `context.nether.speed.multiplier`
- `legacy.healthBonus`

### Numeric Key Groups
- `base.<health|attack|speed|armor|follow|knockback>.<multiplier|bonus>`
- `scale.<health|attack|speed|armor|follow|knockback>.<multiplier|bonus>`
- `variant.<zombie|husk|drowned|villager>.<health|attack|speed|armor|follow|knockback>.<multiplier|bonus>`
- `context.<desert|water|mushroom|nether|end>.<health|attack|speed|armor|follow|knockback>.multiplier`
- `legacy.<speedMultiplier|healthBonus>`

### Toggle Keys
- `attributes.enabled`
- `attributes.scaling`
- `attributes.variantProfiles`
- `attributes.contextProfiles`

---

## Full Numeric Key List (Current Build)

### Base
- `base.health.multiplier`
- `base.health.bonus`
- `base.attack.multiplier`
- `base.attack.bonus`
- `base.speed.multiplier`
- `base.speed.bonus`
- `base.armor.multiplier`
- `base.armor.bonus`
- `base.follow.multiplier`
- `base.follow.bonus`
- `base.knockback.multiplier`
- `base.knockback.bonus`

### Scale
- `scale.health.multiplier`
- `scale.health.bonus`
- `scale.attack.multiplier`
- `scale.attack.bonus`
- `scale.speed.multiplier`
- `scale.speed.bonus`
- `scale.armor.multiplier`
- `scale.armor.bonus`
- `scale.follow.multiplier`
- `scale.follow.bonus`
- `scale.knockback.multiplier`
- `scale.knockback.bonus`

### Variant Zombie
- `variant.zombie.health.multiplier`
- `variant.zombie.health.bonus`
- `variant.zombie.attack.multiplier`
- `variant.zombie.attack.bonus`
- `variant.zombie.speed.multiplier`
- `variant.zombie.speed.bonus`
- `variant.zombie.armor.multiplier`
- `variant.zombie.armor.bonus`
- `variant.zombie.follow.multiplier`
- `variant.zombie.follow.bonus`
- `variant.zombie.knockback.multiplier`
- `variant.zombie.knockback.bonus`

### Variant Husk
- `variant.husk.health.multiplier`
- `variant.husk.health.bonus`
- `variant.husk.attack.multiplier`
- `variant.husk.attack.bonus`
- `variant.husk.speed.multiplier`
- `variant.husk.speed.bonus`
- `variant.husk.armor.multiplier`
- `variant.husk.armor.bonus`
- `variant.husk.follow.multiplier`
- `variant.husk.follow.bonus`
- `variant.husk.knockback.multiplier`
- `variant.husk.knockback.bonus`

### Variant Drowned
- `variant.drowned.health.multiplier`
- `variant.drowned.health.bonus`
- `variant.drowned.attack.multiplier`
- `variant.drowned.attack.bonus`
- `variant.drowned.speed.multiplier`
- `variant.drowned.speed.bonus`
- `variant.drowned.armor.multiplier`
- `variant.drowned.armor.bonus`
- `variant.drowned.follow.multiplier`
- `variant.drowned.follow.bonus`
- `variant.drowned.knockback.multiplier`
- `variant.drowned.knockback.bonus`

### Variant Villager
- `variant.villager.health.multiplier`
- `variant.villager.health.bonus`
- `variant.villager.attack.multiplier`
- `variant.villager.attack.bonus`
- `variant.villager.speed.multiplier`
- `variant.villager.speed.bonus`
- `variant.villager.armor.multiplier`
- `variant.villager.armor.bonus`
- `variant.villager.follow.multiplier`
- `variant.villager.follow.bonus`
- `variant.villager.knockback.multiplier`
- `variant.villager.knockback.bonus`

### Context Desert
- `context.desert.health.multiplier`
- `context.desert.attack.multiplier`
- `context.desert.speed.multiplier`
- `context.desert.armor.multiplier`
- `context.desert.follow.multiplier`
- `context.desert.knockback.multiplier`

### Context Water
- `context.water.health.multiplier`
- `context.water.attack.multiplier`
- `context.water.speed.multiplier`
- `context.water.armor.multiplier`
- `context.water.follow.multiplier`
- `context.water.knockback.multiplier`

### Context Mushroom
- `context.mushroom.health.multiplier`
- `context.mushroom.attack.multiplier`
- `context.mushroom.speed.multiplier`
- `context.mushroom.armor.multiplier`
- `context.mushroom.follow.multiplier`
- `context.mushroom.knockback.multiplier`

### Context Nether
- `context.nether.health.multiplier`
- `context.nether.attack.multiplier`
- `context.nether.speed.multiplier`
- `context.nether.armor.multiplier`
- `context.nether.follow.multiplier`
- `context.nether.knockback.multiplier`

### Context End
- `context.end.health.multiplier`
- `context.end.attack.multiplier`
- `context.end.speed.multiplier`
- `context.end.armor.multiplier`
- `context.end.follow.multiplier`
- `context.end.knockback.multiplier`

### Legacy
- `legacy.speedMultiplier`
- `legacy.healthBonus`

---

## Practical Examples (For Non-Technical Users)

- Make all spawned mobs slightly tankier:
`/zattr set base.health.multiplier 1.15`

- Make only husks hit harder:
`/zattr set variant.husk.attack.multiplier 1.30`

- Make drowned faster in water areas:
`/zattr set context.water.speed.multiplier 1.20`

- Disable biome/dimension context effects quickly:
`/zattr toggle attributes.contextProfiles false`

- Check current value before changing:
`/zattr get variant.drowned.armor.multiplier`

- List all keys if unsure:
`/zattr keys all`

---

## Configuration

All settings are in the common config (`zombieapocalypseaddon-common.toml`) and include explanatory comments.

Main sections:
- `general`
- `dayspawning`
- `variants`
- `horde`
- `bloodmoon`
- `scaling`
- `attributes`
- `nightspawning`
- `biomes`
- `deathcooldown`
- `effects`
- `statistics`
- `drops`

## Installation Notes

- Install on the server for multiplayer worlds.
- Client install is optional. Players can join without the mod.
- Install on the client only if you also want it active in singleplayer.
- Intended for NeoForge or Forge servers matching the release file you download.
- For troubleshooting, enable debug logs with:
`/zdayspawn debug true`

## Development
- Build: `./gradlew build`
- Full clean build: `./gradlew clean build`
- Run tests: `./gradlew test`
