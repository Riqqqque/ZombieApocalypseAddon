# ZombieApocalypseAddon

ZombieApocalypseAddon is a NeoForge 1.21.1 mod that turns the world into a configurable zombie survival experience.

## Features
- Optional sunlight immunity (sun burn only) for zombies.
- Custom day/night zombie spawning around survival players.
- Horde events with configurable schedule, chance, duration, and spawn intensity.
- Blood moon nights with random or forced triggers.
- Difficulty scaling over in-game days (speed, health, armor, weapons).
- Advanced spawned-mob attribute tuning for health, attack, speed, armor, follow range, and knockback resistance.
- Per-variant attribute profiles (zombie, husk, drowned, zombie villager).
- Biome and dimension attribute context multipliers (desert, water, mushroom, nether, end).
- Biome-aware variant selection (husk and drowned weighting).
- Death cooldown that temporarily reduces spawn pressure after player death.
- Persistent kill statistics and cooldown data.
- Runtime admin command controls.

## Commands (OP level 2)
- `/zhelp`
- `/zburn <true|false>`
- `/zkill`
- `/zhorde <start|stop|status>`
- `/zbloodmoon`
- `/zstats [player|all|clear]`
- `/zscaling status`
- `/zdayspawn status`
- `/zdayspawn attributes <true|false>`
- `/zdayspawn attributescaling <true|false>`
- `/zdayspawn variantprofiles <true|false>`
- `/zdayspawn contextprofiles <true|false>`
- `/zattr status`
- `/zattr keys [all]`
- `/zattr get <key>`
- `/zattr set <key> <value>`
- `/zattr toggle <key> <true|false>`
- `/zdayspawn <setting> <value>`

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

## Development
- Build: `./gradlew build`
- Full clean build: `./gradlew clean build`
- Run tests: `./gradlew test`
