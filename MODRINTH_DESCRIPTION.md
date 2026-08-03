# Zombie Apocalypse Addon - Everything Can Be Customized

Turn your Minecraft world into a long-term zombie survival scenario where daytime is no longer safe.

Zombie Apocalypse Addon replaces the usual "survive the night, relax during the day" loop with constant pressure, escalating danger, event surges, and deep server-side control. Zombie-class mobs can spawn around players during both day and night, hordes and blood moons can raise the pressure even higher, and enemy stats can scale as the world gets older.

This is not just "more zombies." It is a configurable zombie survival framework built for servers, challenge worlds, apocalypse packs, and long-running PvE worlds.

## Quick Info

- Multiplayer: install on the server only
- Client install: optional for multiplayer
- Singleplayer: install on the client, because singleplayer runs an integrated server
- Current targets: NeoForge 1.21.1, NeoForge 1.20.1, and Forge 1.20.1
- All admin commands require OP level 2
- Most major systems can be changed live with commands

## What This Mod Adds

- Custom zombie-class spawning around players during both day and night
- Optional zombie sunlight immunity
- Optional block-light spawn protection for torches, lanterns, glowstone, and similar light sources
- Optional baby zombie spawn control
- Optional zombie block breaking with day gating and safety controls
- Optional zombie block placing with one-block bridge and step behavior
- Optional World War Z-style zombie towering with crowd and day gating
- Built-in compatibility controls for modded zombies, AI systems, spawn rules, and equipment
- Natural and manual horde events
- Random and forced blood moon nights
- Morning day-counter announcements
- Manual apocalypse day control
- Early-world daylight spawn grace period
- Day-based difficulty progression
- Advanced attribute tuning for spawned zombie-class mobs
- Biome and dimension-based spawn and stat behavior
- Loaded-chunk-aware custom spawn placement to reduce server stalls
- Temporary spawn pressure relief after player death
- Persistent kill tracking and milestone advancements
- Optional bonus zombie drops, including configurable gunpowder drops
- Optional spawn particles and sounds
- Detailed config comments for easier setup

Vanilla zombie-class mobs include:

- Zombie
- Husk
- Drowned
- Zombie Villager

Modded `Zombie` subclasses are recognized automatically, and known nonstandard zombie entities are supported through optional entity tags.

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
- whether block light should stop custom spawns
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

### Light-Based Spawn Protection

You can optionally make custom spawns respect block light from torches, lanterns, glowstone, and similar light sources.

Examples:

- `-1` = ignore block light and keep the default apocalypse behavior
- `0` = only spawn in complete block darkness
- `7` = classic hostile-mob-style limit

This checks block light only. Sunlight is ignored so daytime spawning can still work.

### Optional Zombie Block Breaking

Zombies can be allowed to break blocks after a chosen apocalypse day.

This system is disabled by default because it can damage bases. If a server owner enables it, they can tune:

- start day
- check interval
- break chance
- break range
- max block hardness
- whether broken blocks drop items
- whether zombies need a target
- whether zombies need to be blocked or chasing a covered target
- whether the vanilla `mobGriefing` gamerule can stop it
- whether containers, modded block entities, tool-required blocks, and light sources are protected

The default hardness limit is meant for soft blocks and wooden doors, while stone, ores, containers, machines, and light blocks stay protected unless you loosen the safety settings.

Quick setup:

- `/zblockbreak dayone` = enable it and make it active immediately
- `/zblockbreak enabled true` = enable it while still respecting the configured start day
- `/zblockbreak startday 10` = let zombies start breaking blocks on day 10

The defaults are intentionally safer: no block breaking unless enabled, no container/machine breaking, no light-source breaking, no tool-required block breaking, and no item drops from broken blocks.

### Optional Zombie Block Placing

Zombie-class mobs can place a safe solid block to cross a one-block gap or make a one-block step while pursuing a target.

This system is disabled by default. If enabled, you can tune:

- start day
- attempt interval and chance
- placed block ID
- lifetime placement limit per zombie
- target requirement and maximum target distance
- whether an obstacle is required
- bridges and steps separately
- fluid and replaceable-block replacement
- whether the vanilla `mobGriefing` gamerule can stop placement

The placed block must be stable, solid, breakable, and free of block entities. Placement stays inside loaded chunks and the world border, cannot overlap entities, and fires normal Forge/NeoForge block-place events so claim and protection mods can cancel it.

Quick setup:

