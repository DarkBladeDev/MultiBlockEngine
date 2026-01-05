package com.darkbladedev.engine.listener;

import com.darkbladedev.engine.api.event.MultiblockBreakEvent;
import com.darkbladedev.engine.MultiBlockEngine;
import com.darkbladedev.engine.api.addon.AddonException;
import com.darkbladedev.engine.api.logging.CoreLogger;
import com.darkbladedev.engine.api.logging.LogKv;
import com.darkbladedev.engine.api.logging.LogLevel;
import com.darkbladedev.engine.api.logging.LogPhase;
import com.darkbladedev.engine.api.logging.LogScope;
import com.darkbladedev.engine.api.wrench.WrenchContext;
import com.darkbladedev.engine.api.wrench.WrenchDispatcher;
import com.darkbladedev.engine.api.wrench.WrenchResult;
import com.darkbladedev.engine.manager.MultiblockManager;
import com.darkbladedev.engine.model.MultiblockInstance;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.event.Event;

public class MultiblockListener implements Listener {

    private final MultiblockManager manager;
    private final Consumer<Event> eventCaller;
    private final WrenchDispatcher wrenchDispatcher;

    public MultiblockListener(MultiblockManager manager) {
        this(manager, Bukkit.getPluginManager()::callEvent, null);
    }

    public MultiblockListener(MultiblockManager manager, Consumer<Event> eventCaller) {
        this(manager, eventCaller, null);
    }

    public MultiblockListener(MultiblockManager manager, WrenchDispatcher wrenchDispatcher) {
        this(manager, Bukkit.getPluginManager()::callEvent, wrenchDispatcher);
    }

    public MultiblockListener(MultiblockManager manager, Consumer<Event> eventCaller, WrenchDispatcher wrenchDispatcher) {
        this.manager = manager;
        this.eventCaller = eventCaller;
        this.wrenchDispatcher = wrenchDispatcher;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (wrenchDispatcher == null) {
            return;
        }

        WrenchContext ctx = new WrenchContext(
                event.getPlayer(),
                event.getClickedBlock(),
                event.getAction(),
                event.getItem(),
                event.getHand()
        );

        WrenchResult result = wrenchDispatcher.dispatch(ctx);
        if (result != null && result.cancelEvent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Optional<MultiblockInstance> instanceOpt = manager.getInstanceAt(block.getLocation());
        if (instanceOpt.isPresent()) {
            MultiblockInstance instance = instanceOpt.get();

            MultiblockBreakEvent mbEvent = new MultiblockBreakEvent(instance, event.getPlayer());
            eventCaller.accept(mbEvent);
            if (mbEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }
            
            // Execute Break Actions
            for (com.darkbladedev.engine.model.action.Action action : instance.type().onBreakActions()) {
                executeActionSafely("BREAK", action, instance, null);
            }
            
            manager.destroyInstance(instance);
            event.getPlayer().sendMessage(Component.textOfChildren(
                    Component.text("Structure destroyed: ", NamedTextColor.RED),
                    Component.text(instance.type().id(), NamedTextColor.WHITE)
            ));
        }
    }

    private void executeActionSafely(String runtimePhase, com.darkbladedev.engine.model.action.Action action, MultiblockInstance instance, Player player) {
        try {
            if (player != null) {
                action.execute(instance, player);
            } else {
                action.execute(instance);
            }
        } catch (Throwable t) {
            String ownerId = action != null ? action.ownerId() : null;
            String typeKey = action != null ? action.typeKey() : null;

            String actionName = "unknown";
            if (typeKey != null && !typeKey.isBlank()) {
                int idx = typeKey.lastIndexOf(':');
                actionName = idx >= 0 ? typeKey.substring(idx + 1) : typeKey;
            } else if (action != null) {
                actionName = action.getClass().getSimpleName();
            }

            Object counter = instance != null ? instance.getVariable("counter") : null;
            String msg = "[" + runtimePhase + "] Action '" + actionName + "' failed Context: counter=" + counter + " Multiblock=" + (instance != null ? instance.type().id() : "unknown") + " Execution continued";

            if (ownerId != null && !ownerId.isBlank() && MultiBlockEngine.getInstance().getAddonManager() != null) {
                MultiBlockEngine.getInstance().getAddonManager().failAddon(ownerId, AddonException.Phase.RUNTIME, msg, t, false);
            } else {
                CoreLogger core = MultiBlockEngine.getInstance().getLoggingManager() != null ? MultiBlockEngine.getInstance().getLoggingManager().core() : null;
                if (core != null) {
                    core.logInternal(new LogScope.Core(), LogPhase.RUNTIME, LogLevel.ERROR, msg, t, new LogKv[] {
                        LogKv.kv("phase", runtimePhase),
                        LogKv.kv("multiblock", instance != null ? instance.type().id() : "unknown"),
                        LogKv.kv("action", actionName)
                    }, Set.of());
                } else {
                    MultiBlockEngine.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "[Runtime] " + msg + " Cause: " + t.getClass().getSimpleName() + ": " + t.getMessage(), t);
                }
            }
        }
    }
}
