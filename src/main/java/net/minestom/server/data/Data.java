package net.minestom.server.data;

import net.minestom.server.MinecraftServer;

import java.util.concurrent.ConcurrentHashMap;

public class Data {

    protected static final DataManager DATA_MANAGER = MinecraftServer.getDataManager();

    protected ConcurrentHashMap<String, Object> data = new ConcurrentHashMap();

    public <T> void set(String key, T value, Class<T> type) {
        // Type registering is only relevant if data should be serialized
        if (this instanceof SerializableData) {
            if (DATA_MANAGER.getDataType(type) == null) {
                throw new UnsupportedOperationException("Type " + type.getName() + " hasn't been registered in DataManager#registerType");
            }
        }

        this.data.put(key, value);
    }

    public <T> T get(String key) {
        return (T) data.get(key);
    }

    public <T> T getOrDefault(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    public Data clone() {
        Data data = new Data();
        data.data = new ConcurrentHashMap<>(this.data);
        return data;
    }

}