- `/zblockplace dayone` = enable it and make it active immediately
- `/zblockplace enabled true` = enable it while keeping the configured start day
- `/zblockplace startday 15` = let zombies start placing blocks on day 15
- `/zblockplace block minecraft:cobblestone` = choose the block they place
- `/zblockplace limit 8` = limit each zombie to eight placed blocks

The safe defaults keep block placing off, start it on day 15 when enabled, limit each zombie to eight blocks, require an active target and obstacle/gap, respect `mobGriefing`, and protect fluids, plants, snow, containers, and occupied spaces.

### Optional Zombie Towering

Crowded, recognized `Zombie` subclasses can climb over each other and push toward an obstructed, covered, or raised target without placing blocks or creating permanent passenger stacks.

This system is disabled by default. When enabled, it starts on day 20 unless you change the start day. You can tune:

- start day
- attempt interval and chance
- maximum target distance
- nearby zombie count and crowd radius
- vertical and forward boost strength
- maximum height above the target
- whether an obstacle, collision, covered target, or raised target is required

The default safety rules require a valid survival target and at least two other nearby `Zombie` subclasses. Checks are staggered across ticks, chance is evaluated before the bounded entity scan, block clearance is verified before movement, and horizontal speed is capped. Riding, passenger-carrying, swimming, lava-bound, and no-AI zombies cannot tower. Airborne zombies need swarm support before receiving another boost.

Quick setup:

- `/ztower enabled true` = enable towering while keeping the day-20 start
- `/ztower startday 30` = delay towering until day 30
- `/ztower dayone` = enable towering immediately
- `/ztower status` = show the active rules and current day gate

### Mod Compatibility

The compatibility layer recognizes modded zombies without blindly taking control of every mob. Recognized entities can count toward nearby caps, `/zkill`, kill milestones, optional drops, and difficulty scaling. Existing modded weapons and armor are preserved by default.

The addon uses official Forge and NeoForge spawn, mob-griefing, block-break, and block-place hooks so spawn-control and protection mods can reject its actions. It also avoids duplicate AI or difficulty behavior for supported mods that already manage those systems, and respects per-zombie door-breaking permission by default.

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

Use `/zcompat status` to see detected integrations. The config also accepts comma-separated entity IDs for unusual zombie mods that need an explicit include or exclusion.

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
- `/zcleanup` - Remove loaded zombie leftovers and reset apocalypse event state
- `/zcleanup uninstall` - Cleanup plus disable spawning, events, scaling, attributes, block breaking, block placing, towering, sunlight immunity, extra drops, and cooldowns before removal

### Compatibility

- `/zcompat` - Show detected supported mods and compatibility safeguards
- `/zcompat status` - Show detected supported mods and compatibility safeguards
- `/zcompat modded <true|false>` - Toggle automatic modded `Zombie` subclass recognition
- `/zcompat difficulty <true|false>` - Toggle addon difficulty for recognized modded zombies
- `/zcompat ai <true|false>` - Toggle addon AI features for recognized modded `Zombie` subclasses
- `/zcompat spawnrules <true|false>` - Let external spawn-control mods approve or reject addon spawns
- `/zcompat externalai <true|false>` - Avoid duplicate AI behavior from supported mods
- `/zcompat externaldifficulty <true|false>` - Avoid double difficulty scaling from supported mods
- `/zcompat doors <true|false>` - Respect per-zombie door-breaking permission
- `/zcompat equipment <true|false>` - Preserve existing modded weapons and armor

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
- `/zdayspawn maxlight <-1-15>` - Set maximum block light for custom spawns, use `-1` to ignore block light
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

### Block Breaking

- `/zblockbreak` - Show current zombie block-breaking settings
- `/zblockbreak status` - Show current zombie block-breaking settings
- `/zblockbreak dayone` - Enable zombie block breaking and set the start day to `0`
- `/zblockbreak enabled <true|false>` - Toggle zombie block breaking
- `/zblockbreak startday <day>` - Set the day when block breaking can start
- `/zblockbreak interval <ticks>` - Set how often each zombie can attempt block breaking
- `/zblockbreak chance <0.0-1.0>` - Set the chance per scheduled block-breaking check
- `/zblockbreak range <1-4>` - Set how far in front of the zombie it checks
- `/zblockbreak hardness <0.0-50.0>` - Set the maximum block hardness zombies can break
- `/zblockbreak drops <true|false>` - Toggle item drops from zombie-broken blocks
- `/zblockbreak target <true|false>` - Require zombies to have a valid target before breaking
- `/zblockbreak obstacle <true|false>` - Require a blocked path or covered target before breaking
- `/zblockbreak mobgriefing <true|false>` - Make mobGriefing and loader mob-griefing events control this feature
- `/zblockbreak containers <true|false>` - Allow or block breaking chests, furnaces, and block-entity blocks
- `/zblockbreak toolblocks <true|false>` - Allow or block breaking blocks that require the correct tool
- `/zblockbreak lights <true|false>` - Allow or block breaking light-emitting blocks

