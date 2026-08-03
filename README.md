# Zombie Apocalypse Addon

Turn your Minecraft world into a nonstop survival scenario.

Server-side only: install it on the server. Players do not need the mod installed on their clients to join.

Current build targets: NeoForge 1.21.1, NeoForge 1.20.1, and Forge 1.20.1.

This mod removes the "safe daytime" feeling by spawning zombie-type mobs around players all day and night. As in-game days pass, those mobs get more dangerous through scaling and configurable attribute systems.

If you want tension at all times instead of a peaceful daytime loop, this mod is built for that.

---

## What This Mod Changes

- Zombies can be configured to ignore sunlight burning (enabled by default).
- Zombie-class mobs spawn around survival players day and night.
- Torch/lantern/glowstone-style light can optionally block custom spawns.
- Optional zombie block breaking can add configurable base pressure after a chosen day.
- Optional zombie block placing lets zombies build limited one-block steps and bridges.
- Optional World War Z-style towering lets crowded zombies climb over each other after a chosen day.
- Compatibility controls recognize modded zombies while avoiding duplicate AI and difficulty systems.
- Horde events can occur naturally or be started manually.
- Blood moon nights can occur randomly or be forced manually.
- Difficulty scaling increases pressure over time.
- Early-game daytime spawning can be delayed for a configurable number of days.
- Optional morning day-counter titles can show the current day at dawn.
- Advanced attribute modifiers let you tune enemy stats deeply.
- Biome and dimension context profiles let stats change by location.
- Custom spawn placement avoids unloaded chunks to reduce server stalls.
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

## 7) Optional Zombie Block Breaking
Zombies can be allowed to break blocks after a configured day.

This is off by default because it can damage bases. If enabled, you control:
- start day
- check interval
- break chance
- range
- max block hardness
- whether blocks drop items
- whether a target or obstacle is required
- whether mobGriefing can stop it
- whether containers, tool-required blocks, and light sources are protected

The default hardness limit is meant for soft blocks and wooden doors, while stone, ores, containers, machines, and light blocks stay protected unless you loosen the safety settings.

Use `/zblockbreak dayone` if you want to enable it and make it active immediately.

## 8) Optional Zombie Block Placing
Zombie-class mobs can place a safe solid block to cross a one-block gap or make a one-block step while pursuing a target.

This is off by default. If enabled, you control:
- start day
- attempt interval and chance
- placed block ID
- lifetime placement limit per zombie
- target requirement and maximum target distance
- whether an obstacle is required
- bridges and steps separately
- fluid and replaceable-block replacement
- whether `mobGriefing` can stop placement

Placed blocks must be stable, solid, breakable blocks without block entities. Placement stays inside loaded chunks and the world border, cannot overlap entities, and fires the normal loader block-place event so claim and protection mods can cancel it.

Use `/zblockplace dayone` for immediate activation, or enable it with a later start day for slower progression.

## 9) Optional Zombie Towering
Recognized Zombie subclasses can use a nearby swarm to climb upward and push toward an obstructed, covered, or raised target.

This is off by default and starts on day 20 when enabled. It never places or breaks blocks. The default rules require:
- a valid non-creative, non-spectator target
- at least two other nearby Zombie subclasses
- a collision, nearby barrier, covered target, or raised target
- enough block clearance above the climbing zombie
- a target within 32 blocks
- a height no more than eight blocks above the target

Checks are staggered across ticks and the chance roll happens before the bounded nearby-entity lookup. Zombies cannot tower while riding, carrying passengers, swimming, in lava, or running without AI. Horizontal speed is capped, and airborne zombies need physical swarm support before receiving another boost.

Use `/ztower enabled true` to keep the default day-20 gate, `/ztower startday <day>` to change it, or `/ztower dayone` for immediate activation.

## 10) Mod Compatibility

Modded `Zombie` subclasses are recognized automatically. Known nonstandard zombie entities are included through optional entity-type tags, and server owners can add or exclude exact entity IDs in the `compatibility` config section.

Recognized modded zombies count toward nearby caps, `/zkill`, kill statistics, milestone advancements, and optional drops. Difficulty scaling can apply to them without replacing equipment they already spawned with. Sun protection and the optional AI features apply to compatible `Zombie` subclasses.

The addon fires the official loader spawn-placement, position, finalize-spawn, block-break, block-place, and mob-griefing hooks. Spawn-control and claim mods can therefore reject addon actions. Compatibility safeguards also avoid duplicate AI or difficulty behavior where another supported mod owns it, and can respect a zombie's door-breaking ability for mods such as Zombie Proof Doors.

Built-in compatibility covers:

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

Use `/zcompat status` to see detected integrations and active safeguards. Each behavior can be changed live, but the defaults are the safest starting point for mixed modpacks.

## 11) Stats, Effects, and Drops
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
| `/zcleanup [uninstall]` | Removes loaded zombie leftovers, resets apocalypse event state, and can pause core systems for safe temporary removal. |

## Compatibility (`/zcompat`)
| Command | What it does |
|---|---|
| `/zcompat [status]` | Shows detected supported mods and active compatibility safeguards. |
| `/zcompat modded <true\|false>` | Toggles automatic recognition of modded `Zombie` subclasses. |
| `/zcompat difficulty <true\|false>` | Toggles addon difficulty scaling for recognized modded zombies. |
| `/zcompat ai <true\|false>` | Toggles addon block breaking, block placing, and towering for recognized modded `Zombie` subclasses. |
| `/zcompat spawnrules <true\|false>` | Lets external spawn-control mods approve or reject addon spawn positions. |
| `/zcompat externalai <true\|false>` | Avoids addon AI features where a supported mod owns zombie AI. |
| `/zcompat externaldifficulty <true\|false>` | Avoids stacking addon difficulty on entities managed by another difficulty mod. |
| `/zcompat doors <true\|false>` | Respects per-zombie door-breaking permission before the addon breaks a door. |
| `/zcompat equipment <true\|false>` | Preserves weapons and armor already supplied by Minecraft or another mod. |

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

