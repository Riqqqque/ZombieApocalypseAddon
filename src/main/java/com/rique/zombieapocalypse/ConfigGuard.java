package com.rique.zombieapocalypse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

/**
 * Undoes config wipes caused by the loader's file watcher.
 *
 * Forge and NeoForge reload the config file whenever it changes on disk. When a text editor saves
 * the file while the game is running, the watcher can read it half written, see most settings as
 * missing, "correct" them to their defaults, and save that over the file. The loader backs the
 * file up before any correction, so a new backup plus settings that jumped straight to their
 * defaults is a reliable sign of a wipe. The previous values are written back a moment later,
 * once the editor has finished.
 */
public final class ConfigGuard {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long RESTORE_DELAY_MILLIS = 1500L;

    private static volatile Map<List<String>, Object> lastValues = Map.of();
    private static volatile long lastBackupTime;

    private ConfigGuard() {
    }

    /** Records the starting values after the loader first loads the config. */
    public static void loaded(Path configFile) {
        lastValues = Config.currentValues();
        lastBackupTime = newestBackupTime(configFile);
    }

    /** Records values the mod just saved, so they are not mistaken for a wipe. */
    public static void saved() {
        if (Config.isLoaded()) {
            lastValues = Config.currentValues();
        }
    }

    /** Checks a loader reload for settings that were reset to defaults by a correction. */
    public static void reloaded(Path configFile) {
        Map<List<String>, Object> before = lastValues;
        Map<List<String>, Object> after = Config.currentValues();
        long backupTime = newestBackupTime(configFile);
        boolean loaderCorrected = backupTime > lastBackupTime;
        lastValues = after;
        lastBackupTime = Math.max(lastBackupTime, backupTime);

        if (loaderCorrected) {
            Map<List<String>, Object> wiped = wipedSettings(before, after, Config.defaultValues());
            if (!wiped.isEmpty()) {
                scheduleRestore(wiped, 1);
                return;
            }
        }
        announceEdits(before, after);
    }

    /** Called after the mod applies a hand edit it picked up from the config file itself. */
    public static void fileEdited() {
        Map<List<String>, Object> before = lastValues;
        Map<List<String>, Object> after = Config.currentValues();
        lastValues = after;
        announceEdits(before, after);
    }

    /** Tells operators which settings a hand edit of the file changed, so they know it took effect. */
    private static void announceEdits(Map<List<String>, Object> before, Map<List<String>, Object> after) {
        if (before.isEmpty()) {
            return;
        }
        List<String> changes = describeChanges(before, after);
        if (changes.isEmpty()) {
            return;
        }
        String message = "Applied " + changes.size() + (changes.size() == 1 ? " change" : " changes")
                + " from the config file: " + String.join(", ", changes.subList(0, Math.min(5, changes.size())))
                + (changes.size() > 5 ? ", and " + (changes.size() - 5) + " more" : "") + '.';
        LOGGER.info("[ZombieApocalypse] {}", message);
        Config.notifyOperators(message);
    }

    static List<String> describeChanges(Map<List<String>, Object> before, Map<List<String>, Object> after) {
        List<String> changes = new java.util.ArrayList<>();
        after.forEach((path, value) -> {
            if (before.containsKey(path) && !sameValue(before.get(path), value)) {
                changes.add(path.get(path.size() - 1) + " " + before.get(path) + " -> " + value);
            }
        });
        return changes;
    }

    private static void scheduleRestore(Map<List<String>, Object> wiped, int attempt) {
        CompletableFuture.delayedExecutor(RESTORE_DELAY_MILLIS * attempt, TimeUnit.MILLISECONDS)
                .execute(() -> Config.runOnServerThread(() -> restore(wiped, attempt)));
    }

