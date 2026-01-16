package com.darkbladedev.engine.api.port;

import java.util.Set;

public record PortDefinition(
        String id,
        PortDirection direction,
        String type,
        PortBlockRef block,
        Set<String> capabilities
) {
    public PortDefinition {
        capabilities = capabilities == null ? Set.of() : Set.copyOf(capabilities);
    }
}

