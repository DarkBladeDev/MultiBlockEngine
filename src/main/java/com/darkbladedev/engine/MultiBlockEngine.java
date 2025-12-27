package com.darkbladedev.engine;

import com.darkbladedev.engine.api.MultiblockAPI;
import com.darkbladedev.engine.api.impl.MultiblockAPIImpl;
import com.darkbladedev.engine.addon.AddonManager;
import com.darkbladedev.engine.command.MultiblockCommand;
import com.darkbladedev.engine.integration.MultiblockExpansion;
import com.darkbladedev.engine.listener.MultiblockListener;
import com.darkbladedev.engine.manager.MultiblockManager;
import com.darkbladedev.engine.model.MultiblockInstance;
import com.darkbladedev.engine.model.MultiblockType;
import com.darkbladedev.engine.parser.MultiblockParser;
import com.darkbladedev.engine.storage.SqlStorage;
import com.darkbladedev.engine.storage.StorageManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collection;
import java.util.List;

import com.darkbladedev.engine.debug.DebugManager;

public class MultiBlockEngine extends JavaPlugin {

    private static final int API_VERSION = 1;

    private static MultiBlockEngine instance;
    private MultiblockManager manager;
    private MultiblockParser parser;
    private StorageManager storage;
    private MultiblockAPIImpl api;
    private DebugManager debugManager;
    private AddonManager addonManager;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("MultiBlockEngine starting...");
        
        // Ensure data folder exists
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        // Save default config
        saveDefaultConfig();

        // Initialize components
        api = new MultiblockAPIImpl();
        manager = new MultiblockManager();
        parser = new MultiblockParser(api);
        storage = new SqlStorage(this);
        storage.init();
        manager.setStorage(storage);
        debugManager = new DebugManager(this);

        addonManager = new AddonManager(this, api);
        manager.setAddonManager(addonManager);

        addonManager.loadAddons();
        
        // Ensure directory exists
        File multiblockDir = new File(getDataFolder(), "multiblocks");
        if (!multiblockDir.exists()) {
            multiblockDir.mkdirs();
            // Create default example if empty
            saveResource("multiblocks/example_portal.yml", false);
        }
        
        // Load definitions
        List<MultiblockType> types = parser.loadAll(multiblockDir);
        for (MultiblockType type : types) {
            try {
                manager.registerType(type);
                getLogger().info("Loaded multiblock: " + type.id());
            } catch (Exception e) {
                getLogger().severe("Failed to register multiblock type: " + type.id() + " Cause: " + e.getMessage());
            }
        }
        
        // Restore persisted instances
        Collection<MultiblockInstance> instances = storage.loadAll();
        for (MultiblockInstance inst : instances) {
            manager.registerInstance(inst);
        }
        getLogger().info("Restored " + instances.size() + " active instances.");

        addonManager.enableAddons();
        
        // Register Listeners
        getServer().getPluginManager().registerEvents(new MultiblockListener(manager), this);
        
        // Register Commands
        MultiblockCommand cmd = new MultiblockCommand(this);
        getCommand("multiblock").setExecutor(cmd);
        getCommand("multiblock").setTabCompleter(cmd);
        
        // Start Ticking
        manager.startTicking(this);
        
        // Register PlaceholderExpansion
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MultiblockExpansion(this).register();
            getLogger().info("Hooked into PlaceholderAPI.");
        }
        
        getLogger().info("MultiBlockEngine enabled with " + types.size() + " types.");
    }

    @Override
    public void onDisable() {
        if (debugManager != null) {
            debugManager.stopAll();
        }
        if (addonManager != null) {
            addonManager.disableAddons();
        }
        if (manager != null) {
            manager.unregisterAll();
        }
        if (storage != null) {
            storage.close();
        }
        getLogger().info("MultiBlockEngine stopping...");
    }
    
    public static MultiBlockEngine getInstance() {
        return instance;
    }

    public static int getApiVersion() {
        return API_VERSION;
    }
    
    public MultiblockManager getManager() {
        return manager;
    }
    
    public MultiblockParser getParser() {
        return parser;
    }
    
    public MultiblockAPI getAPI() {
        return api;
    }
    
    public DebugManager getDebugManager() {
        return debugManager;
    }

    public AddonManager getAddonManager() {
        return addonManager;
    }
}
