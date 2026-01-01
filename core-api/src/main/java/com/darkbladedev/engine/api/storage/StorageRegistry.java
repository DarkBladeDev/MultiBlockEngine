package com.darkbladedev.engine.api.storage;

public interface StorageRegistry {

    void registerFactory(String type, StorageServiceFactory factory);

    StorageService create(String type, StorageDescriptor descriptor);
}
