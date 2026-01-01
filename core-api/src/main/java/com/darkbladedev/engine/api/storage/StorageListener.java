package com.darkbladedev.engine.api.storage;

import com.darkbladedev.engine.api.storage.item.ItemKey;

public interface StorageListener {

    void onInsert(ItemKey key, long amount);

    void onExtract(ItemKey key, long amount);

    void onClear();
}
