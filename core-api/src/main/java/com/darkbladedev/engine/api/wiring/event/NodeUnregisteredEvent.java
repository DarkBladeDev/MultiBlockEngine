package com.darkbladedev.engine.api.wiring.event;

import com.darkbladedev.engine.api.wiring.NetworkNode;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NodeUnregisteredEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final NetworkNode node;

    public NodeUnregisteredEvent(@NotNull NetworkNode node) {
        this.node = node;
    }

    @NotNull
    public NetworkNode getNode() {
        return node;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}

