package com.darkbladedev.engine.api.wrench;

@FunctionalInterface
public interface WrenchInteractable {
    WrenchResult onWrenchUse(WrenchContext context);
}

