package net.minestom.server.instance.light.starlight;

class IntegerUtil {

    public static int getTrailingBit(final int n) {
        return -n & n;
    }

    public static int trailingZeros(final int n) {
        return Integer.numberOfTrailingZeros(n);
    }

    public static int branchlessAbs(final int val) {
        // -n = -1 ^ n + 1
        final int mask = val >> (Integer.SIZE - 1); // -1 if < 0, 0 if >= 0
        return (mask ^ val) - mask; // if val < 0, then (0 ^ val) - 0 else (-1 ^ val) + 1
    }

    private IntegerUtil() {}

}
