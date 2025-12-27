package com.darkbladedev.engine.addon;

import com.darkbladedev.engine.MultiBlockEngine;
import com.darkbladedev.engine.api.MultiblockAPI;
import com.darkbladedev.engine.api.addon.AddonContext;
import com.darkbladedev.engine.api.builder.MultiblockBuilder;
import com.darkbladedev.engine.model.BlockMatcher;
import com.darkbladedev.engine.model.MultiblockType;
import com.darkbladedev.engine.model.action.Action;
import com.darkbladedev.engine.model.condition.Condition;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

public class SimpleAddonContext implements AddonContext {
    private final String addonId;
    private final MultiBlockEngine plugin;
    private final MultiblockAPI api;
    private final Logger logger;

    public SimpleAddonContext(String addonId, MultiBlockEngine plugin, MultiblockAPI api, Logger logger) {
        this.addonId = addonId;
        this.plugin = plugin;
        this.api = api;
        this.logger = logger;
    }

    @Override
    public String getAddonId() {
        return addonId;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getEngineVersion() {
        return MultiBlockEngine.getInstance().getPluginMeta().getVersion();
    }

    @Override
    public int getApiVersion() {
        return MultiBlockEngine.getApiVersion();
    }

    @Override
    public MultiblockAPI getAPI() {
        return api;
    }

    @Override
    public void registerAction(String key, Function<Map<String, Object>, Action> factory) {
        if (!key.startsWith(addonId + ":")) {
            throw new IllegalArgumentException("Action key must start with addon ID prefix: " + addonId + ":");
        }
        api.registerAction(key, factory);
    }

    @Override
    public void registerCondition(String key, Function<Map<String, Object>, Condition> factory) {
        if (!key.startsWith(addonId + ":")) {
            throw new IllegalArgumentException("Condition key must start with addon ID prefix: " + addonId + ":");
        }
        api.registerCondition(key, factory);
    }

    @Override
    public void registerMatcher(String prefix, Function<String, BlockMatcher> factory) {
        if (!addonId.equalsIgnoreCase(prefix)) {
            throw new IllegalArgumentException("Matcher prefix must equal addon ID: " + addonId);
        }
        api.registerMatcher(prefix, factory);
    }

    @Override
    public void registerListener(Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    @Override
    public MultiblockBuilder createMultiblock(String id) {
        String fullId = id.contains(":") ? id : addonId + ":" + id;
        if (!fullId.startsWith(addonId + ":")) {
            throw new IllegalArgumentException("Multiblock id must start with addon ID prefix: " + addonId + ":");
        }
        return new MultiblockBuilder(fullId, addonId);
    }

    @Override
    public void registerMultiblock(MultiblockType type) {
        if (type == null) {
            throw new IllegalArgumentException("type");
        }
        if (!type.id().startsWith(addonId + ":")) {
            throw new IllegalArgumentException("MultiblockType id must start with addon ID prefix: " + addonId + ":");
        }
        api.registerMultiblock(type);
    }

    @Override
    public void runTask(Runnable task) {
        MultiBlockEngine.getInstance().getServer().getScheduler().runTask(MultiBlockEngine.getInstance(), task);
    }

    @Override
    public void runTaskAsync(Runnable task) {
        MultiBlockEngine.getInstance().getServer().getScheduler().runTaskAsynchronously(MultiBlockEngine.getInstance(), task);
    }
}
