package net.minestom.server.instance.palette;

import net.minestom.server.utils.MathUtils;

import java.util.Arrays;

public final class Palettes {
    private Palettes() {
    }

    public static long[] pack(int[] ints, int bitsPerEntry) {
        final int intsPerLong = (int) Math.floor(64d / bitsPerEntry);
        long[] longs = new long[(int) Math.ceil(ints.length / (double) intsPerLong)];
        final long mask = (1L << bitsPerEntry) - 1L;
        for (int i = 0; i < longs.length; i++) {
            for (int intIndex = 0; intIndex < intsPerLong; intIndex++) {
                final int bitIndex = intIndex * bitsPerEntry;
                final int intActualIndex = intIndex + i * intsPerLong;
                if (intActualIndex < ints.length) {
                    longs[i] |= (ints[intActualIndex] & mask) << bitIndex;
                }
            }
        }
        return longs;
    }

    public static void unpack(int[] out, long[] in, int bitsPerEntry) {
        assert in.length != 0 : "unpack input array is zero";

        final double intsPerLong = Math.floor(64d / bitsPerEntry);
        final int intsPerLongCeil = (int) Math.ceil(intsPerLong);

        final long mask = (1L << bitsPerEntry) - 1L;
        for (int i = 0; i < out.length; i++) {
            final int longIndex = i / intsPerLongCeil;
            final int subIndex = i % intsPerLongCeil;
            out[i] = (int) ((in[longIndex] >>> (bitsPerEntry * subIndex)) & mask);
        }
    }

    public static int maxPaletteSize(int bitsPerEntry) {
        return 1 << bitsPerEntry;
    }

    public static int arrayLength(int dimension, int bitsPerEntry) {
        final int elementCount = dimension * dimension * dimension;
        final int valuesPerLong = 64 / bitsPerEntry;
        return (elementCount + valuesPerLong - 1) / valuesPerLong;
    }

    public static int read(int dimension, int bitsPerEntry, long[] values,
                           int x, int y, int z) {
        final int sectionIndex = sectionIndex(dimension, x, y, z);
        final int valuesPerLong = 64 / bitsPerEntry;
        final int index = sectionIndex / valuesPerLong;
        final int bitIndex = (sectionIndex - index * valuesPerLong) * bitsPerEntry;
        final int mask = (1 << bitsPerEntry) - 1;
        return (int) (values[index] >> bitIndex) & mask;
    }

    public static int write(int dimension, int bitsPerEntry, long[] values,
                            int x, int y, int z, int value) {
        final int valuesPerLong = 64 / bitsPerEntry;
        final int sectionIndex = sectionIndex(dimension, x, y, z);
        final int index = sectionIndex / valuesPerLong;
        final int bitIndex = (sectionIndex - index * valuesPerLong) * bitsPerEntry;

        final long block = values[index];
        final long clear = (1L << bitsPerEntry) - 1L;
        final long oldBlock = block >> bitIndex & clear;
        values[index] = block & ~(clear << bitIndex) | ((long) value << bitIndex);
        return (int) oldBlock;
    }

    public static void fill(int bitsPerEntry, long[] values, int value) {
        final int valuesPerLong = 64 / bitsPerEntry;
        long block = 0;
        for (int i = 0; i < valuesPerLong; i++) block |= (long) value << i * bitsPerEntry;
        Arrays.fill(values, block);
    }

    public static int count(int bitsPerEntry, long[] values) {
        final int valuesPerLong = 64 / bitsPerEntry;
        int count = 0;
        for (long block : values) {
            for (int i = 0; i < valuesPerLong; i++) {
                count += (int) ((block >>> i * bitsPerEntry) & ((1 << bitsPerEntry) - 1));
            }
        }
        return count;
    }

    public static int sectionIndex(int dimension, int x, int y, int z) {
        final int dimensionBitCount = MathUtils.bitsToRepresent(dimension - 1);
        return y << (dimensionBitCount << 1) | z << dimensionBitCount | x;
    }

    // Optimized operations

    public static void getAllFill(byte dimension, int value, Palette.EntryConsumer consumer) {
        for (byte y = 0; y < dimension; y++)
            for (byte z = 0; z < dimension; z++)
                for (byte x = 0; x < dimension; x++)
                    consumer.accept(x, y, z, value);
    }
}
