package net.minestom.server.storage;

import io.netty.buffer.Unpooled;
import net.minestom.server.MinecraftServer;
import net.minestom.server.data.DataContainer;
import net.minestom.server.data.DataManager;
import net.minestom.server.data.DataType;
import net.minestom.server.data.SerializableData;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.reader.DataReader;
import net.minestom.server.utils.validate.Check;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StorageFolder {

    private static final DataManager DATA_MANAGER = MinecraftServer.getDataManager();

    private StorageSystem storageSystem;
    private String folderPath;

    private Map<String, SerializableData> cachedData;

    protected StorageFolder(StorageSystem storageSystem, String folderPath, StorageOptions storageOptions) {
        this.storageSystem = storageSystem;
        this.folderPath = folderPath;

        this.cachedData = new HashMap<>();

        this.storageSystem.open(folderPath, storageOptions);
    }

    public byte[] get(String key) {
        return storageSystem.get(key);
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

    public <T> void set(String key, T object, Class<T> type) {
        DataType<T> dataType = DATA_MANAGER.getDataType(type);
        Check.notNull(dataType, "You can only save registered DataType type!");

        PacketWriter packetWriter = new PacketWriter();
        dataType.encode(packetWriter, object); // Encode
        byte[] encodedValue = packetWriter.toByteArray(); // Retrieve bytes

        set(key, encodedValue);
    }

    public <T> T get(String key, Class<T> type) {
        DataType<T> dataType = DATA_MANAGER.getDataType(type);
        Check.notNull(dataType, "You can only save registered DataType type!");

        byte[] data = get(key);
        if (data == null)
            return null;

        PacketReader packetReader = new PacketReader(data);
        T value = dataType.decode(packetReader);
        return value;
    }

    public <T> T getOrDefault(String key, Class<T> type, T defaultValue) {
        T value;
        return (value = get(key, type)) != null ? value : defaultValue;
    }

    /**
     * Get an unique {@link SerializableData} which is cloned if cached or retrieved with the default {@link StorageSystem}
     *
     * @param key           the key of the data
     * @param dataContainer the {@link DataContainer} which will contain the new data
     */
    public void getAndCloneData(String key, DataContainer dataContainer) {
        synchronized (cachedData) {

            // Copy data from the cachedMap
            if (cachedData.containsKey(key)) {
                SerializableData data = cachedData.get(key);
                dataContainer.setData(data.clone());
                return;
            }

            // Load it from the storage system
            byte[] bytes = get(key);
            SerializableData data;

            if (bytes != null) {
                data = DataReader.readData(Unpooled.wrappedBuffer(bytes));
            } else {
                data = new SerializableData();
            }

            dataContainer.setData(data);

        }
    }

    /**
     * Get a shared {@link SerializableData} if already in memory or retrieve it from the default {@link StorageSystem} and save it in cache
     * for further request.
     * Those cached data can be saved using {@link #saveCachedData()} or individually with {@link #saveCachedData(String)}
     * It is also possible to save an individual data and remove it directly with {@link #saveAndRemoveCachedData(String)}
     *
     * @param key           the key of the data
     * @param dataContainer the {@link DataContainer} which will contain the new data
     */
    public void getAndCacheData(String key, DataContainer dataContainer) {
        synchronized (cachedData) {

            // Give the cached SerializableData if already loaded
            if (cachedData.containsKey(key)) {
                dataContainer.setData(cachedData.get(key));
                return;
            }

            // Load it from the storage system and cache it
            byte[] bytes = get(key);
            SerializableData data;

            if (bytes != null) {
                data = DataReader.readData(Unpooled.wrappedBuffer(bytes));
            } else {
                data = new SerializableData();
            }

            dataContainer.setData(data);

            this.cachedData.put(key, data);

        }
    }

    /**
     * Save a specified cached data and remove it from memory
     *
     * @param key the specified cached data key
     */
    public void saveAndRemoveCachedData(String key) {
        synchronized (cachedData) {
            SerializableData serializableData = cachedData.get(key);
            if (serializableData == null)
                return;

            try {
                set(key, serializableData.getSerializedData());
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Remove from map
            this.cachedData.remove(key);
        }
    }

    /**
     * Save the whole cached data
     */
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

    /**
     * Save an unique cached data
     *
     * @param key the data key
     */
    public void saveCachedData(String key) {
        try {
            synchronized (cachedData) {
                SerializableData data = cachedData.get(key);
                set(key, data.getSerializedData());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFolderPath() {
        return folderPath;
    }
}
