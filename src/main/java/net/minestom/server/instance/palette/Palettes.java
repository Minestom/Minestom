package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.IntPredicate;

/// Low level operations on the packed arrays backing a [Palette].
///
/// These operate on raw storage and do not validate their arguments. Passing a width, size, or array
/// that does not describe the same layout the caller packed produces undefined results or an exception.
///
/// ## Packed layout
///
/// Entries live in fixed width lanes inside a `long[]`. A width of `bitsPerEntry` fits `64 / bitsPerEntry`
/// lanes per long, and a lane never straddles two longs, so the high bits of each long go unused whenever
/// the width does not divide 64. Every method takes `bitsPerEntry` separately because the array alone does
/// not describe its own layout.
///
/// Widths are expected in `[1, 32)`. A width of `0` means single value mode, which has no packed array at
/// all, so these methods are never given one.
///
/// Methods taking a `size` stop there rather than at the end of the array, leaving the trailing lanes of
/// the final long untouched. Methods without one cover the whole array.
///
/// ## Values wider than a lane
///
/// Nothing here range checks the values it stores. [#pack(int\[\], int)] masks each value to the lane
/// width, so a wider value is truncated. [#write(int, int, long\[\], int, int, int, int)],
/// [#writeValue(int, int, long\[\], int, int, int, int)] and [#broadcast(int, int)] do not mask, so a wider
/// value overwrites neighbouring lanes. The lane oriented searches
/// ([#countEquals(int, long\[\], int, int)], [#anyEquals(int, long\[\], int, int)] and
/// [#replaceEquals(int, long\[\], int, int, int)]) need their target to fit the lane width and give
/// undefined results otherwise. Keeping values inside `[0, 1 << bitsPerEntry)` is the caller's job.
@ApiStatus.Experimental
public final class Palettes {
    private Palettes() {
    }

    /// Returns the smallest number of bits able to index `count` distinct values, meaning the smallest
    /// `bits` for which `1 << bits >= count`.
    ///
    /// @param count the number of distinct values to index
    /// @return the number of bits needed, or `0` when at most one value needs indexing
    public static int bitsToIndex(int count) {
        return count <= 1 ? 0 : MathUtils.bitsToRepresent(count - 1);
    }

    /// Returns the direct storage width able to hold every id of a registry holding `elementCount` entries.
    ///
    /// Never `0`, because a storage width of `0` cannot hold a value. A registry of at most one
    /// entry therefore yields `1`.
    ///
    /// @param elementCount the number of entries in the registry
    /// @return the direct storage width
    public static int directBits(int elementCount) {
        return Math.max(1, bitsToIndex(elementCount));
    }

    /// Packs every value of `ints` into fixed width lanes.
    ///
    /// Each value is masked to the lane width, so a value outside `[0, 1 << bitsPerEntry)` is truncated
    /// rather than rejected. The result holds `ceil(ints.length / (64 / bitsPerEntry))` longs, and any
    /// trailing lane of the final long that `ints` does not reach stays zero.
    ///
    /// @param ints         the values to pack, in lane order
    /// @param bitsPerEntry the lane width
    /// @return a newly allocated packed array
    public static long[] pack(int[] ints, int bitsPerEntry) {
        final int intsPerLong = 64 / bitsPerEntry;
        final long[] longs = new long[(ints.length + intsPerLong - 1) / intsPerLong];
        final long mask = (1L << bitsPerEntry) - 1L;
        // Only the final long can run past
        final int fullLongs = ints.length / intsPerLong;
        int index = 0;
        for (int i = 0; i < fullLongs; i++) {
            long packed = 0L;
            for (int lane = 0; lane < intsPerLong; lane++, index++) {
                packed |= (ints[index] & mask) << (lane * bitsPerEntry);
            }
            longs[i] = packed;
        }
        if (fullLongs < longs.length) {
            long packed = 0L;
            for (int lane = 0; index < ints.length; lane++, index++) {
                packed |= (ints[index] & mask) << (lane * bitsPerEntry);
            }
            longs[fullLongs] = packed;
        }
        return longs;
    }

    /// Unpacks the first `out.length` lanes of `in` into `out`.
    ///
    /// How many entries are read is decided by `out`, not by `in`, so `in` must hold at least that many
    /// lanes at this width.
    ///
    /// @param out          receives one value per lane, in lane order
    /// @param in           the packed array to read, must not be empty
    /// @param bitsPerEntry the lane width
    public static void unpack(int[] out, long[] in, int bitsPerEntry) {
        assert in.length != 0 : "unpack input array is zero";

        final int intsPerLong = 64 / bitsPerEntry;
        final long mask = (1L << bitsPerEntry) - 1L;
        for (int i = 0; i < out.length; i++) {
            final int longIndex = i / intsPerLong;
            final int subIndex = i % intsPerLong;
            out[i] = (int) ((in[longIndex] >>> (bitsPerEntry * subIndex)) & mask);
        }
    }

