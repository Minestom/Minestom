package net.minestom.server.instance.light;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class NewLightComputation {
    private static final int SECTION_SIZE = 16;
    // We need 4 bit to display a light level, so 16*16*16 / 2 bytes.
    private static final int LIGHT_BYTE_ARRAY_SIZE = SECTION_SIZE * SECTION_SIZE * SECTION_SIZE / 2;
    public static final byte[] EMPTY_CONTENT = new byte[LIGHT_BYTE_ARRAY_SIZE];
    public static final byte[] CONTENT_FULLY_LIT = new byte[LIGHT_BYTE_ARRAY_SIZE];

    static {
        Arrays.fill(CONTENT_FULLY_LIT, (byte) -1);
    }

    /**
     * Bakes two lighting data sets into one.
     * This is a {@link Math#max(int, int)} call for every light level at every position.
     *
     * @param light1 the first light data
     * @param light2 the second light data
     * @return the baked light data
     * @implNote for performance reasons, whenever an empty data set is passed,
     * it should be the {@link #EMPTY_CONTENT} data set.
     */
    public static byte @NotNull [] bake(byte @NotNull [] light1, byte @NotNull [] light2) {
        assert light1.length == LIGHT_BYTE_ARRAY_SIZE;
        assert light2.length == LIGHT_BYTE_ARRAY_SIZE;

        // Some simple optimizations
        if (light1 == EMPTY_CONTENT) return light2;
        if (light2 == EMPTY_CONTENT) return light1;

        assert !Arrays.equals(light1, EMPTY_CONTENT);
        assert !Arrays.equals(light2, EMPTY_CONTENT);

        byte[] max = new byte[LIGHT_BYTE_ARRAY_SIZE];
        for (int i = 0; i < light1.length; i++) {
            final byte l1 = light1[i];
            final byte l2 = light2[i];

            // 2 light levels stored, each 4-bit
            // first light level
            final byte a1 = (byte) (l1 & 0xF);
            final byte a2 = (byte) (l2 & 0xF);

            // second light level
            final byte b1 = (byte) ((l1 >> 4) & 0xF);
            final byte b2 = (byte) ((l2 >> 4) & 0xF);

            final byte a = (byte) Math.max(a1, a2);
            final byte b = (byte) Math.max(b1, b2);

            max[i] = (byte) (a | (b << 4));
        }
        return max;
    }

    /**
     * Get the light level at the corresponding index
     *
     * @param light the light data
     * @param index the light index
     * @return the light level
     */
    public static int getLight(byte[] light, int index) {
        assert index >> 1 < light.length;
        final byte value = light[index >> 1];
        return (value >> ((index & 1) << 2)) & 0xF;
    }

    public static int getLight(byte[] light, int x, int y, int z) {
        return getLight(light, index(x, y, z));
    }

    public static int index(int x, int y, int z) {
        assert (x & 0xF) == x;
        assert (y & 0xF) == y;
        assert (z & 0xF) == z;
        return x | (z << 4) | (y << 8);
    }
}
