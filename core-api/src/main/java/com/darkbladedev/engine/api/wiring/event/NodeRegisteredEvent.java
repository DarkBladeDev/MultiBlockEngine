package com.darkbladedev.engine.api.wiring.event;

import com.darkbladedev.engine.api.wiring.NetworkGraph;
import com.darkbladedev.engine.api.wiring.NetworkNode;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NodeRegisteredEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final NetworkNode node;
    private final NetworkGraph graph;

    public NodeRegisteredEvent(@NotNull NetworkNode node, @NotNull NetworkGraph graph) {
        this.node = node;
        this.graph = graph;
    }

    @NotNull
    public NetworkNode getNode() {
        return node;
    }

    @NotNull
    public NetworkGraph getGraph() {
        return graph;
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

