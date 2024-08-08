package net.minestom.server.utils;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.ToIntFunction;

@ApiStatus.Internal
public final class ArrayUtils {

    private ArrayUtils() {
    }

    public static boolean isEmpty(@Nullable Object @NotNull [] array) {
        for (Object object : array) {
            if (object != null) return false;
        }
        return true;
    }

    public static <T> int[] mapToIntArray(Collection<T> collection, ToIntFunction<T> function) {
        final int size = collection.size();
        if (size == 0)
            return new int[0];
        int[] result = new int[size];
        int i = 0;
        for (T object : collection) {
            result[i++] = function.applyAsInt(object);
        }
        assert i == size;
        return result;
    }

    public static <K, V> Map<K, V> toMap(@NotNull K[] keys, @NotNull V[] values, int length) {
        assert keys.length >= length && keys.length == values.length;
        return switch (length) {
            case 0 -> Map.of();
            case 1 -> Map.of(keys[0], values[0]);
            case 2 -> Map.of(keys[0], values[0], keys[1], values[1]);
            case 3 -> Map.of(keys[0], values[0], keys[1], values[1], keys[2], values[2]);
            case 4 -> Map.of(keys[0], values[0], keys[1], values[1], keys[2], values[2],
                    keys[3], values[3]);
            case 5 -> Map.of(keys[0], values[0], keys[1], values[1], keys[2], values[2],
                    keys[3], values[3], keys[4], values[4]);
            case 6 -> Map.of(keys[0], values[0], keys[1], values[1], keys[2], values[2],
                    keys[3], values[3], keys[4], values[4], keys[5], values[5]);
            case 7 -> Map.of(keys[0], values[0], keys[1], values[1], keys[2], values[2],
                    keys[3], values[3], keys[4], values[4], keys[5], values[5], keys[6], values[6]);
            case 8 -> Map.of(keys[0], values[0], keys[1], values[1], keys[2], values[2],
                    keys[3], values[3], keys[4], values[4], keys[5], values[5], keys[6], values[6],
                    keys[7], values[7]);
            case 9 -> Map.of(keys[0], values[0], keys[1], values[1], keys[2], values[2],
                    keys[3], values[3], keys[4], values[4], keys[5], values[5], keys[6], values[6],
                    keys[7], values[7], keys[8], values[8]);
            case 10 -> Map.of(keys[0], values[0], keys[1], values[1], keys[2], values[2],
                    keys[3], values[3], keys[4], values[4], keys[5], values[5], keys[6], values[6],
                    keys[7], values[7], keys[8], values[8], keys[9], values[9]);
            default -> Map.copyOf(new Object2ObjectArrayMap<>(keys, values, length));
        };
    }
}
