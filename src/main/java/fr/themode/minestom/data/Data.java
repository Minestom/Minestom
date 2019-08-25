package fr.themode.minestom.data;

import java.util.concurrent.ConcurrentHashMap;

public class Data {

    private ConcurrentHashMap<String, Object> data = new ConcurrentHashMap();
    private ConcurrentHashMap<String, DataType> dataType = new ConcurrentHashMap<>();

    public <T> void set(String key, T value, DataType<T> type) {
        this.data.put(key, value);
        this.dataType.put(key, type);
    }

    public <T> T get(String key) {
        return (T) data.get(key);
    }

    public <T> T getOrDefault(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    // TODO serialization

}
