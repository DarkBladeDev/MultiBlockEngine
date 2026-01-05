package com.darkbladedev.engine.api.error;

import com.darkbladedev.engine.api.i18n.MessageKey;

public interface ErrorCode {

    MessageKey messageKey();

    default String id() {
        MessageKey key = messageKey();
        return key == null ? "unknown" : key.fullKey();
    }
}

