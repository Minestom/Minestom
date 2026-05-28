package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
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
        Arrays.fill(values, broadcast(bitsPerEntry, value));
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

    /// Builds a 64-bit pattern with {@code value} placed in every {@code bitsPerEntry}-wide lane.
    public static long broadcast(int bitsPerEntry, int value) {
        final int valuesPerLong = 64 / bitsPerEntry;
        long pattern = 0L;
        for (int i = 0; i < valuesPerLong; i++) pattern |= (long) value << (i * bitsPerEntry);
        return pattern;
    }

    /// Counts the packed entries equal to {@code target} among the first {@code size} entries.
    /// Scans 64 bits at a time using borrow-safe SWAR zero-lane detection.
    public static int countEquals(int bitsPerEntry, long[] values, int size, int target) {
        final int valuesPerLong = 64 / bitsPerEntry;
        final long ones = broadcast(bitsPerEntry, 1);
        final long lowMask = ones * ((1L << (bitsPerEntry - 1)) - 1);
        final long highBits = ones * (1L << (bitsPerEntry - 1));
        final long broadcastTarget = ones * target;
        int result = 0;
        for (int i = 0, idx = 0; i < values.length; i++, idx += valuesPerLong) {
            result += Long.bitCount(matchingLanes(values[i], broadcastTarget, lowMask, highBits, size - idx, valuesPerLong, bitsPerEntry));
        }
        return result;
    }

    /// Returns true if any of the first {@code size} packed entries equals {@code target}.
    public static boolean anyEquals(int bitsPerEntry, long[] values, int size, int target) {
        final int valuesPerLong = 64 / bitsPerEntry;
        final long ones = broadcast(bitsPerEntry, 1);
        final long lowMask = ones * ((1L << (bitsPerEntry - 1)) - 1);
        final long highBits = ones * (1L << (bitsPerEntry - 1));
        final long broadcastTarget = ones * target;
        for (int i = 0, idx = 0; i < values.length; i++, idx += valuesPerLong) {
            if (matchingLanes(values[i], broadcastTarget, lowMask, highBits, size - idx, valuesPerLong, bitsPerEntry) != 0)
                return true;
        }
        return false;
    }

    /// Replaces every packed entry equal to {@code oldValue} with {@code newValue} among the first
    /// {@code size} entries, returning the number of entries replaced.
    public static int replaceEquals(int bitsPerEntry, long[] values, int size, int oldValue, int newValue) {
        final int valuesPerLong = 64 / bitsPerEntry;
        final long ones = broadcast(bitsPerEntry, 1);
        final long lowMask = ones * ((1L << (bitsPerEntry - 1)) - 1);
        final long highBits = ones * (1L << (bitsPerEntry - 1));
        final long broadcastOld = ones * oldValue;
        final long broadcastNew = ones * newValue;
        int result = 0;
        for (int i = 0, idx = 0; i < values.length; i++, idx += valuesPerLong) {
            final long block = values[i];
            final long zeros = matchingLanes(block, broadcastOld, lowMask, highBits, size - idx, valuesPerLong, bitsPerEntry);
            if (zeros == 0) continue;
            // Expand each lane's high-bit marker to a full-lane mask, then swap the matching lanes.
            final long laneMask = zeros | (zeros - (zeros >>> (bitsPerEntry - 1)));
            values[i] = (block & ~laneMask) | (broadcastNew & laneMask);
            result += Long.bitCount(zeros);
        }
        return result;
    }

    /// High bit set in each lane equal to {@code broadcastTarget}, restricted to the first
    /// {@code remaining} lanes. Borrow-safe so a zero lane never spills into its neighbour.
    private static long matchingLanes(long block, long broadcastTarget, long lowMask, long highBits,
                                      int remaining, int valuesPerLong, int bitsPerEntry) {
        final long x = block ^ broadcastTarget;
        final long t = (x & lowMask) + lowMask;
        long zeros = ~(t | x) & highBits;
        if (remaining < valuesPerLong) zeros &= (1L << (remaining * bitsPerEntry)) - 1L;
        return zeros;
    }

    public static int sectionIndex(int dimension, int x, int y, int z) {
        final int dimensionBitCount = MathUtils.bitsToRepresent(dimension - 1);
        return y << (dimensionBitCount << 1) | z << dimensionBitCount | x;
    }

    static void validateIndices(int bitsPerEntry, int dimension, long[] values, int paletteSize) {
        final int valuesPerLong = 64 / bitsPerEntry;
        final int size = dimension * dimension * dimension;
        final long mask = (1L << bitsPerEntry) - 1L;
        for (int i = 0, idx = 0; i < values.length; i++) {
            long block = values[i];
            final int end = Math.min(valuesPerLong, size - idx);
            for (int j = 0; j < end; j++, idx++) {
                final int paletteIdx = (int) (block & mask);
                if (paletteIdx >= paletteSize)
                    throw new IllegalArgumentException("Palette index out of range: " + paletteIdx + " >= " + paletteSize);
                block >>>= bitsPerEntry;
            }
        }
    }

    // Optimized operations

    public static void getAllFill(byte dimension, int value, Palette.EntryConsumer consumer) {
        for (byte y = 0; y < dimension; y++)
            for (byte z = 0; z < dimension; z++)
                for (byte x = 0; x < dimension; x++)
                    consumer.accept(x, y, z, value);
    }

    public static long[] remap(int dimension, int oldBitsPerEntry, int newBitsPerEntry,
                               long[] values, Int2IntFunction function) {
        final long[] result = new long[arrayLength(dimension, newBitsPerEntry)];
        final int magicMask = (1 << oldBitsPerEntry) - 1;
        final int oldValuesPerLong = 64 / oldBitsPerEntry;
        final int newValuesPerLong = 64 / newBitsPerEntry;
        final int size = dimension * dimension * dimension;
        long newValue = 0;
        int newValueIndex = 0;
        int newBitIndex = 0;
        outer:
        {
            for (int i = 0; i < values.length; i++) {
                long value = values[i];
                final int startIndex = i * oldValuesPerLong;
                final int endIndex = Math.min(startIndex + oldValuesPerLong, size);
                for (int index = startIndex; index < endIndex; index++) {
                    final int paletteIndex = (int) (value & magicMask);
                    value >>>= oldBitsPerEntry;
                    newValue |= ((long) function.get(paletteIndex)) << (newBitIndex++ * newBitsPerEntry);
                    if (newBitIndex >= newValuesPerLong) {
                        result[newValueIndex++] = newValue;
                        if (newValueIndex == result.length) {
                            break outer;
                        }
                        newBitIndex = 0;
                        newValue = 0;
                    }
                }
            }
            result[newValueIndex] = newValue;
        }
        return result;
    }
}
