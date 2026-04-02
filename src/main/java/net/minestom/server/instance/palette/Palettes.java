package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;

@ApiStatus.Internal
public final class Palettes {
    private Palettes() {
    }

    public static long[] pack(int[] ints, int bitsPerEntry) {
        final int intsPerLong = (int) Math.floor(64d / bitsPerEntry);
        final long[] longs = new long[(int) Math.ceil(ints.length / (double) intsPerLong)];
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

    public static int arrayLength(int dimension, int bitsPerEntry) {
        final int elementCount = dimension * dimension * dimension;
        final int valuesPerLong = 64 / bitsPerEntry;
        return (elementCount + valuesPerLong - 1) / valuesPerLong;
    }

    public static int directBitsPerEntry(int maxValue, int maxBitsPerEntry, int directBits) {
        return Math.clamp(64 / (64 / MathUtils.bitsToRepresent(maxValue)), maxBitsPerEntry + 1, directBits);
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

    public static int sectionIndex(int dimension, int x, int y, int z) {
        final int dimensionBitCount = dimensionBits(dimension);
        return y << (dimensionBitCount << 1) | z << dimensionBitCount | x;
    }

    public static int paletteBits(int paletteSize) {
        return Integer.SIZE - Integer.numberOfLeadingZeros(paletteSize - 1);
    }

    public static int dimensionBits(int dimension) {
        return Integer.numberOfTrailingZeros(dimension); // Always a power of 2
    }

    // Validation

    public static void validateCoord(int dimension, int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0)
            throw new IllegalArgumentException("Coordinates must be non-negative");
        if (x >= dimension || y >= dimension || z >= dimension)
            throw new IllegalArgumentException("Coordinates must be less than the dimension size, got " + x + ", " + y + ", " + z + " for dimension " + dimension);
    }

    public static void validateValue(int value, int directBits) {
        if (value < 0)
            throw new IllegalArgumentException("Palette values must be non-negative");
        if (value >= (1 << directBits))
            throw new IllegalArgumentException("Palette values must fit in the direct bits size");
    }

    public static void validateDimension(int dimension) {
        if (dimension <= 1 || (dimension & dimension - 1) != 0)
            throw new IllegalArgumentException("Dimension must be a positive power of 2, got " + dimension);
    }

    // Optimized operations

    public static void getAllFill(byte dimension, int value, Palette.EntryConsumer consumer) {
        for (byte y = 0; y < dimension; y++)
            for (byte z = 0; z < dimension; z++)
                for (byte x = 0; x < dimension; x++)
                    consumer.accept(x, y, z, value);
    }

    public static int count(int dimension, int bitsPerEntry, long[] values, int paletteIndex) {
        if (paletteIndex < 0) return 0;
        int result = 0;
        final int size = dimension * dimension * dimension;
        final int valuesPerLong = 64 / bitsPerEntry;
        final int mask = (1 << bitsPerEntry) - 1;
        for (int i = 0, idx = 0; i < values.length; i++) {
            long block = values[i];
            final int end = Math.min(valuesPerLong, size - idx);
            for (int j = 0; j < end; j++, idx++) {
                if (((int) (block & mask)) == paletteIndex) result++;
                block >>>= bitsPerEntry;
            }
        }
        return result;
    }

    public static int singleValue(int dimension, int bitsPerEntry, long[] values) {
        int result = -1;
        final int size = dimension * dimension * dimension;
        final int valuesPerLong = 64 / bitsPerEntry;
        final int mask = (1 << bitsPerEntry) - 1;
        for (int i = 0, idx = 0; i < values.length; i++) {
            long block = values[i];
            final int end = Math.min(valuesPerLong, size - idx);
            for (int j = 0; j < end; j++, idx++) {
                if (result < 0) {
                    result = (int) (block & mask);
                } else if (result != (block & mask)) {
                    return -1;
                }
                block >>>= bitsPerEntry;
            }
        }
        return result;
    }

    public static long[] remap(int dimension, int oldBitsPerEntry, int newBitsPerEntry,
                               long[] values, Int2IntFunction function) {
        return remap(dimension, oldBitsPerEntry, newBitsPerEntry, values, false, function);
    }

    public static long[] remap(int dimension, int oldBitsPerEntry, int newBitsPerEntry,
                               long[] values, boolean forceRealloc, Int2IntFunction function) {
        final long[] result = forceRealloc || oldBitsPerEntry != newBitsPerEntry ?
                new long[arrayLength(dimension, newBitsPerEntry)] : values;
        final int magicMask = (1 << oldBitsPerEntry) - 1;
        final int oldValuesPerLong = 64 / oldBitsPerEntry;
        final int newValuesPerLong = 64 / newBitsPerEntry;
        final int size = dimension * dimension * dimension;
        long newValue = 0;
        int newValueIndex = 0;
        int newBitIndex = 0;
        outer: {
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

    public static int fill(
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ,
            int paletteIndex, int airPaletteIndex,
            long[] values, int dimension, int bitsPerEntry) {
        int countDelta = 0;
        if (paletteIndex == airPaletteIndex) {
            countDelta -= (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        }

        final int dimensionMinus = dimension - 1;
        final int dimensionBits = dimensionBits(dimension);
        final int finalXTravel = dimensionMinus - maxX;
        final int initialZTravel = minZ << dimensionBits;
        final int finalZTravel = (dimensionMinus - maxZ) << dimensionBits;

        final int valuesPerLong = 64 / bitsPerEntry;
        final int maxBitIndex = bitsPerEntry * valuesPerLong;
        final int mask = (1 << bitsPerEntry) - 1;

        int index = minY << (dimensionBits << 1);
        for (int y = minY; y <= maxY; y++) {
            index += initialZTravel;
            for (int z = minZ; z <= maxZ; z++) {
                index += minX;
                int blockIndex = index / valuesPerLong;
                int bitIndex = (index % valuesPerLong) * bitsPerEntry;
                long block = values[blockIndex];
                for (int x = minX; x <= maxX; x++) {
                    if (bitIndex >= maxBitIndex) {
                        values[blockIndex] = block;
                        bitIndex = 0;
                        blockIndex++;
                        block = values[blockIndex];
                    }

                    if (((block >>> bitIndex) & mask) == airPaletteIndex) countDelta++;
                    block = (block & ~(((long) mask) << bitIndex)) | (((long) paletteIndex) << bitIndex);

                    bitIndex += bitsPerEntry;
                    index++;
                }
                values[blockIndex] = block;
                index += finalXTravel;
            }
            index += finalZTravel;
        }
        return countDelta;
    }
}