    /// Unpacks the first `size` lanes of `values`, resolving each through `palette`.
    ///
    /// @param size         the number of entries to unpack
    /// @param bitsPerEntry the lane width
    /// @param values       the packed array to read
    /// @param palette      maps a lane to the value it represents, or `null` when lanes already hold values
    /// @return a newly allocated array of `size` values
    public static int[] unpackValues(int size, int bitsPerEntry, long[] values, int @Nullable [] palette) {
        final int[] result = new int[size];
        final int valuesPerLong = 64 / bitsPerEntry;
        final int mask = (1 << bitsPerEntry) - 1;
        int index = 0;
        for (long block : values) {
            final int end = Math.min(valuesPerLong, size - index);
            for (int lane = 0; lane < end; lane++, index++) {
                final int storedValue = (int) (block & mask);
                result[index] = palette == null ? storedValue : palette[storedValue];
                block >>>= bitsPerEntry;
            }
        }
        return result;
    }

    /// Returns how many distinct values a lane of the given width can address.
    ///
    /// @param bitsPerEntry the lane width
    /// @return 1 << bitsPerEntry
    public static int maxPaletteSize(int bitsPerEntry) {
        return 1 << bitsPerEntry;
    }

    /// Returns how many entries a palette of the given side length holds.
    ///
    /// @param dimension the side length
    /// @return `dimension` cubed
    public static int maxSize(int dimension) {
        return dimension * dimension * dimension;
    }

    /// Returns how many longs one palette of the given side length needs at the given width.
    ///
    /// @param dimension    the side length of the palette
    /// @param bitsPerEntry the lane width
    /// @return the required length of the packed array
    public static int arrayLength(int dimension, int bitsPerEntry) {
        final int elementCount = maxSize(dimension);
        final int valuesPerLong = 64 / bitsPerEntry;
        return (elementCount + valuesPerLong - 1) / valuesPerLong;
    }

    /// Reads the raw lane at the given coordinates.
    ///
    /// The result is whatever the lane holds, which is a palette index for indirect storage and a value for
    /// direct storage. Use [#readValue(int, int, long\[\], int\[\], int, int, int)] to resolve indices.
    ///
    /// @param dimension    the side length of the palette
    /// @param bitsPerEntry the lane width
    /// @param values       the packed array to read
    /// @param x            the local X coordinate
    /// @param y            the local Y coordinate
    /// @param z            the local Z coordinate
    /// @return the raw lane content
    public static int read(int dimension, int bitsPerEntry, long[] values,
                           int x, int y, int z) {
        final int sectionIndex = sectionIndex(dimension, x, y, z);
        final int valuesPerLong = 64 / bitsPerEntry;
        final int index = sectionIndex / valuesPerLong;
        final int bitIndex = (sectionIndex - index * valuesPerLong) * bitsPerEntry;
        final int mask = (1 << bitsPerEntry) - 1;
        return (int) (values[index] >> bitIndex) & mask;
    }

    /// Reads the value at the given coordinates, resolving it through `palette`.
    ///
    /// @param dimension    the side length of the palette
    /// @param bitsPerEntry the lane width
    /// @param values       the packed array to read
    /// @param palette      maps a lane to the value it represents, or `null` when lanes already hold values
    /// @param x            the local X coordinate
    /// @param y            the local Y coordinate
    /// @param z            the local Z coordinate
    /// @return the stored value
    public static int readValue(int dimension, int bitsPerEntry, long[] values, int @Nullable [] palette,
                                int x, int y, int z) {
        final int sectionIndex = sectionIndex(dimension, x, y, z);
        final int valuesPerLong = 64 / bitsPerEntry;
        final int longIndex = sectionIndex / valuesPerLong;
        final int bitIndex = (sectionIndex - longIndex * valuesPerLong) * bitsPerEntry;
        final int storedValue = (int) (values[longIndex] >> bitIndex) & (1 << bitsPerEntry) - 1;
        return palette == null ? storedValue : palette[storedValue];
    }

