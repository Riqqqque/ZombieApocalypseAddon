# Zombie Apocalypse Addon - Everything Can Be Customized

Turn your Minecraft world into a long-term zombie survival scenario where daytime is no longer safe.

Zombie Apocalypse Addon replaces the usual "survive the night, relax during the day" loop with constant pressure, escalating danger, event surges, and deep server-side control. Zombie-class mobs can spawn around players during both day and night, hordes and blood moons can raise the pressure even higher, and enemy stats can scale as the world gets older.

This is not just "more zombies." It is a configurable zombie survival framework built for servers, challenge worlds, apocalypse packs, and long-running PvE worlds.

## Quick Info

- Multiplayer: install on the server only
- Client install: optional for multiplayer
- Singleplayer: install on the client, because singleplayer runs an integrated server
- Current targets: NeoForge 1.21.1 and Forge 1.20.1
- All admin commands require OP level 2
- Most major systems can be changed live with commands

## What This Mod Adds

- Custom zombie-class spawning around players during both day and night
- Optional zombie sunlight immunity
- Optional baby zombie spawn control
- Natural and manual horde events
- Random and forced blood moon nights
- Morning day-counter announcements
- Manual apocalypse day control
- Early-world daylight spawn grace period
- Day-based difficulty progression
- Advanced attribute tuning for spawned zombie-class mobs
- Biome and dimension-based spawn and stat behavior
- Temporary spawn pressure relief after player death
- Persistent kill tracking and milestone advancements
- Optional bonus zombie drops, including configurable gunpowder drops
- Optional spawn particles and sounds
- Detailed config comments for easier setup

Zombie-class mobs include:

- Zombie
- Husk
- Drowned
- Zombie Villager

## Core Gameplay Systems

### Constant Threat Spawning

Custom spawn waves can run near survival players all day and night.

You can control:

- how often spawn checks happen
- the chance of a spawn wave
- how many mobs are attempted per wave
- the per-player nearby zombie cap
- spawn range around each player
- minimum spawn distance
- spawn-position attempts per mob
- whether overworld spawns require open sky
- whether baby zombie spawns are allowed

This lets you tune the mod for anything from a tense vanilla-style survival world to a full hardcore apocalypse server.

### Horde Events

Hordes are timed high-pressure zombie surges with their own settings.

You can configure:

- day interval for horde scheduling
- chance a scheduled horde actually starts
- horde duration
- event spawn interval
- horde wave size
- horde spawn multiplier
- on-screen event notifications

Admins can also start or stop hordes manually.

### Blood Moon Nights

Blood moons can happen randomly at night or be forced manually.

During a blood moon:

- spawn pressure increases heavily
- event multipliers stack with normal spawning systems
- players can receive on-screen alerts when notifications are enabled

### Day Counter and Day Control

The mod includes an apocalypse day counter system.

- Show the current day on screen every morning
- Toggle morning day announcements in config or by command
- Check the current day with `/zday status`
- Set the apocalypse day manually with `/zday set <day>`

When the day is changed, apocalypse event scheduling state is reset so horde and blood moon logic stays consistent.

### Early-Day Daylight Spawn Grace

If you want the beginning of a world to be less punishing during daylight, daytime custom spawning can be delayed until a chosen day.

Examples:

- `0` = daytime spawning is active immediately
- `10` = daytime spawning only starts on day 10
- `15` = daytime spawning only starts on day 15

This can be changed in the config or live with command.

### Difficulty Scaling

Zombie pressure can scale as the world progresses.

The mod supports:

- scaling start day
- maximum scaling day
- legacy speed and health scaling
- full attribute scaling through the advanced attribute system

This creates a stronger long-term survival curve instead of flat difficulty.

### Variant, Biome, and Dimension Logic

Spawn composition and stat behavior can change based on where mobs spawn.

Supported behavior includes:

- more husks in desert-like biomes
- more drowned in water-heavy biomes
- mushroom fields as optional safe zones
- optional custom spawning in the Nether and End
- biome and dimension context multipliers for attributes

### Death Cooldown

After a player dies, spawn pressure can be reduced temporarily to avoid repeated instant deaths.

This is useful on servers and harder setups where nonstop pressure can otherwise snowball too hard.

### Stats, Milestones, Drops, and Effects

The mod includes support systems around the main apocalypse gameplay:

- persistent zombie kill tracking
- death cooldown tracking
- optional bonus zombie drops
- configurable gunpowder drops from zombie-class mobs
- optional spawn particles and sounds
- optional debug logging
- zombie kill milestone advancements at 250, 1000, and 3000 kills

## Advanced Attribute System

One of the largest systems in the mod is the attribute pipeline for zombie-class mobs when they enter the world.

Supported attributes:

- Health
- Attack Damage
- Movement Speed
- Armor
- Follow Range
- Knockback Resistance

### Attribute Flow

For each affected mob, values are applied in this order:

1. Start from vanilla base values
2. Apply global base profile
3. Apply per-variant profile if enabled
4. Apply biome and dimension context multipliers if enabled
5. Apply attribute-based day scaling if enabled
6. Apply legacy health and speed scaling compatibility values
7. Clamp to safe limits

### Why This Matters

This lets you build very specific difficulty behavior.

Examples:

- all spawned mobs are slightly faster globally
- husks hit harder than standard zombies
- drowned are tankier in water biomes
- Nether spawns are faster and more aggressive
- late-game mobs detect players from farther away

You can keep the system simple, or tune it deeply for server balance.

## Commands

All commands require OP level 2.

### General

- `/zhelp` - Show built-in help
- `/zburn <true|false>` - Control whether zombies burn in daylight
- `/zkill` - Remove zombie-class entities from loaded levels

### Day Control

- `/zday` - Show current world day status
- `/zday status` - Show current world day and time-of-day
- `/zday set <day>` - Set the apocalypse day counter

### Events

- `/zhorde start` - Start a horde immediately
- `/zhorde stop` - Stop the active horde
- `/zhorde status` - Show horde, blood moon, and multiplier state
- `/zbloodmoon` - Force a blood moon now or queue it for tonight

### Stats

- `/zstats` - Show your own stats or server summary from console
- `/zstats <player>` - Show stats for a specific player
- `/zstats all` - Show server totals
- `/zstats clear` - Clear tracked stats, cooldowns, and milestone progress

### Spawn Settings

- `/zdayspawn status` - Show current spawn settings
- `/zdayspawn enabled <true|false>` - Toggle custom spawning
- `/zdayspawn chance <0.0-1.0>` - Set spawn-wave chance
- `/zdayspawn babychance <0.0-1.0>` - Set baby zombie chance, use `0.0` to disable new baby zombie-class spawns
- `/zdayspawn interval <ticks>` - Set normal spawn interval
- `/zdayspawn eventinterval <ticks>` - Set horde/blood moon spawn interval
- `/zdayspawn amount <1-50>` - Set mobs attempted per wave
- `/zdayspawn attempts <1-40>` - Set spawn-position attempts per mob
- `/zdayspawn max <1-500>` - Set nearby zombie cap per player
- `/zdayspawn range <16-128>` - Set spawn range around players
- `/zdayspawn mindist <8-64>` - Set minimum spawn distance from players
- `/zdayspawn daylightstart <day>` - Delay daytime custom spawning until a chosen day
- `/zdayspawn sky <true|false>` - Require open sky for overworld custom spawns
- `/zdayspawn variants <true|false>` - Toggle zombie variant spawning
- `/zdayspawn nightboost <true|false>` - Toggle night spawn boost
- `/zdayspawn horde <true|false>` - Toggle horde system
- `/zdayspawn hordechance <0.0-1.0>` - Set scheduled horde chance
- `/zdayspawn daycounter <true|false>` - Toggle morning day-counter titles
- `/zdayspawn bloodmoon <true|false>` - Toggle random blood moons
- `/zdayspawn scaling <true|false>` - Toggle day-based scaling
- `/zdayspawn attributes <true|false>` - Toggle advanced attributes
- `/zdayspawn attributescaling <true|false>` - Toggle attribute day scaling
- `/zdayspawn variantprofiles <true|false>` - Toggle per-variant attribute profiles
- `/zdayspawn contextprofiles <true|false>` - Toggle biome/dimension context profiles
- `/zdayspawn biomes <true|false>` - Toggle biome spawn modifiers
- `/zdayspawn nether <true|false>` - Allow custom spawning in the Nether
- `/zdayspawn end <true|false>` - Allow custom spawning in the End
- `/zdayspawn cooldown <true|false>` - Toggle death cooldown system
- `/zdayspawn effects <true|false>` - Toggle spawn particles and sounds
- `/zdayspawn debug <true|false>` - Toggle debug logging

### Scaling

- `/zscaling status` - Show current scaling state

### Live Attribute Tuning

- `/zattr status` - Show attribute system status
- `/zattr keys` - Show key format help
- `/zattr keys all` - Show all supported numeric and toggle keys
- `/zattr get <key>` - Read a numeric attribute value
- `/zattr set <key> <value>` - Set a numeric attribute value
- `/zattr toggle <key> <true|false>` - Change a supported boolean attribute toggle

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

If you are unsure what key to use, run:

- `/zattr keys all`

## Config and Tuning

The config file is designed to be readable and includes comments, safe ranges, and explanations for new users.

You can use it for:

- a more hostile vanilla-style survival world
- a PvE server with escalating danger
- a challenge pack apocalypse setup
- a long-running world with increasing pressure over time
- a softer early game that becomes harder later
- a brutal server where day and night are both dangerous

Most major settings can also be changed live with commands, which makes balancing much easier during active testing.

## Suggested Starting Points

For a lighter survival world:

- delay daylight spawning with `daylightstart`
- keep horde chance low
- keep scaling slow
- keep baby zombie chance low or disabled

For a hardcore apocalypse world:

- enable day and night custom spawning
- enable hordes and blood moons
- enable scaling and advanced attributes
- increase event wave size and event multipliers

## Installation Notes

- Multiplayer servers only need the mod installed on the server.
- Players can join without installing the mod on their client.
- For singleplayer, install it on the client.
- Use the NeoForge file for Minecraft 1.21.1.
- Use the Forge file for Minecraft 1.20.1.
- Back up your world before changing major spawn or scaling settings.

If you want a configurable zombie apocalypse system with constant pressure, escalating events, live admin controls, and deep stat tuning, this mod is built for exactly that.
