package com.darkbladedev.engine.addon;

import com.darkbladedev.engine.MultiBlockEngine;
import com.darkbladedev.engine.api.MultiblockAPI;
import com.darkbladedev.engine.api.addon.AddonException;
import com.darkbladedev.engine.api.addon.MultiblockAddon;
import com.darkbladedev.engine.api.addon.Version;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;

public class AddonManager {

    public enum AddonState {
        DISCOVERED,
        LOADED,
        ENABLED,
        FAILED,
        DISABLED
    }

    private record DiscoveredAddon(File file, AddonMetadata metadata) {}

    private record LoadedAddon(
        AddonMetadata metadata,
        MultiblockAddon addon,
        URLClassLoader classLoader,
        Logger logger,
        Path dataFolder
    ) {}

    private final MultiBlockEngine plugin;
    private final MultiblockAPI api;
    private final File addonFolder;
    private final AddonDataDirectorySystem dataDirectorySystem;
    private final AddonServiceRegistry serviceRegistry;
    private final AddonDependencyResolver dependencyResolver;
    private final Map<String, DiscoveredAddon> discoveredAddons = new HashMap<>();
    private final Map<String, LoadedAddon> loadedAddons = new HashMap<>();
    private final Map<String, AddonState> states = new HashMap<>();
    private final ArrayDeque<String> enableOrder = new ArrayDeque<>();
    private List<String> resolvedOrder = List.of();

    public AddonManager(MultiBlockEngine plugin, MultiblockAPI api) {
        this.plugin = plugin;
        this.api = api;
        this.addonFolder = new File(plugin.getDataFolder(), "addons");
        this.dataDirectorySystem = new AddonDataDirectorySystem(plugin, this.addonFolder.toPath());
        this.serviceRegistry = new AddonServiceRegistry();
        this.dependencyResolver = new AddonDependencyResolver();
    }

    public void loadAddons() {
        if (!addonFolder.exists()) {
            addonFolder.mkdirs();
        }

        try {
            dataDirectorySystem.ensureRootDirectory();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[MultiBlockEngine][AddonFS] Failed to initialize addons root folder: " + addonFolder.getAbsolutePath(), e);
            return;
        }

        File[] files = addonFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null) return;

        Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        Map<String, List<DiscoveredAddon>> candidates = new HashMap<>();

        for (File file : files) {
            try {
                AddonMetadata metadata = readMetadata(file);
                if (metadata == null) {
                    continue;
                }

                candidates.computeIfAbsent(metadata.id(), k -> new ArrayList<>()).add(new DiscoveredAddon(file, metadata));
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load addon from file: " + file.getName(), e);
            }
        }

        discoveredAddons.clear();
        loadedAddons.clear();
        states.clear();
        enableOrder.clear();
        resolvedOrder = List.of();

        for (Map.Entry<String, List<DiscoveredAddon>> entry : candidates.entrySet()) {
            String id = entry.getKey();
            List<DiscoveredAddon> list = entry.getValue();
            if (list.size() > 1) {
                StringBuilder sb = new StringBuilder();
                for (DiscoveredAddon d : list) {
                    if (!sb.isEmpty()) sb.append(", ");
                    sb.append(d.file().getName()).append("@").append(d.metadata().version());
                }
                plugin.getLogger().severe("[MultiBlockEngine] Addon " + id + " FAILED\nReason: Multiple versions detected (remove duplicates): " + sb);
                states.put(id, AddonState.FAILED);
                continue;
            }

            DiscoveredAddon discovered = list.get(0);
            discoveredAddons.put(id, discovered);
            states.put(id, AddonState.DISCOVERED);
        }

        Map<String, AddonMetadata> metadataById = new HashMap<>();
        for (Map.Entry<String, DiscoveredAddon> e : discoveredAddons.entrySet()) {
            metadataById.put(e.getKey(), e.getValue().metadata());
        }

        AddonDependencyResolver.Resolution resolution = dependencyResolver.resolve(metadataById);
        for (String warning : resolution.warnings()) {
            plugin.getLogger().warning(warning);
        }
        for (Map.Entry<String, String> fail : resolution.failures().entrySet()) {
            states.put(fail.getKey(), AddonState.FAILED);
            plugin.getLogger().severe("[MultiBlockEngine] Addon " + fail.getKey() + " FAILED\nReason: " + fail.getValue());
        }