    /// Returns the highest Y of the given column whose value matches the predicate.
    ///
    /// The column is scanned downwards from `dimension - 1` and the scan stops at the first match.
    ///
    /// @param dimension    the side length of the palette
    /// @param bitsPerEntry the lane width
    /// @param values       the packed array to read
    /// @param palette      maps a lane to the value it represents, or `null` when lanes already hold values
    /// @param x            the local X coordinate of the column
    /// @param z            the local Z coordinate of the column
    /// @param predicate    tests each entry of the column
    /// @return the highest matching Y, or `-1` when nothing matches
    public static int height(int dimension, int bitsPerEntry, long[] values, int @Nullable [] palette,
                             int x, int z, Palette.EntryPredicate predicate) {
        final int valuesPerLong = 64 / bitsPerEntry;
        final int mask = (1 << bitsPerEntry) - 1;
        // Hoisted out of sectionIndex, which would otherwise recompute them on every step. Each step
        // stays independent of the last so the loop keeps its instruction level parallelism.
        final int dimensionBits = MathUtils.bitsToRepresent(dimension - 1);
        final int columnIndex = (z << dimensionBits) | x;
        final int yShift = dimensionBits << 1;
        for (int y = dimension - 1; y >= 0; y--) {
            final int sectionIndex = (y << yShift) | columnIndex;
            final int longIndex = sectionIndex / valuesPerLong;
            final int bitIndex = (sectionIndex - longIndex * valuesPerLong) * bitsPerEntry;
            final int paletteIndex = (int) (values[longIndex] >> bitIndex) & mask;
            final int value = palette == null ? paletteIndex : palette[paletteIndex];
            if (predicate.get(x, y, z, value)) return y;
        }
        return -1;
    }

    /// Writes a lane at the given coordinates and returns what it held before.
    ///
    /// `value` is not masked, so a value outside `[0, 1 << bitsPerEntry)` corrupts neighbouring lanes.
    ///
    /// @param dimension    the side length of the palette
    /// @param bitsPerEntry the lane width
    /// @param values       the packed array to modify
    /// @param x            the local X coordinate
    /// @param y            the local Y coordinate
    /// @param z            the local Z coordinate
    /// @param value        the lane content to store
    /// @return the previous lane content
    public static int write(int dimension, int bitsPerEntry, long[] values,
                            int x, int y, int z, int value) {
        final int valuesPerLong = 64 / bitsPerEntry;
        final int sectionIndex = sectionIndex(dimension, x, y, z);
        final int index = sectionIndex / valuesPerLong;
        final int bitIndex = (sectionIndex - index * valuesPerLong) * bitsPerEntry;

        final long block = values[index];
        final long clear = (1L << bitsPerEntry) - 1L;
        final long oldBlock = block >> bitIndex & clear;
        values[index] = (block & ~(clear << bitIndex)) | ((long) value << bitIndex);
        return (int) oldBlock;
    }

    /// Writes a lane at the given coordinates, discarding what it held before.
    ///
    /// Equivalent to [#write(int, int, long\[\], int, int, int, int)] without the read back, and with the
    /// same requirement that `value` fit the lane width.
    ///
    /// @param dimension    the side length of the palette
    /// @param bitsPerEntry the lane width
    /// @param values       the packed array to modify
    /// @param x            the local X coordinate
    /// @param y            the local Y coordinate
    /// @param z            the local Z coordinate
    /// @param value        the lane content to store
    public static void writeValue(int dimension, int bitsPerEntry, long[] values,
                                  int x, int y, int z, int value) {
        final int valuesPerLong = 64 / bitsPerEntry;
        final int sectionIndex = sectionIndex(dimension, x, y, z);
        final int index = sectionIndex / valuesPerLong;
        final int bitIndex = (sectionIndex - index * valuesPerLong) * bitsPerEntry;
        final long clear = (1L << bitsPerEntry) - 1L;
        values[index] = (values[index] & ~(clear << bitIndex)) | ((long) value << bitIndex);
    }

