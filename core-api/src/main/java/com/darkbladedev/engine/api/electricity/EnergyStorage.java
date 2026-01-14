package com.darkbladedev.engine.api.electricity;

public interface EnergyStorage extends EnergyNode {
    long stored();

    long maxStored();

    long charge(long amount);

    long discharge(long amount);

    default void onCharged(long amount) {
    }

    default void onDischarged(long amount) {
    }
}

