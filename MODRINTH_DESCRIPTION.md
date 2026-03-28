# Zombie Apocalypse Addon

Turn your world into a long-term survival scenario where daytime is no longer safe.

Zombie Apocalypse Addon replaces the normal "sleep through the night and relax during the day" loop with constant pressure, escalating danger, and fully configurable zombie systems. Zombies can keep spawning around players during the day, special events can spike pressure even higher, and enemy strength can evolve over time through layered scaling and attribute profiles.

This is not a lightweight spawn tweak. It is a full zombie survival framework built for servers, challenge worlds, and configurable apocalypse-style gameplay.

## What It Does

- Custom zombie-class spawning around players during both day and night
- Optional sunlight immunity for zombies
- Natural and manual horde events
- Random and forced blood moon nights
- Day-based difficulty progression
- Advanced attribute tuning for spawned zombie-class mobs
- Biome and dimension-based spawn and stat behavior
- Temporary spawn pressure relief after player death
- Persistent kill tracking and milestone advancements
- Live in-game admin controls for most major systems

Zombie-class mobs include:

- Zombie
- Husk
- Drowned
- Zombie Villager

## Core Systems

### Constant Threat Spawning

The mod can run custom spawn waves near survival players all day and night.

You can control:

- spawn interval
- spawn chance
- zombies per wave
- maximum nearby zombies per player
- spawn range
- minimum spawn distance
- number of spawn attempts per mob
- whether overworld spawns require open sky

This makes daytime feel dangerous instead of safe, and it lets you tune the mod for anything from a tense survival world to a full hardcore apocalypse server.

### Horde Events

Hordes are high-pressure zombie surges with their own settings.

You can configure:

- day interval for horde roll checks
- chance a scheduled horde actually starts
- horde duration
- event spawn interval
- event wave size
- event spawn multiplier
- on-screen event notifications

Admins can also start or stop hordes manually.

### Blood Moon Nights

Blood moons can trigger randomly at night or be forced manually.

During a blood moon:

- spawn pressure increases heavily
- event multipliers stack with normal spawning systems
- players get on-screen event alerts when enabled

### Day Counter and Day Control

The mod includes a world day counter system.

- Show the current day each morning on screen
- Toggle morning announcements in config or with command
- Check the current day with `/zday status`
- Set the apocalypse day manually with `/zday set <day>`

When the day is changed, active apocalypse event scheduling state is reset so horde and blood moon logic stays consistent.

### Early-Day Daylight Spawn Grace

If you want the first part of a world to be less annoying during daylight, daytime custom spawning can be delayed until a chosen day.

Examples:

- `0` = daytime spawning is active immediately
- `10` = daytime spawning only starts on day 10

This can be changed in config or live with command.

### Difficulty Scaling

Zombie pressure can scale as the world progresses.

The mod supports:

- scaling start day
- maximum scaling day
- legacy scaling values for speed and health
- full attribute scaling through the advanced attribute system

This makes the world feel increasingly hostile over time instead of flat and predictable.

### Variant, Biome, and Dimension Logic

Spawn composition and stat behavior can change by where mobs spawn.

Supported behavior includes:

- more husks in desert-like biomes
- more drowned in water-heavy biomes
- mushroom fields as optional safe zones
- optional custom spawning in Nether and End
- biome and dimension context multipliers for attributes

### Death Cooldown

After a player dies, spawn pressure can be temporarily reduced to avoid instant repeated deaths.

This is especially useful on servers and harder settings where nonstop spawn pressure can otherwise snowball into frustration.

### Stats, Milestones, Drops, and Effects

The mod also includes support systems around the main apocalypse gameplay:

- persistent zombie kill tracking
- death cooldown tracking
- optional bonus zombie drops
- optional spawn particles and sounds
- optional debug logging
- zombie kill milestone advancements at 250, 1000, and 3000 kills

## Advanced Attribute System

One of the biggest systems in the mod is the attribute pipeline for mod-spawned zombie-class mobs.

Supported attributes:

- Health
- Attack Damage
- Movement Speed
- Armor
- Follow Range
- Knockback Resistance

### Attribute Flow

For each spawned mob, values are applied in this order:

1. Start from vanilla base values
2. Apply global base profile
3. Apply per-variant profile if enabled
4. Apply biome/dimension context multipliers if enabled
5. Apply attribute-based day scaling if enabled
6. Apply legacy health/speed scaling compatibility values
7. Clamp to safe limits

