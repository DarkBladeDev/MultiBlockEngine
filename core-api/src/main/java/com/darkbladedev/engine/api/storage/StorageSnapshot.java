package com.darkbladedev.engine.api.storage;

import com.darkbladedev.engine.api.storage.item.ItemKey;

import java.util.Map;

public interface StorageSnapshot {

    long timestamp();

    Map<ItemKey, Long> entries();
}
