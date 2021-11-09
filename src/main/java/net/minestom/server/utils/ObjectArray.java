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
        if (index >= array.length) {
            T[] newArray = allocate(index * 2 + 1);
            System.arraycopy(array, 0, newArray, 0, array.length);
            this.array = newArray;
        }
        array[index] = object;
        this.max = Math.max(max, index);
    }

    public T get(int index) {
        return index < array.length ? array[index] : null;
    }

    public void trim() {
        T[] newArray = allocate(max + 1);
        System.arraycopy(array, 0, newArray, 0, max + 1);
        this.array = newArray;
    }

    private static <T> T[] allocate(int length) {
        //noinspection unchecked
        return (T[]) new Object[length];
    }
}
