package net.minestom.server.utils;

import org.jetbrains.annotations.ApiStatus;

/**
 * Used to mimic the final keyword
 *
 * @param <T> type of the object
 */
@ApiStatus.Internal
public final class FinalObject<T> {
    private T obj;
    private boolean finalized;

    /**
     * Set the value
     *
     * @param object initial value
     * @throws RuntimeException if this method called more than once on the same object
     */
    public void set(T object) {
        if (finalized) {
            throw new RuntimeException("Object is already set!");
        } else {
            this.obj = object;
            finalized = true;
        }
    }

    public boolean isSet() {
        return obj != null;
    }

    public T get() {
        return obj;
    }
}