    /// Copies a region of an indirect source into a direct target, resolving each lane through `sourcePalette`.
    ///
    /// The source bounds are half open, and the caller is expected to have clamped them so that every source
    /// coordinate shifted by the offset lands inside the target.
    ///
    /// @param dimension          the side length shared by both palettes
    /// @param targetBitsPerEntry the lane width of the target
    /// @param targetValues       the packed array to modify
    /// @param sourceBitsPerEntry the lane width of the source
    /// @param sourceValues       the packed array to read
    /// @param sourcePalette      maps a source lane to the value it represents
    /// @param sourceMinX         the inclusive lower X bound of the copied region
    /// @param sourceMinY         the inclusive lower Y bound of the copied region
    /// @param sourceMinZ         the inclusive lower Z bound of the copied region
    /// @param sourceMaxX         the exclusive upper X bound of the copied region
    /// @param sourceMaxY         the exclusive upper Y bound of the copied region
    /// @param sourceMaxZ         the exclusive upper Z bound of the copied region
    /// @param offsetX            the X offset applied when writing
    /// @param offsetY            the Y offset applied when writing
    /// @param offsetZ            the Z offset applied when writing
    public static void copyIndirectToDirect(int dimension,
                                            int targetBitsPerEntry, long[] targetValues,
                                            int sourceBitsPerEntry, long[] sourceValues, int[] sourcePalette,
                                            int sourceMinX, int sourceMinY, int sourceMinZ,
                                            int sourceMaxX, int sourceMaxY, int sourceMaxZ,
                                            int offsetX, int offsetY, int offsetZ) {
        for (int y = sourceMinY; y < sourceMaxY; y++) {
            for (int z = sourceMinZ; z < sourceMaxZ; z++) {
                for (int x = sourceMinX; x < sourceMaxX; x++) {
                    final int sourceIndex = read(dimension, sourceBitsPerEntry, sourceValues, x, y, z);
                    writeValue(dimension, targetBitsPerEntry, targetValues, x + offsetX, y + offsetY, z + offsetZ, sourcePalette[sourceIndex]);
                }
            }
        }
    }

    /// Sets every lane of `values` to `value`, including the trailing lanes of the final long.
    ///
    /// `value` is not masked, so it must fit the lane width.
    ///
    /// @param bitsPerEntry the lane width
    /// @param values       the packed array to modify
    /// @param value        the lane content to store everywhere
    public static void fill(int bitsPerEntry, long[] values, int value) {
        Arrays.fill(values, broadcast(bitsPerEntry, value));
    }

    /// Builds a 64-bit pattern with `value` placed in every `bitsPerEntry`-wide lane.
    ///
    /// `value` is not masked, so a wider value spills across lane boundaries.
    ///
    /// @param bitsPerEntry the lane width
    /// @param value        the lane content to repeat
    /// @return the repeated pattern
    public static long broadcast(int bitsPerEntry, int value) {
        final int valuesPerLong = 64 / bitsPerEntry;
        long pattern = 0L;
        for (int i = 0; i < valuesPerLong; i++) pattern |= (long) value << (i * bitsPerEntry);
        return pattern;
    }

    /// Counts the packed entries equal to `target` among the first `size` entries.
    /// Scans 64 bits at a time using borrow-safe SWAR zero-lane detection.
    ///
    /// `target` must fit in `bitsPerEntry` bits. A wider target is broadcast across lane boundaries and the
    /// result is undefined.
    ///
    /// @param bitsPerEntry the lane width
    /// @param values       the packed array to read
    /// @param size         the number of entries to consider
    /// @param target       the lane content to match
    /// @return the number of matching entries
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

    /// Returns true if any of the first `size` packed entries equals `target`.
    ///
    /// `target` must fit in `bitsPerEntry` bits, as for [#countEquals(int, long\[\], int, int)].
    ///
    /// @param bitsPerEntry the lane width
    /// @param values       the packed array to read
    /// @param size         the number of entries to consider
    /// @param target       the lane content to match
    /// @return `true` when at least one entry matches
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

    /// Returns true if every one of the first `size` packed entries equals `target`.
    ///
    /// Compares 64 bits at a time against the broadcast target and stops at the first differing long, so a
    /// mismatch near the start returns in a few nanoseconds. Lanes past `size` are ignored.
    ///
    /// `target` must fit in `bitsPerEntry` bits, as for [#countEquals(int, long\[\], int, int)].
    ///
    /// @param bitsPerEntry the lane width
    /// @param values       the packed array to read
    /// @param size         the number of entries to consider
    /// @param target       the lane content to match
    /// @return `true` when every entry matches
    public static boolean allEquals(int bitsPerEntry, long[] values, int size, int target) {
        final int valuesPerLong = 64 / bitsPerEntry;
        final long usedLanes = broadcast(bitsPerEntry, (1 << bitsPerEntry) - 1);
        final long broadcastTarget = broadcast(bitsPerEntry, target);
        final int fullLongs = size / valuesPerLong;
        for (int i = 0; i < fullLongs; i++) {
            if (((values[i] ^ broadcastTarget) & usedLanes) != 0) return false; // needs vector api
        }
        final int remaining = size - fullLongs * valuesPerLong;
        if (remaining == 0) return true;
        final long tailMask = (1L << (remaining * bitsPerEntry)) - 1;
        return ((values[fullLongs] ^ broadcastTarget) & tailMask) == 0;
    }

