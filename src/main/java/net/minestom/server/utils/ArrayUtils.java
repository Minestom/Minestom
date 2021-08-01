package net.minestom.server.utils;

import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

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
        loop:
        for (final long aValue : a) {
            for (final long bValue : b) {
                if (bValue == aValue) {
                    continue loop;
                }
            }
            consumer.accept(aValue);
        }
    }

    public static int @NotNull [] toArray(@NotNull IntList list) {
        int[] array = new int[list.size()];
        list.getElements(0, array, 0, array.length);
        return array;
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
