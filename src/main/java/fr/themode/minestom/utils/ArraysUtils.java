package fr.themode.minestom.utils;

public class ArraysUtils {

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

}