    /// Returns true if the predicate matches any of the first `size` packed entries.
    ///
    /// Entries are tested one lane at a time and evaluation stops at the first match.
    ///
    /// @param bitsPerEntry the lane width
    /// @param values       the packed array to read
    /// @param size         the number of entries to consider
    /// @param palette      maps a lane to the value it represents, or `null` when lanes already hold values
    /// @param predicate    tests each value
    /// @return `true` when at least one entry matches
    public static boolean anyMatch(int bitsPerEntry, long[] values, int size, int @Nullable [] palette,
                                   IntPredicate predicate) {
        final int valuesPerLong = 64 / bitsPerEntry;
        final int mask = (1 << bitsPerEntry) - 1;
        for (int i = 0, index = 0; i < values.length; i++) {
            long block = values[i];
            final int end = Math.min(valuesPerLong, size - index);
            for (int j = 0; j < end; j++, index++) {
                final int paletteIndex = (int) (block & mask);
                final int value = palette != null && paletteIndex < palette.length
                        ? palette[paletteIndex] : paletteIndex;
                if (predicate.test(value)) return true;
                block >>>= bitsPerEntry;
            }
        }
        return false;
    }

    /// Returns true if the predicate matches all of the first `size` packed entries.
    ///
    /// Entries are tested one lane at a time and evaluation stops at the first mismatch.
    ///
    /// @param bitsPerEntry the lane width
    /// @param values       the packed array to read
    /// @param size         the number of entries to consider
    /// @param palette      maps a lane to the value it represents, or `null` when lanes already hold values
    /// @param predicate    tests each value
    /// @return `true` when every entry matches
    public static boolean allMatch(int bitsPerEntry, long[] values, int size, int @Nullable [] palette,
                                   IntPredicate predicate) {
        final int valuesPerLong = 64 / bitsPerEntry;
        final int mask = (1 << bitsPerEntry) - 1;
        for (int i = 0, index = 0; i < values.length; i++) {
            long block = values[i];
            final int end = Math.min(valuesPerLong, size - index);
            for (int j = 0; j < end; j++, index++) {
                final int paletteIndex = (int) (block & mask);
                final int value = palette != null && paletteIndex < palette.length
                        ? palette[paletteIndex] : paletteIndex;
                if (!predicate.test(value)) return false;
                block >>>= bitsPerEntry;
            }
        }
        return true;
    }

    /// Counts entries matching the predicate among the first `size` packed entries.
    ///
    /// @param bitsPerEntry the lane width
    /// @param values       the packed array to read
    /// @param size         the number of entries to consider
    /// @param palette      maps a lane to the value it represents, or `null` when lanes already hold values
    /// @param predicate    tests each value
    /// @return the number of matching entries
    public static int countMatches(int bitsPerEntry, long[] values, int size, int @Nullable [] palette,
                                   IntPredicate predicate) {
        final int valuesPerLong = 64 / bitsPerEntry;
        final int mask = (1 << bitsPerEntry) - 1;
        int result = 0;
        for (int i = 0, index = 0; i < values.length; i++) {
            long block = values[i];
            final int end = Math.min(valuesPerLong, size - index);
            for (int j = 0; j < end; j++, index++) {
                final int paletteIndex = (int) (block & mask);
                final int value = palette != null && paletteIndex < palette.length
                        ? palette[paletteIndex] : paletteIndex;
                if (predicate.test(value)) result++;
                block >>>= bitsPerEntry;
            }
        }
        return result;
    }

    /// Replaces every packed entry equal to `oldValue` with `newValue` among the first
    /// `size` entries, returning the number of entries replaced.
    ///
    /// Both values must fit in `bitsPerEntry` bits. A wider value is broadcast across lane boundaries, and
    /// the array is left holding content that is neither the old nor the new value.
    ///
    /// @param bitsPerEntry the lane width
    /// @param values       the packed array to modify
    /// @param size         the number of entries to consider
    /// @param oldValue     the lane content to match
    /// @param newValue     the lane content to store in its place
    /// @return the number of entries replaced
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

    /// High bit set in each lane equal to `broadcastTarget`, restricted to the first
    /// `remaining` lanes. Borrow-safe so a zero lane never spills into its neighbour.
    private static long matchingLanes(long block, long broadcastTarget, long lowMask, long highBits,
                                      int remaining, int valuesPerLong, int bitsPerEntry) {
        final long x = block ^ broadcastTarget;
        final long t = (x & lowMask) + lowMask;
        long zeros = ~(t | x) & highBits;
        if (remaining < valuesPerLong) zeros &= (1L << (remaining * bitsPerEntry)) - 1L;
        return zeros;
    }

