# Release Checklist

Use this for every public release.

## Code and versions

- Confirm the intended Minecraft, loader, mapping, and API versions in all three Gradle projects.
- Review the diff for gameplay conflicts, permissions, command completion, config compatibility, and server-only safety.
- Bump the NeoForge 1.21.1 version and both Minecraft 1.20.1 versions.
- Keep old commands and config keys compatible unless a breaking change is clearly documented.

## Verification

- Run `./gradlew clean build --console=plain --warning-mode all`.
- Run the Markdown table check for the repository and wiki.
- Start a dedicated server with each of the three jars and verify it reaches `Done` without mod errors.
- Inspect jar names, descriptors, versions, server-only metadata, and SHA-256 hashes.
- Confirm the three final jars are together in `build/modrinth`.

## Documentation

- Update README versions, commands, installation notes, and current behavior.
- Update the wiki home, command reference, config guide, troubleshooting, and release notes when affected.
- Update the Modrinth and CurseForge descriptions when behavior or support information changes.
- Write a short release changelog containing only features, fixes, and important compatibility notes.

## Publish and verify

- Commit and push the main repository and wiki.
- Create the GitHub tag and release with all three jars.
- Upload all three matching files to Modrinth and CurseForge with the correct loader and game-version metadata.
- Put the latest NeoForge 1.21.1 jar in the configured Prism Launcher test instance.
- Verify public version pages, filenames, hashes, release text, and CI status.
- Remove reproducible Gradle/runtime output after verification while keeping the release jars in `build/modrinth`.
