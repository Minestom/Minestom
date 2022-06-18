package net.minestom.server.utils;

import java.util.function.Supplier;

/**
 * Used to cache objects
 *
 * @param <T> type of the object
 */
public final class ObjectCache<T> {
    private final Supplier<T> supplier;
    private T obj;

    public ObjectCache(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public void invalidate() {
        obj = null;
    }

    public T get() {
        if (obj == null) {
            obj = supplier.get();
        }
        return obj;
    }
}
