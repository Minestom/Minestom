package net.minestom.server.data;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Data {

    public static final Data EMPTY = new Data() {
        @Override
        public <T> void set(String key, T value, Class<T> type) {}

        @Override
        public <T> T get(String key) {
            return null;
        }

        @Override
        public boolean hasKey(String key) {
            return false;
        }

        @Override
        public <T> T getOrDefault(String key, T defaultValue) {
            return defaultValue;
        }
    };

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

    /**
     * @param key
     * @return true if the data contains the key, false otherwise
     */
    public boolean hasKey(String key) {
        return data.containsKey(key);
    }

    /**
     * @return an unmodifiable set containing all keys
     */
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(data.keySet());
    }

    /**
     * @return true if the data does not contain anything, false otherwise
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    public Data clone() {
        Data data = new Data();
        data.data = new ConcurrentHashMap<>(this.data);
        return data;
    }

}
