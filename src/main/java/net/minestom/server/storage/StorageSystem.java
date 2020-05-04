package net.minestom.server.storage;

import java.util.function.Consumer;

public interface StorageSystem {

    void open(String folderName);

    void get(String key, Consumer<byte[]> callback);

    void set(String key, byte[] data);

    void delete(String key);

    void close();

}
