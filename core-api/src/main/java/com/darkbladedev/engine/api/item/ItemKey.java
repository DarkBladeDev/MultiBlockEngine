package com.darkbladedev.engine.api.item;

import com.darkbladedev.engine.api.storage.item.NamespacedKey;
import org.jetbrains.annotations.Nullable;

public interface ItemKey extends com.darkbladedev.engine.api.storage.item.ItemKey {

    NamespacedKey id();

    int version();

    @Override
    default NamespacedKey type() {
        return id();
    }

    @Override
    default int damage() {
        return version();
    }

    @Override
    default @Nullable String nbtHash() {
        return null;
    }

}
