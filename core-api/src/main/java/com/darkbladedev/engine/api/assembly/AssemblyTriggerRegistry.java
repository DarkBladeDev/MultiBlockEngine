package com.darkbladedev.engine.api.assembly;

import java.util.Collection;
import java.util.Optional;

public interface AssemblyTriggerRegistry {

    void register(AssemblyTrigger trigger);

    Optional<AssemblyTrigger> get(String id);

    Collection<AssemblyTrigger> all();
}

