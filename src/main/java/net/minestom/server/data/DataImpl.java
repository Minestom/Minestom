package net.minestom.server.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link Data} implementation which uses a {@link ConcurrentHashMap}.
 */
@Deprecated
public class DataImpl extends Data {

    protected final ConcurrentHashMap<String, Object> data = new ConcurrentHashMap<>();

    /**
     * Data key = Class
     * Used to know the type of an element of this data object (for serialization purpose)
     */
    protected final ConcurrentHashMap<String, Class> dataType = new ConcurrentHashMap<>();

    @Override
    public synchronized <T> void set(@NotNull String key, @Nullable T value, @Nullable Class<T> type) {
        if (value != null) {
            this.data.put(key, value);
            this.dataType.put(key, type);
        } else {
            this.data.remove(key);
            this.dataType.remove(key);
        }
    }

    @Override
    public <T> T get(@NotNull String key) {
        return (T) data.get(key);
    }

    @Override
    public <T> T getOrDefault(@NotNull String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean hasKey(@NotNull String key) {
        return data.containsKey(key);
    }

    @NotNull
    @Override
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(data.keySet());
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @NotNull
    @Override
    public DataImpl clone() {
        return (DataImpl) super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataImpl data1 = (DataImpl) o;
        return Objects.equals(data, data1.data) &&
                Objects.equals(dataType, data1.dataType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, dataType);
    }
}
