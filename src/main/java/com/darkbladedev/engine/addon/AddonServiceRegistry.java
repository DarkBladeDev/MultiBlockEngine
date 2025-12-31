package com.darkbladedev.engine.addon;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class AddonServiceRegistry {

    private record ServiceEntry(String providerAddonId, Object service) {}

    private final Map<Class<?>, ServiceEntry> services = new HashMap<>();

    public synchronized <T> void register(String addonId, Class<T> serviceType, T service) {
        Objects.requireNonNull(addonId, "addonId");
        Objects.requireNonNull(serviceType, "serviceType");
        Objects.requireNonNull(service, "service");

        ServiceEntry existing = services.get(serviceType);
        if (existing != null && !existing.providerAddonId().equals(addonId)) {
            throw new IllegalStateException("Service already registered: " + serviceType.getName() + " Provider=" + existing.providerAddonId());
        }

        services.put(serviceType, new ServiceEntry(addonId, service));
    }

    public synchronized <T> Optional<T> resolveIfEnabled(Class<T> serviceType, Function<String, AddonManager.AddonState> stateProvider) {
        Objects.requireNonNull(serviceType, "serviceType");
        Objects.requireNonNull(stateProvider, "stateProvider");

        ServiceEntry entry = services.get(serviceType);
        if (entry == null) {
            return Optional.empty();
        }

        AddonManager.AddonState state = stateProvider.apply(entry.providerAddonId());
        if (state != AddonManager.AddonState.ENABLED) {
            return Optional.empty();
        }

        Object svc = entry.service();
        if (!serviceType.isInstance(svc)) {
            return Optional.empty();
        }

        return Optional.of(serviceType.cast(svc));
    }
}

