package com.darkbladedev.engine.api.assembly;

public interface AssemblyTrigger {

    String id();

    boolean shouldTrigger(AssemblyContext context);
}

