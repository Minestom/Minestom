package net.minestom.server.utils;

import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.function.ToIntFunction;

@ApiStatus.Internal
public final class ArrayUtils {
    private ArrayUtils() {
    }

    public static <T> int[] mapToIntArray(Collection<T> collection, ToIntFunction<T> function) {
        final int size = collection.size();
        if (size == 0) return new int[0];
        int[] result = new int[size];
        int i = 0;
        for (T object : collection) {
            result[i++] = function.applyAsInt(object);
        }
        assert i == size;
        return result;
    }
}
