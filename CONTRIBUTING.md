# ZombieApocalypseAddon

Server-side-only Minecraft mod shipped for three loader targets:

| Target | Module | Version var | Java |
|---|---|---|---|
| NeoForge 1.21.1 | root | `mod_version` (2.x) | 21 |
| Forge 1.20.1 | `forge1201/` | `forge_mod_version` (1.5.x) | 17 |
| NeoForge 1.20.1 | `neoforge1201/` | `forge_mod_version` (1.5.x) | 17 |

`neoforge1201` reuses `forge1201` sources via `neoforge1201/build.gradle` sourceSets.
Adapter copies live in `forge1201/src/main/java`; everything else is shared.
Adapter files must differ only by loader API calls — any other diff is drift.

## Build / test

Three independent Gradle builds (each has its own `settings.gradle`).

```powershell
# Full build: root jar + tests + both 1.20.1 jars via composite tasks,
# then syncs all three jars into build\modrinth\
.\gradlew.bat clean build --no-daemon --console=plain --warning-mode all

# Focused tests (JUnit 5, root only)
.\gradlew.bat test --tests "ClassName" --console=plain --no-daemon

# Adapter-only compile check
.\gradlew.bat -p forge1201 compileJava
.\gradlew.bat -p neoforge1201 compileJava
```

CI (`.github/workflows/build.yml`) runs `./gradlew build` plus
`scripts/check-markdown-tables.ps1` against the repo and the live wiki clone.

## Conventions

- Pure logic lives in package-private static helpers (see `SpawnMath`,
  `EventSchedule`, `ConfigValidator`, `StatisticsManager`) so it can be
  unit-tested without a running server. Keep it that way.
- `SavedData` subclasses persist world state; `save`/`load` ignore the
  `HolderLookup.Provider` arg on 1.21.1, so tests may pass `null`.
- Command mutating nodes go through `CommandUtil.admin` (permission level 2);
  read-only nodes stay public.
- `Config.set(...)` writes through to the config file; batch changes inside
  `Config.edit(() -> ...)`.
- Feature toggles (`/za <feature> on`) must load working defaults via
  `FeaturePresets` — never just flip the enable flag.

## Verified invariants (do not regress)

- `/zday set` resets persisted horde/blood-moon scheduling state.
- `/zstats clear` revokes online advancements and queues offline resets.
- Block breaking / placing / towering are independent, off by default.
- Towering never triggers in ordinary ground combat or normal jumps; tower
  riders/roots never spawn inside floors or ceilings; dismounts require
  stable, collision-free, reachable ground.
- NeoForge 1.21.1 jar `pack.mcmeta` uses resource-pack format 34; the 1.20.1
  jars use 15 and share `forge1201`'s pack.mcmeta on purpose.
