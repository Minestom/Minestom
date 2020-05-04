package net.minestom.server.storage;

import io.netty.buffer.Unpooled;
import net.minestom.server.data.DataContainer;
import net.minestom.server.data.SerializableData;
import net.minestom.server.reader.DataReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class StorageFolder {

    private StorageSystem storageSystem;
    private String folderPath;

    private Map<String, SerializableData> cachedData;

    protected StorageFolder(StorageSystem storageSystem, String folderPath) {
        this.storageSystem = storageSystem;
        this.folderPath = folderPath;

        this.cachedData = new HashMap<>();

        this.storageSystem.open(folderPath);
    }

    public void get(String key, Consumer<byte[]> callback) {
        this.storageSystem.get(key, callback);
    }

    public void set(String key, byte[] data) {
        this.storageSystem.set(key, data);
    }

    public void delete(String key) {
        this.storageSystem.delete(key);
    }

    public void close() {
        this.storageSystem.close();
    }

    public void getAndCloneData(String key, DataContainer dataContainer, Runnable callback) {
        synchronized (cachedData) {

            // Copy data from the cachedMap
            if (cachedData.containsKey(key)) {
                SerializableData data = cachedData.get(key);
                dataContainer.setData(data.clone());
                if (callback != null)
                    callback.run();
                return;
            }

            // Load it from the storage system
            get(key, bytes -> {
                SerializableData data;

                if (bytes != null) {
                    data = DataReader.readData(Unpooled.wrappedBuffer(bytes));
                } else {
                    data = new SerializableData();
                }

                dataContainer.setData(data);

                if (callback != null)
                    callback.run();
            });

        }
    }

    public void getAndCloneData(String key, DataContainer dataContainer) {
        getAndCloneData(key, dataContainer, null);
    }

    public void getAndCacheData(String key, DataContainer dataContainer, Runnable callback) {
        synchronized (cachedData) {

            // Give the cached SerializableData if already loaded
            if (cachedData.containsKey(key)) {
                dataContainer.setData(cachedData.get(key));
                if (callback != null)
                    callback.run();
                return;
            }

            // Load it from the storage system and cache it
            get(key, bytes -> {
                SerializableData data;

                if (bytes != null) {
                    data = DataReader.readData(Unpooled.wrappedBuffer(bytes));
                } else {
                    data = new SerializableData();
                }

                dataContainer.setData(data);

                this.cachedData.put(key, data);

                if (callback != null)
                    callback.run();
            });

        }
    }

    public void getAndCacheData(String key, DataContainer dataContainer) {
        getAndCacheData(key, dataContainer, null);
    }

    public void saveCachedData() {
        try {
            synchronized (cachedData) {
                for (Map.Entry<String, SerializableData> entry : cachedData.entrySet()) {
                    String key = entry.getKey();
                    SerializableData data = entry.getValue();

                    set(key, data.getSerializedData());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFolderPath() {
        return folderPath;
    }
}
