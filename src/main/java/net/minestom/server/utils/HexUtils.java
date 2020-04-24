package net.minestom.server.utils;

public class HexUtils {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static char byteToHex(byte b) {
        int v = b & 0xFF;
        char hexChar = HEX_ARRAY[v >>> 4];
        return hexChar;
    }

}