## Block Breaking (`/zblockbreak`)
| Command | What it does |
|---|---|
| `/zblockbreak` | Shows current zombie block-breaking settings. |
| `/zblockbreak status` | Shows current zombie block-breaking settings. |
| `/zblockbreak dayone` | Enables zombie block breaking and sets the start day to 0. |
| `/zblockbreak enabled <true\|false>` | Toggles zombie block breaking. |
| `/zblockbreak startday <0-3650>` | Sets the day when block breaking can start. |
| `/zblockbreak interval <20-72000>` | Sets how often each zombie can attempt block breaking. |
| `/zblockbreak chance <0.0-1.0>` | Sets the chance per scheduled block-breaking check. |
| `/zblockbreak range <1-4>` | Sets how far in front of the zombie it checks. |
| `/zblockbreak hardness <0.0-50.0>` | Sets the maximum block hardness zombies can break. |
| `/zblockbreak drops <true\|false>` | Toggles item drops from zombie-broken blocks. |
| `/zblockbreak target <true\|false>` | Requires zombies to have a valid target before breaking. |
| `/zblockbreak obstacle <true\|false>` | Requires a blocked path or covered target before breaking. |
| `/zblockbreak mobgriefing <true\|false>` | Makes mobGriefing and loader mob-griefing events control this feature. |
| `/zblockbreak containers <true\|false>` | Allows or blocks breaking chests, furnaces, and block-entity blocks. |
| `/zblockbreak toolblocks <true\|false>` | Allows or blocks breaking blocks that require the correct tool. |
| `/zblockbreak lights <true\|false>` | Allows or blocks breaking light-emitting blocks. |

## Block Placing (`/zblockplace`)
| Command | What it does |
|---|---|
| `/zblockplace` | Shows current zombie block-placing settings. |
| `/zblockplace status` | Shows current zombie block-placing settings. |
| `/zblockplace dayone` | Enables zombie block placing and sets the start day to 0. |
| `/zblockplace enabled <true\|false>` | Toggles zombie block placing. |
| `/zblockplace startday <0-3650>` | Sets the day when block placing can start. |
| `/zblockplace interval <20-72000>` | Sets how often each zombie can attempt block placing. |
| `/zblockplace chance <0.0-1.0>` | Sets the chance per scheduled block-placing check. |
| `/zblockplace block <namespace:id>` | Chooses the safe solid block zombies place. |
| `/zblockplace limit <0-256>` | Sets the lifetime placement limit per zombie. `0` is unlimited. |
| `/zblockplace distance <4-128>` | Sets the farthest target distance that allows placement. |
| `/zblockplace target <true\|false>` | Requires zombies to have a valid target before placing. |
| `/zblockplace obstacle <true\|false>` | Requires a blocked path, covered/raised target, or gap. |
| `/zblockplace mobgriefing <true\|false>` | Makes `mobGriefing` and loader mob-griefing events control placement. |
| `/zblockplace bridges <true\|false>` | Toggles filling one-block-deep gaps. |
| `/zblockplace steps <true\|false>` | Toggles placing one-block steps. |
| `/zblockplace fluids <true\|false>` | Allows or blocks replacing fluid blocks. |
| `/zblockplace replaceable <true\|false>` | Allows or blocks replacing plants, snow, and similar blocks. |
| `/zblockplace resetcounts` | Resets lifetime placement counts for loaded zombie-class mobs. |

## Zombie Towering (`/ztower`)
| Command | What it does |
|---|---|
| `/ztower` | Shows current zombie towering settings. |
| `/ztower status` | Shows current zombie towering settings. |
| `/ztower dayone` | Enables towering and sets the start day to 0. |
| `/ztower enabled <true\|false>` | Toggles zombie towering. |
| `/ztower startday <0-3650>` | Sets the day when towering can start. |
| `/ztower interval <5-72000>` | Sets how often each zombie can check for towering. |
| `/ztower chance <0.0-1.0>` | Sets the chance per scheduled towering check. |
| `/ztower distance <4-128>` | Sets the farthest target distance that allows towering. |
| `/ztower crowd <1-16>` | Sets the number of other nearby zombies required. |
| `/ztower radius <0.75-6.0>` | Sets the nearby-crowd search radius. |
| `/ztower vertical <0.1-1.0>` | Sets the upward velocity of a towering boost. |
| `/ztower forward <0.0-0.6>` | Sets the target-facing horizontal velocity. |
| `/ztower height <1-32>` | Sets the maximum height above the target. |
| `/ztower obstacle <true\|false>` | Requires a collision, barrier, covered target, or raised target. |

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
- `compatibility`
- `dayspawning`
- `variants`
- `blockbreaking`
- `blockplacing`
- `towering`
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

If custom zombies stop spawning, run `/zdayspawn status`. The command now warns when
`minSpawnDistance` cannot fit inside `spawnRange`; that invalid combination pauses
custom spawning until one of those values is corrected.

## Development
- Build: `./gradlew build`
- Full clean build: `./gradlew clean build`
- Run tests: `./gradlew test`