### Why This Matters

This lets you build very specific difficulty behavior, for example:

- all spawned mobs are slightly faster globally
- husks hit harder than standard zombies
- drowned are tankier in water biomes
- Nether spawns are faster and more aggressive
- late-game mobs detect players from farther away

You can keep the system simple, or you can tune it deeply for server balance.

## Commands

All commands require OP level 2.

### General

- `/zhelp` - Show built-in help
- `/zburn <true|false>` - Control whether zombies burn in daylight
- `/zkill` - Remove zombie-class entities from loaded levels

### Day Control

- `/zday` - Show current world day status
- `/zday status` - Show current world day and time-of-day
- `/zday set <day>` - Set the world day counter

### Events

- `/zhorde start` - Start a horde immediately
- `/zhorde stop` - Stop the active horde
- `/zhorde status` - Show horde and blood moon status
- `/zbloodmoon` - Force a blood moon now or queue it for tonight

### Stats

- `/zstats` - Show your own stats or server summary from console
- `/zstats <player>` - Show stats for a specific player
- `/zstats all` - Show server totals
- `/zstats clear` - Clear tracked stats, cooldowns, and milestone progress

### Spawn Settings

- `/zdayspawn status`
- `/zdayspawn enabled <true|false>`
- `/zdayspawn chance <0.0-1.0>`
- `/zdayspawn interval <ticks>`
- `/zdayspawn eventinterval <ticks>`
- `/zdayspawn amount <1-50>`
- `/zdayspawn attempts <1-40>`
- `/zdayspawn max <1-500>`
- `/zdayspawn range <16-128>`
- `/zdayspawn mindist <8-64>`
- `/zdayspawn daylightstart <day>`
- `/zdayspawn sky <true|false>`
- `/zdayspawn variants <true|false>`
- `/zdayspawn nightboost <true|false>`
- `/zdayspawn horde <true|false>`
- `/zdayspawn hordechance <0.0-1.0>`
- `/zdayspawn daycounter <true|false>`
- `/zdayspawn bloodmoon <true|false>`
- `/zdayspawn scaling <true|false>`
- `/zdayspawn attributes <true|false>`
- `/zdayspawn attributescaling <true|false>`
- `/zdayspawn variantprofiles <true|false>`
- `/zdayspawn contextprofiles <true|false>`
- `/zdayspawn biomes <true|false>`
- `/zdayspawn nether <true|false>`
- `/zdayspawn end <true|false>`
- `/zdayspawn cooldown <true|false>`
- `/zdayspawn effects <true|false>`
- `/zdayspawn debug <true|false>`

### Scaling

- `/zscaling status` - Show current scaling state

### Live Attribute Tuning

- `/zattr status`
- `/zattr keys`
- `/zattr keys all`
- `/zattr get <key>`
- `/zattr set <key> <value>`
- `/zattr toggle <key> <true|false>`

## /zattr Key Groups

Numeric key groups:

- `base.<health|attack|speed|armor|follow|knockback>.<multiplier|bonus>`
- `scale.<health|attack|speed|armor|follow|knockback>.<multiplier|bonus>`
- `variant.<zombie|husk|drowned|villager>.<health|attack|speed|armor|follow|knockback>.<multiplier|bonus>`
- `context.<desert|water|mushroom|nether|end>.<health|attack|speed|armor|follow|knockback>.multiplier`
- `legacy.<speedMultiplier|healthBonus>`

Toggle keys:

- `attributes.enabled`
- `attributes.scaling`
- `attributes.variantProfiles`
- `attributes.contextProfiles`

If you want the full live list from the current build, use:

- `/zattr keys all`

## Config and Tuning

The config file includes comments, ranges, and enough control to support very different playstyles.

You can use it for:

- a more hostile vanilla-like survival world
- a PvE server with escalating danger
- a challenge pack apocalypse setup
- a long-running world with increasing pressure over time

Most major settings can also be changed live with commands, which makes balancing much easier during active playtesting.

## Version Notes

Current release layout:

- NeoForge `1.21.1`
- Forge `1.20.1`

If you want a configurable zombie apocalypse system with constant pressure, scaling danger, live admin controls, and deep stat tuning, this mod is built for exactly that.
