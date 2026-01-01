package com.darkbladedev.engine.api.storage;

public interface StorageExceptionHandler {

    void handle(StorageService storage, Throwable error);
}
