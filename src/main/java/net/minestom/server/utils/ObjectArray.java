package net.minestom.server.utils;

import org.jetbrains.annotations.ApiStatus;

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
        this.array = allocate(size);
    }

    public ObjectArray() {
        this(0);
    }

    public void set(int index, T object) {
        T[] array = this.array;
        if (index >= array.length) {
            T[] temp = allocate(index * 2 + 1);
            System.arraycopy(array, 0, temp, 0, array.length);
            this.array = array = temp;
        }
        array[index] = object;
        this.max = Math.max(max, index);
    }

    public T get(int index) {
        final T[] array = this.array;
        return index < array.length ? array[index] : null;
    }

    public void trim() {
        final int max = this.max;
        T[] temp = allocate(max + 1);
        System.arraycopy(array, 0, temp, 0, max + 1);
        this.array = temp;
    }

    private static <T> T[] allocate(int length) {
        //noinspection unchecked
        return (T[]) new Object[length];
    }
}
