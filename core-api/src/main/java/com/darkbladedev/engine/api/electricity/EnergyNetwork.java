package com.darkbladedev.engine.api.electricity;

import com.darkbladedev.engine.api.wiring.NetworkGraph;

public interface EnergyNetwork {
    NetworkGraph graph();

    long produced();

    long consumed();

    long stored();

    long starved();

    long overflowed();
}

