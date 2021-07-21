package net.minestom.server.utils;

import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.LongConsumer;

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

    public static void forDifferencesBetweenArray(long @NotNull [] a, long @NotNull [] b,
                                                  @NotNull LongConsumer consumer) {
        for (final long aValue : a) {
            boolean contains = false;
            for (final long bValue : b) {
                if (bValue == aValue) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                consumer.accept(aValue);
            }
        }
    }

    public static int @NotNull [] toArray(@NotNull IntList list) {
        int[] array = new int[list.size()];
        list.getElements(0, array, 0, array.length);
        return array;
    }

    /**
     * Gets if two arrays share the same start until {@code length}.
     *
     * @param first  the first array
     * @param second the second array
     * @param length the length to check (0-length)
     * @param <T>    the type of the arrays
     * @return true if both arrays share the same start
     */
    public static <T> boolean sameStart(@NotNull T[] first, @NotNull T[] second, int length) {
        if (Math.min(first.length, second.length) < length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (!Objects.equals(first[i], second[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean empty(byte[] array) {
        for (byte b : array) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }
}
