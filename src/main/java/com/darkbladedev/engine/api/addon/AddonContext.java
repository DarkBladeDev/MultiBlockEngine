package com.darkbladedev.engine.api.addon;

import com.darkbladedev.engine.api.MultiblockAPI;
import com.darkbladedev.engine.api.builder.MultiblockBuilder;
import com.darkbladedev.engine.model.BlockMatcher;
import com.darkbladedev.engine.model.MultiblockType;
import com.darkbladedev.engine.model.action.Action;
import com.darkbladedev.engine.model.condition.Condition;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

public interface AddonContext {
    String getAddonId();
    Logger getLogger();
    String getEngineVersion();
    int getApiVersion();
    MultiblockAPI getAPI();
    
    void registerAction(String key, Function<Map<String, Object>, Action> factory);
    void registerCondition(String key, Function<Map<String, Object>, Condition> factory);
    void registerMatcher(String prefix, Function<String, BlockMatcher> factory);
    void registerListener(Listener listener);
    MultiblockBuilder createMultiblock(String id);
    void registerMultiblock(MultiblockType type);
    
    void runTask(Runnable task);
    void runTaskAsync(Runnable task);
}
