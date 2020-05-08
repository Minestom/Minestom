package net.minestom.server.storage;

public interface StorageSystem {

    void open(String folderName);

    byte[] get(String key);

    void set(String key, byte[] data);

    void delete(String key);

    void close();

}
