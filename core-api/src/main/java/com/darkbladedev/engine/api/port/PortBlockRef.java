package com.darkbladedev.engine.api.port;

public sealed interface PortBlockRef permits PortBlockRef.Controller, PortBlockRef.Offset {

    record Controller() implements PortBlockRef {
    }

    record Offset(int dx, int dy, int dz) implements PortBlockRef {
    }
}

