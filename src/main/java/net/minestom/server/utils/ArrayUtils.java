package net.minestom.server.utils;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@ApiStatus.Internal
public final class ArrayUtils {

    private ArrayUtils() {
    }

    public static int[] concatenateIntArrays(int @NotNull []... arrays) {
        int totalLength = 0;
        for (int[] array : arrays) {
            totalLength += array.length;
        }
        int[] result = new int[totalLength];
        int startingPos = 0;
        for (int[] array : arrays) {
            System.arraycopy(array, 0, result, startingPos, array.length);
            startingPos += array.length;
        }
        return result;
    }

    public static void removeElement(@NotNull Object[] arr, int index) {
        System.arraycopy(arr, index + 1, arr, index, arr.length - 1 - index);
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

    public static int @NotNull [] toArray(@NotNull IntList list) {
        int[] array = new int[list.size()];
        list.getElements(0, array, 0, array.length);
        return array;
    }

    private static final int INDEX_NOT_FOUND = -1;

    public static int indexOf(final Object[] array, final Object objectToFind) {
        return indexOf(array, objectToFind, 0);
    }

    public static int indexOf(final Object[] array, final Object objectToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (objectToFind == null) {
            for (int i = startIndex; i < array.length; i++) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = startIndex; i < array.length; i++) {
                if (objectToFind.equals(array[i])) {
                    return i;
                }
            }
        }
        return INDEX_NOT_FOUND;
    }
}
