package com.darkbladedev.engine.api.electricity;

public interface EnergyProducer extends EnergyNode {
    long producePerTick();

    default void onEnergyProduced(long amount) {
    }
}

