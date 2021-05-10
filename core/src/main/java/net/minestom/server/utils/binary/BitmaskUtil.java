package net.minestom.server.utils.binary;

public final class BitmaskUtil {

    public static byte changeBit(byte value, byte mask, byte replacement, byte shift) {
        return (byte) (value & ~mask | (replacement << shift));
    }

}
