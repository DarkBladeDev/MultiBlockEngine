package com.darkbladedev.engine.api.wiring.debug;

public interface NetworkInspectView {

    void putMetric(String key, Object value);

    void addLine(String line);
}

