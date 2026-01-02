package com.darkbladedev.engine.api.item;

import java.util.Map;

public interface ItemDefinition {

    ItemKey key();

    String displayName();

    Map<String, Object> properties();
}

