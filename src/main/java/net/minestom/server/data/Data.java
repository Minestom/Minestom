package net.minestom.server.data;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Data {

    public static final Data EMPTY = new Data() {
        @Override
        public <T> void set(String key, T value, Class<T> type) {
        }

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

    /**
     * Set a value to a specific key
     *
     * @param key   the key
     * @param value the value object
     * @param type  the value type
     * @param <T>   the value generic
     */
    public <T> void set(String key, T value, Class<T> type) {
        this.data.put(key, value);
    }

    /**
     * Retrieve a value based on its key
     *
     * @param key the key
     * @param <T> the value type
     * @return the data associated with the key
     * @throws NullPointerException if the key is not found
     */
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    /**
     * Retrieve a value based on its key, give a default value if not found
     *
     * @param key          the key
     * @param defaultValue the value to return if the key is not found
     * @param <T>          the value type
     * @return {@link #get(String)} if found, {@code defaultValue} otherwise
     */
    public <T> T getOrDefault(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    /**
     * Get if the data has a key
     *
     * @param key the key to check
     * @return true if the data contains the key
     */
    public boolean hasKey(String key) {
        return data.containsKey(key);
    }

    /**
     * Get the list of data keys
     *
     * @return an unmodifiable set containing all keys
     */
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(data.keySet());
    }

    /**
     * Get if the data is empty or not
     *
     * @return true if the data does not contain anything, false otherwise
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Clone this data
     *
     * @return a cloned data object
     */
    public Data clone() {
        Data data = new Data();
        data.data.putAll(this.data);
        return data;
    }

}
