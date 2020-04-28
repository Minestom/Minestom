package net.minestom.server.data;

import java.util.concurrent.ConcurrentHashMap;

public class Data {

    protected ConcurrentHashMap<String, Object> data = new ConcurrentHashMap();

    public <T> void set(String key, T value, Class<T> type) {
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