    /// Returns the lane index of the given coordinates.
    ///
    /// Entries are ordered Y major, then Z, then X, matching the vanilla section layout.
    ///
    /// @param dimension the side length of the palette
    /// @param x         the local X coordinate
    /// @param y         the local Y coordinate
    /// @param z         the local Z coordinate
    /// @return the index of the lane holding that entry
    public static int sectionIndex(int dimension, int x, int y, int z) {
        final int dimensionBitCount = MathUtils.bitsToRepresent(dimension - 1);
        return y << (dimensionBitCount << 1) | z << dimensionBitCount | x;
    }

    /// Checks that every lane of an indirect palette addresses an existing entry.
    ///
    /// Only the first `maxSize(dimension)` lanes are checked, so the trailing lanes of the final long may
    /// hold anything.
    ///
    /// @param bitsPerEntry the lane width
    /// @param dimension    the side length of the palette
    /// @param values       the packed array to check
    /// @param paletteSize  the number of entries the lanes may address
    /// @throws IllegalArgumentException if any lane addresses an entry at or beyond `paletteSize`
    public static void validateIndices(int bitsPerEntry, int dimension, long[] values, int paletteSize) {
        final int valuesPerLong = 64 / bitsPerEntry;
        final int size = maxSize(dimension);
        final int laneMask = (1 << bitsPerEntry) - 1;
        // Every representable lane addresses an entry, so nothing can be out of range
        if (paletteSize > laneMask) return;
        if ((paletteSize & paletteSize - 1) == 0) {
            // A power of two leaves the out of range lanes as exactly those with a bit at or above its
            // width, so one mask per long rejects a whole block at a time
            final long forbidden = broadcast(bitsPerEntry, laneMask & -paletteSize);
            boolean clean = true;
            for (int i = 0, idx = 0; i < values.length && clean; i++, idx += valuesPerLong) {
                long block = values[i];
                final int remaining = size - idx;
                if (remaining < valuesPerLong) block &= (1L << (remaining * bitsPerEntry)) - 1L;
                clean = (block & forbidden) == 0;
            }
            if (clean) return;
            // Fall through to report which lane offended
        }
        for (int i = 0, idx = 0; i < values.length; i++) {
            long block = values[i];
            final int end = Math.min(valuesPerLong, size - idx);
            for (int j = 0; j < end; j++, idx++) {
                final int paletteIdx = (int) (block & laneMask);
                if (paletteIdx >= paletteSize)
                    throw new IllegalArgumentException("Palette index out of range: " + paletteIdx + " >= " + paletteSize);
                block >>>= bitsPerEntry;
            }
        }
    }

    // Optimized operations

    /// Reports every coordinate of a palette with the same value, as single value mode stores it.
    ///
    /// @param dimension the side length of the palette
    /// @param value     the value reported for every coordinate
    /// @param consumer  receives each entry
    public static void getAllFill(byte dimension, int value, Palette.EntryConsumer consumer) {
        for (byte y = 0; y < dimension; y++)
            for (byte z = 0; z < dimension; z++)
                for (byte x = 0; x < dimension; x++)
                    consumer.accept(x, y, z, value);
    }

    /// Reports every entry of a direct palette, walking the packed array in lane order.
    ///
    /// Coordinates are derived from the lane index, so entries arrive Y major.
    ///
    /// @param dimension    the side length of the palette
    /// @param bitsPerEntry the lane width
    /// @param values       the packed array to read
    /// @param consumer     receives each entry
    public static void getAllDirect(int dimension, int bitsPerEntry, long[] values,
                                    Palette.EntryConsumer consumer) {
        final int mask = (1 << bitsPerEntry) - 1;
        final int valuesPerLong = 64 / bitsPerEntry;
        final int size = maxSize(dimension);
        final int dimensionMask = dimension - 1;
        final int dimensionBits = MathUtils.bitsToRepresent(dimensionMask);
        final int shiftedDimensionBits = dimensionBits << 1;
        for (int i = 0; i < values.length; i++) {
            final long value = values[i];
            final int startIndex = i * valuesPerLong;
            final int endIndex = Math.min(startIndex + valuesPerLong, size);
            for (int index = startIndex; index < endIndex; index++) {
                final int bitIndex = (index - startIndex) * bitsPerEntry;
                final int paletteIndex = (int) (value >> bitIndex & mask);
                final int y = index >> shiftedDimensionBits;
                final int z = index >> dimensionBits & dimensionMask;
                final int x = index & dimensionMask;
                consumer.accept(x, y, z, paletteIndex);
            }
        }
    }

