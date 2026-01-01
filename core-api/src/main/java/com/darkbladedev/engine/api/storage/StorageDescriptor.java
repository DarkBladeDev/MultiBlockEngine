package com.darkbladedev.engine.api.storage;

import java.util.Map;
import java.util.UUID;

public interface StorageDescriptor {

    UUID id();

    long capacity();

    Map<String, Object> properties();
}
