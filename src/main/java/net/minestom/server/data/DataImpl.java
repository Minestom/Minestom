package net.minestom.server.data;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link Data} implementation which use a {@link ConcurrentHashMap}
 */
public class DataImpl implements Data {

    protected final ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<>();

    @Override
    public <T> void set(String key, T value, Class<T> type) {
        this.data.put(key, value);
    }

    @Override
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    @Override
    public <T> T getOrDefault(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean hasKey(String key) {
        return data.containsKey(key);
    }

    @Override
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(data.keySet());
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public Data clone() {
        DataImpl data = new DataImpl();
        data.data.putAll(this.data);
        return data;
    }

}
