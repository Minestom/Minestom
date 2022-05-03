package net.minestom.server.utils.collection;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Arrays;

/**
 * Represents an array which will be resized to the highest required index.
 *
 * @param <T> the type of the array
 */
@ApiStatus.Internal
public final class ObjectArray<T> {
    private T[] array;
    private int max;

    public ObjectArray(int size) {
        //noinspection unchecked
        this.array = (T[]) new Object[size];
    }

    public ObjectArray() {
        this(0);
    }

    public void set(int index, @Nullable T object) {
        T[] array = this.array;
        if (index >= array.length) {
            final int newLength = index * 2 + 1;
            this.array = array = Arrays.copyOf(array, newLength);
        }
        array[index] = object;
        this.max = Math.max(max, index);
    }

    public @UnknownNullability T get(int index) {
        final T[] array = this.array;
        return index < array.length ? array[index] : null;
    }

    public void trim() {
        this.array = Arrays.copyOf(array, max + 1);
    }
}