    private static void restore(Map<List<String>, Object> wiped, int attempt) {
        Map<List<String>, Object> current = Config.currentValues();
        Map<List<String>, Object> defaults = Config.defaultValues();
        Map<List<String>, Object> restore = stillWiped(wiped, current, defaults);
        if (restore.isEmpty()) {
            return;
        }

        try {
            Config.setAll(restore);
        } catch (RuntimeException e) {
            if (attempt < 3) {
                scheduleRestore(wiped, attempt + 1);
                return;
            }
            LOGGER.error("[ZombieApocalypse] Could not restore {} config settings after the loader reset them: {}",
                    restore.size(), describe(restore.keySet()), e);
            Config.notifyOperators("The loader reset " + restore.size() + " config settings to defaults and they"
                    + " could not be put back automatically. Check the server log for the list.");
            return;
        }

        String message = "The loader reset " + restore.size() + (restore.size() == 1 ? " setting" : " settings")
                + " in the config file to defaults, and they were put back. This happens when the file has a syntax"
                + " error or is read halfway through a save. If you just edited it, check that your edit is still"
                + " there; the loader keeps the old file as a .bak in the config folder.";
        LOGGER.warn("[ZombieApocalypse] {} Restored: {}", message, describe(restore.keySet()));
        Config.notifyOperators(message);
    }

    /**
     * Settings that held a custom value before the reload and their default value after it.
     * The map holds the value to put back.
     */
    static Map<List<String>, Object> wipedSettings(
            Map<List<String>, Object> before,
            Map<List<String>, Object> after,
            Map<List<String>, Object> defaults) {
        Map<List<String>, Object> wiped = new LinkedHashMap<>();
        before.forEach((path, previous) -> {
            if (!defaults.containsKey(path) || !after.containsKey(path)) {
                return;
            }
            Object defaultValue = defaults.get(path);
            if (!sameValue(previous, defaultValue) && sameValue(after.get(path), defaultValue)) {
                wiped.put(path, previous);
            }
        });
        return wiped;
    }

    /** Leaves out settings that changed again since the wipe, for example when the editor's save landed. */
    static Map<List<String>, Object> stillWiped(
            Map<List<String>, Object> wiped,
            Map<List<String>, Object> current,
            Map<List<String>, Object> defaults) {
        Map<List<String>, Object> restore = new LinkedHashMap<>();
        wiped.forEach((path, previous) -> {
            if (current.containsKey(path) && sameValue(current.get(path), defaults.get(path))) {
                restore.put(path, previous);
            }
        });
        return restore;
    }

    public static boolean sameValue(Object first, Object second) {
        if (first instanceof Number a && second instanceof Number b) {
            return Double.compare(a.doubleValue(), b.doubleValue()) == 0;
        }
        return Objects.equals(first, second);
    }

    private static String describe(Iterable<List<String>> paths) {
        StringBuilder text = new StringBuilder();
        for (List<String> path : paths) {
            if (!text.isEmpty()) {
                text.append(", ");
            }
            text.append(String.join(".", path));
        }
        return text.toString();
    }

    /** Newest loader backup of this config, such as zombieapocalypseaddon-common-1.toml.bak. */
    static long newestBackupTime(Path configFile) {
        if (configFile == null || configFile.getParent() == null) {
            return 0L;
        }
        String name = configFile.getFileName().toString();
        String base = name.endsWith(".toml") ? name.substring(0, name.length() - 5) : name;
        try (Stream<Path> files = Files.list(configFile.getParent())) {
            return files
                    .filter(file -> isBackupName(file.getFileName().toString(), base))
                    .mapToLong(ConfigGuard::modifiedTime)
                    .max()
                    .orElse(0L);
        } catch (IOException e) {
            return 0L;
        }
    }

    static boolean isBackupName(String fileName, String base) {
        return fileName.startsWith(base + "-") && fileName.endsWith(".bak");
    }

    private static long modifiedTime(Path file) {
        try {
            return Files.getLastModifiedTime(file).toMillis();
        } catch (IOException e) {
            return 0L;
        }
    }
}
