package net.minestom.server.utils;

import java.util.ArrayList;

public class ArrayUtils {

    public static byte[] concenateByteArrays(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }
        byte[] result = new byte[totalLength];

        int startingPos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, startingPos, array.length);
        }

        return result;
    }

    public static void removeElement(Object[] arr, int index) {
        System.arraycopy(arr, index + 1, arr, index, arr.length - 1 - index);
    }

    /**
     * @param a
     * @param b
     * @return an array containing a's indexes that aren't in b array
     */
    public static int[] getDifferencesBetweenArray(long[] a, long[] b) {
        int counter = 0;
        int[] indexes = new int[Math.max(a.length, b.length)];

        for (int i = 0; i < a.length; i++) {
            long aValue = a[i];
            boolean contains = false;
            for (int n = 0; n < b.length; n++) {
                long bValue = b[n];
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

    public static int[] toArray(ArrayList<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }

}