    /// Reports every entry of an indirect palette, resolving each lane through `palette`.
    ///
    /// @param dimension    the side length of the palette
    /// @param bitsPerEntry the lane width
    /// @param values       the packed array to read
    /// @param palette      maps a lane to the value it represents
    /// @param consumer     receives each entry
    public static void getAllIndirect(int dimension, int bitsPerEntry, long[] values, int[] palette,
                                      Palette.EntryConsumer consumer) {
        final int mask = (1 << bitsPerEntry) - 1;
        final int valuesPerLong = 64 / bitsPerEntry;
        final int size = maxSize(dimension);
        final int dimensionMask = dimension - 1;
        final int dimensionBits = MathUtils.bitsToRepresent(dimensionMask);
        final int shiftedDimensionBits = dimensionBits << 1;
        for (int i = 0; i < values.length; i++) {
            final long value = values[i];
            final int startIndex = i * valuesPerLong;
            final int endIndex = Math.min(startIndex + valuesPerLong, size);
            for (int index = startIndex; index < endIndex; index++) {
                final int bitIndex = (index - startIndex) * bitsPerEntry;
                final int paletteIndex = (int) (value >> bitIndex & mask);
                final int y = index >> shiftedDimensionBits;
                final int z = index >> dimensionBits & dimensionMask;
                final int x = index & dimensionMask;
                consumer.accept(x, y, z, palette[paletteIndex]);
            }
        }
    }

    /// Reports each distinct lane content among the first `size` entries with how often it occurs.
    ///
    /// Contents are reported in an unspecified order, each exactly once, and the reported counts sum
    /// to `size`.
    ///
    /// The tally is a directly indexed array, so this allocates `4 << bitsPerEntry` bytes regardless of how
    /// many distinct values are actually present. That is 128 KiB at the widest block storage, which still
    /// measures faster than hashing every lane. Distinct contents are recorded on first touch, so emission
    /// visits only the values present rather than the whole domain.
    ///
    /// @param bitsPerEntry the lane width
    /// @param values       the packed array to read
    /// @param size         the number of entries to consider
    /// @param consumer     receives each distinct lane content and its count
    public static void getAllCounts(int bitsPerEntry, long[] values, int size,
                                    Palette.ValueCountConsumer consumer) {
        final int valuesPerLong = 64 / bitsPerEntry;
        final int mask = (1 << bitsPerEntry) - 1;
        final int[] counts = new int[1 << bitsPerEntry];
        final int[] touched = new int[Math.min(size, counts.length)];
        int distinct = 0;
        int index = 0;
        for (long block : values) {
            final int end = Math.min(valuesPerLong, size - index);
            for (int lane = 0; lane < end; lane++, index++) {
                final int value = (int) (block & mask);
                if (counts[value]++ == 0) touched[distinct++] = value;
                block >>>= bitsPerEntry;
            }
        }
        for (int i = 0; i < distinct; i++) {
            final int value = touched[i];
            consumer.accept(value, counts[value]);
        }
    }

