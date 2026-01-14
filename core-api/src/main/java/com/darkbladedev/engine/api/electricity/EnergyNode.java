package com.darkbladedev.engine.api.electricity;

import com.darkbladedev.engine.api.wiring.NetworkNode;

public interface EnergyNode {
    NetworkNode networkNode();

    long capacity();
}

