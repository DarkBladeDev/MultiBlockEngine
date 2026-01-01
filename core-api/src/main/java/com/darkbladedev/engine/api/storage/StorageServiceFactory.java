package com.darkbladedev.engine.api.storage;

public interface StorageServiceFactory {

    StorageService create(StorageDescriptor descriptor);
}