    /// Compares two palettes entry by entry from the given starting entry.
    ///
    /// Either side may be in single value mode, in which case its width is `0`, its packed array
    /// goes unread, and its single value stands in for every entry.
    ///
    /// @param size               the number of entries held by both palettes
    /// @param start              the first compared entry, must be in `[0, size]`, entries before it are ignored
    /// @param firstBitsPerEntry  the lane width of the first palette, or `0` for single value mode
    /// @param firstSingleValue   the single value of the first palette, read only in single value mode
    /// @param firstValues        the packed array of the first palette, unread in single value mode
    /// @param firstPalette       maps a first lane to its value, or `null` when lanes already hold values
    /// @param secondBitsPerEntry the lane width of the second palette, or `0` for single value mode
    /// @param secondSingleValue  the single value of the second palette, read only in single value mode
    /// @param secondValues       the packed array of the second palette, unread in single value mode
    /// @param secondPalette      maps a second lane to its value, or `null` when lanes already hold values
    /// @return `true` when every entry from `start` onwards is equal
    @SuppressWarnings("DataFlowIssue") // firstValues and firstPalette are interconnected in nullability
    public static boolean compare(int size, int start,
                                  int firstBitsPerEntry, int firstSingleValue, long @Nullable [] firstValues,
                                  int @Nullable [] firstPalette,
                                  int secondBitsPerEntry, int secondSingleValue, long @Nullable [] secondValues,
                                  int @Nullable [] secondPalette) {
        if (start >= size) return true;
        final int firstMask = firstBitsPerEntry == 0 ? 0 : (1 << firstBitsPerEntry) - 1;
        final int secondMask = secondBitsPerEntry == 0 ? 0 : (1 << secondBitsPerEntry) - 1;
        final int firstValuesPerLong = firstBitsPerEntry == 0 ? 0 : 64 / firstBitsPerEntry;
        final int secondValuesPerLong = secondBitsPerEntry == 0 ? 0 : 64 / secondBitsPerEntry;
        if (firstBitsPerEntry == secondBitsPerEntry && firstBitsPerEntry != 0
                && firstPalette == null && secondPalette == null) {
            // Identical raw layouts, so the packed arrays compare whole through a vectorized mismatch
            final int startLong = start / firstValuesPerLong;
            final int startLane = start - startLong * firstValuesPerLong;
            int from = startLong;
            if (startLane != 0) {
                // Partial first long: compare only the lanes from the starting one onward
                long mask = -(1L << (startLane * firstBitsPerEntry));
                final int startUsed = size - startLong * firstValuesPerLong;
                if (startUsed < firstValuesPerLong) mask &= (1L << (startUsed * firstBitsPerEntry)) - 1L;
                if (((firstValues[startLong] ^ secondValues[startLong]) & mask) != 0) return false;
                from = startLong + 1;
            }
            final int relative = Arrays.mismatch(firstValues, from, firstValues.length,
                    secondValues, from, secondValues.length);
            if (relative < 0) return true;
            final int differing = from + relative;
            if (differing < firstValues.length - 1) return false;
            // Only the final long differs, which may be entirely in lanes past the end
            final int used = size - differing * firstValuesPerLong;
            if (used >= firstValuesPerLong) return false;
            final long usedMask = (1L << (used * firstBitsPerEntry)) - 1L;
            return (firstValues[differing] & usedMask) == (secondValues[differing] & usedMask);
        }
        // Lane cursors are stepped rather than derived, keeping the divisions out of the loop
        long firstBlock = 0, secondBlock = 0;
        int firstLane = 0, firstLongIndex = 0;
        int secondLane = 0, secondLongIndex = 0;
        if (firstBitsPerEntry != 0) {
            firstLongIndex = start / firstValuesPerLong;
            firstLane = start - firstLongIndex * firstValuesPerLong;
            firstBlock = firstValues[firstLongIndex] >>> (firstLane * firstBitsPerEntry);
        }
        if (secondBitsPerEntry != 0) {
            secondLongIndex = start / secondValuesPerLong;
            secondLane = start - secondLongIndex * secondValuesPerLong;
            secondBlock = secondValues[secondLongIndex] >>> (secondLane * secondBitsPerEntry);
        }
        for (int index = start; index < size; index++) {
            if (firstBitsPerEntry != 0 && firstLane == firstValuesPerLong) {
                firstLane = 0;
                firstBlock = firstValues[++firstLongIndex];
            }
            int first = firstBitsPerEntry == 0 ? firstSingleValue : (int) (firstBlock & firstMask);
            firstBlock >>>= firstBitsPerEntry;
            firstLane++;
            if (firstPalette != null) first = firstPalette[first];

            if (secondBitsPerEntry != 0 && secondLane == secondValuesPerLong) {
                secondLane = 0;
                secondBlock = secondValues[++secondLongIndex];
            }
            int second = secondBitsPerEntry == 0 ? secondSingleValue : (int) (secondBlock & secondMask);
            secondBlock >>>= secondBitsPerEntry;
            secondLane++;
            if (secondPalette != null) second = secondPalette[second];
            if (first != second) return false;
        }
        return true;
    }

    /// Repacks a palette at a new width, passing each lane through `function`.
    ///
    /// Only the first `maxSize(dimension)` lanes are read, and the result is sized for the new width. The
    /// function receives raw lane content and returns the lane content to store, so it can renumber indices,
    /// resolve them to values, or leave them alone. Its result is not masked and must fit `newBitsPerEntry`.
    ///
    /// @param dimension       the side length of the palette
    /// @param oldBitsPerEntry the lane width of `values`
    /// @param newBitsPerEntry the lane width of the result
    /// @param values          the packed array to read
    /// @param function        maps each old lane content to its new lane content
    /// @return a newly allocated packed array at the new width
    @SuppressWarnings("LabelledBreakTarget")
    public static long[] remap(int dimension, int oldBitsPerEntry, int newBitsPerEntry,
                               long[] values, Int2IntFunction function) {
        final long[] result = new long[arrayLength(dimension, newBitsPerEntry)];
        final int magicMask = (1 << oldBitsPerEntry) - 1;
        final int oldValuesPerLong = 64 / oldBitsPerEntry;
        final int newValuesPerLong = 64 / newBitsPerEntry;
        final int size = maxSize(dimension);
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