### Block Placing

- `/zblockplace` - Show current zombie block-placing settings
- `/zblockplace status` - Show current zombie block-placing settings
- `/zblockplace dayone` - Enable zombie block placing and set the start day to `0`
- `/zblockplace enabled <true|false>` - Toggle zombie block placing
- `/zblockplace startday <0-3650>` - Set the day when block placing can start
- `/zblockplace interval <20-72000>` - Set how often each zombie can attempt block placing
- `/zblockplace chance <0.0-1.0>` - Set the chance per scheduled block-placing check
- `/zblockplace block <namespace:id>` - Choose the safe solid block zombies place
- `/zblockplace limit <0-256>` - Set the lifetime placement limit per zombie; `0` is unlimited
- `/zblockplace distance <4-128>` - Set the farthest target distance that allows placement
- `/zblockplace target <true|false>` - Require zombies to have a valid target before placing
- `/zblockplace obstacle <true|false>` - Require a blocked path, covered/raised target, or gap
- `/zblockplace mobgriefing <true|false>` - Make `mobGriefing` and loader events control placement
- `/zblockplace bridges <true|false>` - Toggle filling one-block-deep gaps
- `/zblockplace steps <true|false>` - Toggle placing one-block steps
- `/zblockplace fluids <true|false>` - Allow or block replacing fluid blocks
- `/zblockplace replaceable <true|false>` - Allow or block replacing plants, snow, and similar blocks
- `/zblockplace resetcounts` - Reset lifetime placement counts for loaded zombie-class mobs

### Zombie Towering

- `/ztower` - Show current zombie towering settings
- `/ztower status` - Show current zombie towering settings
- `/ztower dayone` - Enable towering and set the start day to `0`
- `/ztower enabled <true|false>` - Toggle zombie towering
- `/ztower startday <0-3650>` - Set the day when towering can start
- `/ztower interval <5-72000>` - Set how often each zombie can check for towering
- `/ztower chance <0.0-1.0>` - Set the chance per scheduled towering check
- `/ztower distance <4-128>` - Set the farthest target distance that allows towering
- `/ztower crowd <1-16>` - Set the number of other nearby zombies required
- `/ztower radius <0.75-6.0>` - Set the nearby-crowd search radius
- `/ztower vertical <0.1-1.0>` - Set the upward velocity
- `/ztower forward <0.0-0.6>` - Set the target-facing horizontal velocity
- `/ztower height <1-32>` - Set the maximum height above the target
- `/ztower obstacle <true|false>` - Require a collision, barrier, covered target, or raised target

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

The `compatibility` section contains safe defaults, plain-language explanations, exact entity-ID include/exclude fields, and the same main toggles exposed by `/zcompat`.

You can use it for:

- a more hostile vanilla-style survival world
- a PvE server with escalating danger
- a challenge pack apocalypse setup
- a long-running world with increasing pressure over time
- a softer early game that becomes harder later
- a brutal server where day and night are both dangerous
- an optional base-pressure setup where zombies can break selected blocks after a chosen day
- a late-game swarm setup where crowded zombies can tower over defenses after a chosen day

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
- optionally enable `/zblockbreak dayone` or set a later `/zblockbreak startday`
- optionally enable `/ztower enabled true` and tune `/ztower startday`

## Installation Notes

- Multiplayer servers only need the mod installed on the server.
- Players can join without installing the mod on their client.
- For singleplayer, install it on the client.
- Use the NeoForge 1.21.1 file for Minecraft 1.21.1 NeoForge servers.
- Use the NeoForge 1.20.1 file for Minecraft 1.20.1 NeoForge servers.
- Use the Forge 1.20.1 file for Minecraft 1.20.1 Forge servers.
- Back up your world before changing major spawn or scaling settings.
- If custom zombies stop spawning, run `/zdayspawn status`. It warns when the minimum spawn distance cannot fit inside the configured spawn range, which pauses custom spawning until the values are corrected.

If you want a configurable zombie apocalypse system with constant pressure, escalating events, live admin controls, and deep stat tuning, this mod is built for exactly that.
