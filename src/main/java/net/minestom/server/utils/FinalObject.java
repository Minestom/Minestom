package net.minestom.server.utils;

import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

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
        return finalized;
    }

    public boolean optionalSet(T object) {
        if (isSet()) return false;
        set(object);
        return true;
    }

    public boolean optionalSet(Supplier<T> objectSupplier) {
        if (isSet()) return false;
        set(objectSupplier.get());
        return true;
    }

    public T get() {
        return obj;
    }
}
