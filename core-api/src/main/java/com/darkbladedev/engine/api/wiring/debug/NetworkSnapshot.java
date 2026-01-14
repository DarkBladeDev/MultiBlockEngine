package com.darkbladedev.engine.api.wiring.debug;

import com.darkbladedev.engine.api.wiring.NetworkConnection;
import com.darkbladedev.engine.api.wiring.NetworkNode;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public record NetworkSnapshot(
        UUID networkId,
        Collection<NetworkNode> nodes,
        Collection<NetworkConnection> connections,
        Map<String, Object> metrics
) {
}

