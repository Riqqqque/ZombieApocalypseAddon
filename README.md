# ZombieApocalypseAddon

ZombieApocalypseAddon is a NeoForge 1.21.1 mod that turns the world into a configurable zombie survival experience.

## Features
- Optional sunlight immunity (sun burn only) for zombies.
- Custom day/night zombie spawning around survival players.
- Horde events with configurable schedule, chance, duration, and spawn intensity.
- Blood moon nights with random or forced triggers.
- Difficulty scaling over in-game days (speed, health, armor, weapons).
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
