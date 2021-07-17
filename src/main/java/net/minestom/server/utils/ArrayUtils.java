package net.minestom.server.utils;

import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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

    /**
     * Gets the differences between 2 arrays.
     *
     * @param a the first array
     * @param b the second array
     * @return an array containing a's indexes that aren't in b array
     */
    public static int @NotNull [] getDifferencesBetweenArray(long @NotNull [] a, long @NotNull [] b) {
        int counter = 0;
        int[] indexes = new int[Math.max(a.length, b.length)];

        for (int i = 0; i < a.length; i++) {
            final long aValue = a[i];
            boolean contains = false;
            for (final long bValue : b) {
                if (bValue == aValue) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                indexes[counter++] = i;
            }
        }

        // Resize array
        int[] result = new int[counter];
        System.arraycopy(indexes, 0, result, 0, counter);
        return result;
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
}