        resolvedOrder = resolution.loadOrder();
        for (String id : resolvedOrder) {
            if (states.getOrDefault(id, AddonState.DISABLED) == AddonState.FAILED) {
                continue;
            }

            DiscoveredAddon discovered = discoveredAddons.get(id);
            if (discovered == null) {
                continue;
            }

            try {
                loadAddon(discovered);
            } catch (Exception e) {
                failAddon(id, AddonException.Phase.LOAD, "Unhandled exception during addon load", e, true);
            }
        }
    }

    public AddonState getState(String addonId) {
        return states.getOrDefault(addonId, AddonState.DISABLED);
    }

    public void failAddon(String addonId, AddonException.Phase phase, String message, Throwable cause, boolean fatal) {
        if (addonId == null || addonId.isBlank()) {
            addonId = "unknown";
        }

        String header = "[MultiBlockEngine][Addon:" + addonId + "][" + phase.name() + "] ";

        if (cause == null) {
            plugin.getLogger().severe(header + message);
        } else if (shouldLogStacktrace(cause)) {
            plugin.getLogger().log(Level.SEVERE, header + message + " Cause: " + cause.getClass().getSimpleName() + ": " + cause.getMessage(), cause);
        } else {
            plugin.getLogger().severe(header + message + " Cause: " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
        }

        boolean markFailed = fatal || phase == AddonException.Phase.LOAD || phase == AddonException.Phase.ENABLE;
        if (markFailed) {
            states.put(addonId, AddonState.FAILED);
        }

        LoadedAddon loaded = loadedAddons.get(addonId);
        if (fatal && loaded != null) {
            try {
                loaded.addon().onDisable();
            } catch (Throwable t) {
                plugin.getLogger().log(Level.SEVERE, header + "Error during disable after failure", t);
            }
        }
    }

    private void loadAddon(DiscoveredAddon discovered) throws IOException {
        AddonMetadata metadata = discovered.metadata();
        String addonId = metadata.id();
        if (states.getOrDefault(addonId, AddonState.DISABLED) == AddonState.FAILED) {
            return;
        }

        URL[] urls = {discovered.file().toURI().toURL()};
        URLClassLoader loader = new URLClassLoader(urls, plugin.getClass().getClassLoader());
        Logger addonLogger = Logger.getLogger("MultiBlockEngine-Addon-" + addonId);
        addonLogger.setParent(plugin.getLogger());

        MultiblockAddon addon;
        try {
            Class<?> clazz = loader.loadClass(metadata.mainClass());
            if (!MultiblockAddon.class.isAssignableFrom(clazz)) {
                failAddon(addonId, AddonException.Phase.LOAD, "Main class does not implement MultiblockAddon: " + metadata.mainClass(), null, true);
                close(loader);
                return;
            }
            addon = (MultiblockAddon) clazz.getDeclaredConstructor().newInstance();
        } catch (Throwable t) {
            failAddon(addonId, AddonException.Phase.LOAD, "Failed to instantiate addon main class: " + metadata.mainClass(), t, true);
            close(loader);
            return;
        }

        String reportedId;
        try {
            reportedId = Objects.requireNonNull(addon.getId(), "addon.getId()");
        } catch (Throwable t) {
            failAddon(addonId, AddonException.Phase.LOAD, "Addon getId() failed", t, true);
            close(loader);
            return;
        }

        if (!reportedId.equals(addonId)) {
            failAddon(addonId, AddonException.Phase.LOAD, "Addon id mismatch. addon.properties=" + addonId + " getId()=" + reportedId, null, true);
            close(loader);
            return;
        }

        String reportedVersion;
        try {
            reportedVersion = Objects.requireNonNull(addon.getVersion(), "addon.getVersion()");
        } catch (Throwable t) {
            failAddon(addonId, AddonException.Phase.LOAD, "Addon getVersion() failed", t, true);
            close(loader);
            return;
        }

        if (!reportedVersion.trim().equals(metadata.version().raw())) {
            failAddon(addonId, AddonException.Phase.LOAD, "Addon version mismatch. addon.properties=" + metadata.version().raw() + " getVersion()=" + reportedVersion.trim(), null, true);
            close(loader);
            return;
        }

        Path dataFolder;
        try {
            dataFolder = dataDirectorySystem.ensureAddonDataFolder(addonId);
        } catch (Exception e) {
            Path failedPath;
            try {
                String folderName = AddonDataDirectorySystem.normalizeAddonFolderName(addonId);
                failedPath = addonFolder.toPath().resolve(folderName).normalize();
            } catch (Exception ignored) {
                failedPath = addonFolder.toPath();
            }
            dataDirectorySystem.logFs(addonId, "LOAD", failedPath, e, "addon failed");
            failAddon(addonId, AddonException.Phase.LOAD, "Failed to prepare addon data folder", e, true);
            close(loader);
            return;
        }

        SimpleAddonContext context = new SimpleAddonContext(addonId, plugin, api, addonLogger, dataFolder, this, serviceRegistry);
        try {
            addon.onLoad(context);
        } catch (AddonException e) {
            failAddon(addonId, AddonException.Phase.LOAD, e.getMessage(), e.getCause(), e.isFatal());
            close(loader);
            return;
        } catch (Throwable t) {
            failAddon(addonId, AddonException.Phase.LOAD, "Unhandled exception during onLoad", t, true);
            close(loader);
            return;
        }

        loadedAddons.put(addonId, new LoadedAddon(metadata, addon, loader, addonLogger, dataFolder));
        states.put(addonId, AddonState.LOADED);
        plugin.getLogger().info("[MultiBlockEngine][Addon:" + addonId + "][LOAD] Loaded v" + metadata.version());
    }

    private AddonMetadata readMetadata(File file) throws IOException {
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("addon.properties");
            if (entry == null) {
                plugin.getLogger().warning("[MultiBlockEngine][AddonLoader] Skipping " + file.getName() + ": missing addon.properties");
                return null;
            }

            Properties props = new Properties();
            try (InputStream in = jar.getInputStream(entry)) {
                props.load(in);
            }

            String id = trimToNull(props.getProperty("id"));
            String versionStr = trimToNull(props.getProperty("version"));
            String main = trimToNull(props.getProperty("main"));

            if (main == null) {
                Attributes attributes = jar.getManifest() != null ? jar.getManifest().getMainAttributes() : null;
                if (attributes != null) {
                    main = trimToNull(attributes.getValue("Multiblock-Addon-Main"));
                }
            }

            if (id == null || versionStr == null || main == null) {
                plugin.getLogger().warning("[MultiBlockEngine][AddonLoader] Skipping " + file.getName() + ": addon.properties requires id, version, main");
                return null;
            }

            if (!id.matches("[a-z0-9][a-z0-9_\\-]*(?::[a-z0-9][a-z0-9_\\-]*)?")) {
                plugin.getLogger().warning("[MultiBlockEngine][AddonLoader] Skipping " + file.getName() + ": invalid id '" + id + "'");
                return null;
            }

            Version version;
            try {
                version = Version.parse(versionStr);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[MultiBlockEngine][AddonLoader] Skipping " + file.getName() + ": invalid version '" + versionStr + "'");
                return null;
            }

            int apiVersion;
            String apiStr = trimToNull(props.getProperty("api"));
            if (apiStr == null) {
                apiStr = trimToNull(props.getProperty("apiVersion"));
            }

            if (apiStr == null) {
                plugin.getLogger().warning("[MultiBlockEngine][AddonLoader] Skipping " + file.getName() + ": missing api");
                return null;
            }

            try {
                apiVersion = Integer.parseInt(apiStr);
            } catch (NumberFormatException ignored) {
                plugin.getLogger().warning("[MultiBlockEngine][AddonLoader] Skipping " + file.getName() + ": invalid api version '" + apiStr + "'");
                return null;
            }

            Map<String, Version> required = parseDependencyMap(id, trimToNull(props.getProperty("depends.required")), file.getName());
            Map<String, Version> optional = parseDependencyMap(id, trimToNull(props.getProperty("depends.optional")), file.getName());

            String legacy = trimToNull(props.getProperty("depends"));
            if (legacy != null && required.isEmpty()) {
                Version min = Version.parse("0.0.0");
                Map<String, Version> legacyReq = new HashMap<>();
                for (String part : legacy.split("[,; ]+")) {
                    String dep = trimToNull(part);
                    if (dep == null) continue;
                    if (!dep.matches("[a-z0-9][a-z0-9_\\-]*(?::[a-z0-9][a-z0-9_\\-]*)?")) {
                        plugin.getLogger().warning("[MultiBlockEngine][AddonLoader] Invalid legacy depends entry '" + dep + "' in " + id);
                        continue;
                    }
                    if (dep.equals(id)) {
                        plugin.getLogger().warning("[MultiBlockEngine][AddonLoader] Ignoring self-dependency in " + id);
                        continue;
                    }
                    legacyReq.put(dep, min);
                }
                required = Map.copyOf(legacyReq);
            }

            if (!required.isEmpty() && !optional.isEmpty()) {
                for (String depId : required.keySet()) {
                    if (optional.containsKey(depId)) {
                        plugin.getLogger().warning("[MultiBlockEngine][AddonLoader] Optional dependency '" + depId + "' overridden by required in " + id);
                    }
                }
                Map<String, Version> filteredOpt = new HashMap<>(optional);
                filteredOpt.keySet().removeAll(required.keySet());
                optional = Map.copyOf(filteredOpt);
            }

            List<String> dependsIds = new ArrayList<>(required.keySet());
            dependsIds.addAll(optional.keySet());
            dependsIds = List.copyOf(dependsIds);

            return new AddonMetadata(id, version, apiVersion, main, required, optional, dependsIds);
        }
    }

    private Map<String, Version> parseDependencyMap(String ownerId, String raw, String fileName) {
        if (raw == null || raw.isBlank()) {
            return Map.of();
        }

        Map<String, Version> map = new HashMap<>();
        for (String part : raw.split("[,; ]+")) {
            String token = trimToNull(part);
            if (token == null) continue;

            int idx = token.indexOf(">=");
            if (idx < 1 || idx + 2 >= token.length()) {
                throw new IllegalArgumentException("Invalid addon.properties in " + ownerId + ": Invalid dependency format: " + token + " (expected <id>>=<version>)");
            }

            String depId = trimToNull(token.substring(0, idx));
            String verStr = trimToNull(token.substring(idx + 2));

            if (depId == null || verStr == null) {
                throw new IllegalArgumentException("Invalid addon.properties in " + ownerId + ": Invalid dependency format: " + token + " (expected <id>>=<version>)");
            }

            if (!depId.matches("[a-z0-9][a-z0-9_\\-]*(?::[a-z0-9][a-z0-9_\\-]*)?")) {
                throw new IllegalArgumentException("Invalid addon.properties in " + ownerId + ": Invalid dependency id: " + depId);
            }

            if (depId.equals(ownerId)) {
                throw new IllegalArgumentException("Invalid addon.properties in " + ownerId + ": Self dependency not allowed");
            }

            Version min;
            try {
                min = Version.parse(verStr);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid addon.properties in " + ownerId + ": Invalid dependency version: " + depId + ">=" + verStr);
            }

            if (map.containsKey(depId)) {
                plugin.getLogger().warning("[MultiBlockEngine][AddonLoader] Duplicate dependency '" + depId + "' in " + ownerId + " (" + fileName + ") - last one wins");
            }
            map.put(depId, min);
        }

        return Map.copyOf(map);
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static void close(URLClassLoader loader) {
        try {
            loader.close();
        } catch (IOException ignored) {
        }
    }

    public void enableAddons() {
        for (String id : resolvedOrder) {
            LoadedAddon loaded = loadedAddons.get(id);
            if (loaded == null) continue;
            if (states.getOrDefault(id, AddonState.DISABLED) != AddonState.LOADED) continue;

            String missing = missingRequiredEnabledDependencies(loaded.metadata());
            if (missing != null) {
                plugin.getLogger().severe("[MultiBlockEngine] Addon " + id + " FAILED\nReason: " + missing);
                states.put(id, AddonState.FAILED);
                close(loaded.classLoader());
                continue;
            }

            try {
                loaded.addon().onEnable();
                states.put(id, AddonState.ENABLED);
                enableOrder.addLast(id);
                plugin.getLogger().info("[MultiBlockEngine][Addon:" + id + "][ENABLE] Enabled");
            } catch (AddonException e) {
                failAddon(id, AddonException.Phase.ENABLE, e.getMessage(), e.getCause(), e.isFatal());
                close(loaded.classLoader());
            } catch (Throwable t) {
                failAddon(id, AddonException.Phase.ENABLE, "Unhandled exception during onEnable", t, true);
                close(loaded.classLoader());
            }
        }
    }

    public void disableAddons() {
        while (!enableOrder.isEmpty()) {
            String id = enableOrder.removeLast();
            LoadedAddon loaded = loadedAddons.get(id);
            if (loaded == null) continue;

            try {
                loaded.addon().onDisable();
                plugin.getLogger().info("[MultiBlockEngine][Addon:" + id + "][DISABLE] Disabled");
            } catch (Throwable t) {
                failAddon(id, AddonException.Phase.DISABLE, "Unhandled exception during onDisable", t, false);
            }

            states.put(id, AddonState.DISABLED);
            close(loaded.classLoader());
        }

        for (String id : new HashSet<>(loadedAddons.keySet())) {
            LoadedAddon loaded = loadedAddons.remove(id);
            if (loaded != null) {
                close(loaded.classLoader());
            }
            states.putIfAbsent(id, AddonState.DISABLED);
        }
    }

    private String missingRequiredEnabledDependencies(AddonMetadata meta) {
        List<String> missing = new ArrayList<>();

        for (String depId : meta.requiredDependencies().keySet()) {
            if (states.getOrDefault(depId, AddonState.DISABLED) != AddonState.ENABLED) {
                Version min = meta.requiredDependencies().get(depId);
                missing.add(depId + " >=" + min);
            }
        }

        if (missing.isEmpty()) {
            return null;
        }

        return "Missing required dependency " + String.join(", ", missing);
    }

    private static boolean shouldLogStacktrace(Throwable t) {
        if (t == null) return false;
        return !(t instanceof IllegalArgumentException) && !(t instanceof IOException);
    }
}
