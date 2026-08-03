# Contributing

Bug reports, compatibility details, documentation fixes, and focused code changes are welcome.

## Before You Start

1. Search the [existing issues](https://github.com/Riqqqque/ZombieApocalypseAddon/issues).
2. Use the matching issue form for bugs, config help, or features.
3. Discuss large behavior changes before writing them.
4. Keep each pull request focused on one problem.

## Development Setup

- Use Java 21 to run the aggregate Gradle build.
- Do not add generated jars, Gradle caches, test worlds, logs, or IDE-specific files.
- Never commit access tokens, server addresses, private logs, or player data.

Build every supported target on Windows:

```powershell
.\gradlew.bat clean build --console=plain --warning-mode all
```

Run the Markdown table check after documentation changes:

```powershell
.\scripts\check-markdown-tables.ps1
```

## Pull Request Checklist

- The change works on every loader/version it touches.
- Existing config keys and commands remain compatible unless the change is explicitly documented.
- New gameplay behavior is configurable, safe by default, and bounded for server performance.
- User-facing config comments and wiki text explain any new setting.
- Tests cover reusable logic where practical.
- The aggregate Gradle build passes.

By submitting a contribution, you confirm it is your work and allow it to be included and distributed as part of Zombie Apocalypse Addon under this project's license.
