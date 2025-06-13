package net.minestom.server.utils.collection;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Represents an array which will be resized to the highest required index.
 *
 * @param <T> the type of the array
 */
@ApiStatus.Internal
public sealed interface ObjectArray<T>
        permits ObjectArrayImpl.SingleThread, ObjectArrayImpl.Concurrent {
    static <T> @NotNull ObjectArray<T> singleThread(int initialSize) {
        return new ObjectArrayImpl.SingleThread<>(initialSize);
    }

    static <T> @NotNull ObjectArray<T> singleThread() {
        return singleThread(0);
    }

    static <T> @NotNull ObjectArray<T> concurrent(int initialSize) {
        return new ObjectArrayImpl.Concurrent<>(initialSize);
    }

    static <T> @NotNull ObjectArray<T> concurrent() {
        return concurrent(0);
    }

    @UnknownNullability T get(int index);

    void set(int index, @Nullable T object);

    default void remove(int index) {
        set(index, null);
    }

    void trim();

    @Contract(pure = true)
    @UnknownNullability T @NotNull [] arrayCopy(@NotNull Class<T> type);

    /**
     * Copies the array into a list.
     * Requires all elements to be present and indexed from 0.
     *
     * @return List of the array elements
     */
    @Contract(pure = true)
    @NotNull @Unmodifiable List<@NotNull T> toList();
}
