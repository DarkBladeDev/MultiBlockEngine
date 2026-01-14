package com.darkbladedev.engine.api.electricity;

public interface EnergyConsumer extends EnergyNode {
    long demandPerTick();

    default void onEnergyReceived(long amount) {
    }
}

