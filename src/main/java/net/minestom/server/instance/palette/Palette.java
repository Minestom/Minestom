package net.minestom.server.instance.palette;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.LONG;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT_ARRAY;

/// Palette is an integer storage with three models used to store block states, biomes, and other compact values.
/// It does not assign semantic meaning to any value; callers decide which values represent air, defaults, or sentinels.
///
/// Single Value Mode `(bitsPerEntry == 0)`: All entries have the same value.
/// No arrays allocated, value stored directly.
///
/// Indirect Mode: The packed array stores palette indices. Small palettes use a linear reverse
/// lookup, while larger palettes use a hash index.
///
/// Direct Mode: The packed array stores values directly and has no palette lookup structure.
/// It is only used when its width exceeds the largest indirect width, so a palette sized for a registry
/// that already fits indirect storage stays indirect.
///
/// You can optimize for space/speed using [#optimize(Optimization)]
///
/// A palette holds `dimension * dimension * dimension` entries addressed by local coordinates, each in the range `[0, dimension)`.
/// Methods taking coordinates throw [IllegalArgumentException] when a coordinate is outside that range.
///
/// Implementations are mutable and not thread safe. Concurrent reads are safe only when no thread writes.
/// The storage mode is an implementation detail that any mutation may change.
public sealed interface Palette extends Cloneable permits PaletteImpl {
    /// Side length of a block palette, meaning one chunk section.
    int BLOCK_DIMENSION = 16;
    /// Smallest indirect storage width of a block palette.
    int BLOCK_PALETTE_MIN_BITS = 4;
    /// Largest indirect storage width of a block palette. Wider content switches to direct storage.
    int BLOCK_PALETTE_MAX_BITS = 8;
    /// Direct storage width of a block palette, wide enough for every block state id.
    int BLOCK_PALETTE_DIRECT_BITS = 15;

    /// Side length of a biome palette, meaning one quarter of a chunk section on each axis.
    int BIOME_DIMENSION = 4;
    /// Smallest indirect storage width of a biome palette.
    int BIOME_PALETTE_MIN_BITS = 1;
    /// Largest indirect storage width of a biome palette. Wider content switches to direct storage.
    int BIOME_PALETTE_MAX_BITS = 3;

    /// Creates a block palette with the requested initial storage width.
    ///
    /// @param bitsPerEntry the initial number of bits per entry
    /// @return a new block palette
    static Palette blocks(int bitsPerEntry) {
        return sized(BLOCK_DIMENSION, BLOCK_PALETTE_MIN_BITS, BLOCK_PALETTE_MAX_BITS, BLOCK_PALETTE_DIRECT_BITS, bitsPerEntry);
    }

    /// Creates an empty block palette, meaning every entry is `0`.
    ///
    /// @return a new block palette
    static Palette blocks() {
        return empty(BLOCK_DIMENSION, BLOCK_PALETTE_MIN_BITS, BLOCK_PALETTE_MAX_BITS, BLOCK_PALETTE_DIRECT_BITS);
    }

    /// Creates an empty biome palette, meaning every entry is `0`, whose direct storage can represent every entry in a registry.
    ///
    /// @param biomeCount the number of entries in the biome registry
    /// @return a new empty biome palette
    static Palette biomes(int biomeCount) {
        return empty(BIOME_DIMENSION, BIOME_PALETTE_MIN_BITS, BIOME_PALETTE_MAX_BITS, Palettes.directBits(biomeCount));
    }

    /// Creates a biome palette with the requested initial storage width, whose direct storage can
    /// represent every entry in a registry.
    ///
    /// @param biomeCount   the number of entries in the biome registry
    /// @param bitsPerEntry the initial number of bits per entry
    /// @return a new biome palette
    static Palette biomes(int biomeCount, int bitsPerEntry) {
        return sized(BIOME_DIMENSION, BIOME_PALETTE_MIN_BITS, BIOME_PALETTE_MAX_BITS, Palettes.directBits(biomeCount), bitsPerEntry);
    }

    /// Creates an empty palette, meaning every entry is `0`, in single value mode.
    ///
    /// @param dimension       the side length of the palette, must be a power of two greater than 1
    /// @param minBitsPerEntry the smallest indirect storage width
    /// @param maxBitsPerEntry the largest indirect storage width, above which direct storage is used
    /// @param directBits      the storage width used by direct mode, wide enough for every representable value
    /// @return a new empty palette
    /// @throws IllegalArgumentException if the dimension is not a power of two greater than 1
    static Palette empty(int dimension, int minBitsPerEntry, int maxBitsPerEntry, int directBits) {
        return new PaletteImpl((byte) dimension, (byte) minBitsPerEntry, (byte) maxBitsPerEntry, (byte) directBits);
    }

    /// Creates an empty palette, meaning every entry is `0`, with the requested initial storage width.
    ///
    /// A width of `0` starts the palette in single value mode, which is what [#empty(int, int, int, int)]
    /// does. Any other width preallocates the packed array, avoiding the resize a first [#set(int, int, int, int)]
    /// would otherwise trigger.
    ///
    /// @param dimension       the side length of the palette, must be a power of two greater than 1
    /// @param minBitsPerEntry the smallest indirect storage width
    /// @param maxBitsPerEntry the largest indirect storage width, above which direct storage is used
    /// @param directBits      the storage width used by direct mode, wide enough for every representable value
    /// @param bitsPerEntry    the initial number of bits per entry
    /// @return a new empty palette
    /// @throws IllegalArgumentException if the dimension is not a power of two greater than 1
    static Palette sized(int dimension, int minBitsPerEntry, int maxBitsPerEntry, int directBits, int bitsPerEntry) {
        return new PaletteImpl((byte) dimension, (byte) minBitsPerEntry, (byte) maxBitsPerEntry, (byte) directBits, (byte) bitsPerEntry);
    }

    /// Returns the value stored at the given coordinates.
    ///
    /// @param x the local X coordinate
    /// @param y the local Y coordinate
    /// @param z the local Z coordinate
    /// @return the stored value
    /// @throws IllegalArgumentException if a coordinate is outside `[0, dimension())`
    int get(int x, int y, int z);

    /// Consumes every entry of this palette, including entries whose value is `0`.
    ///
    /// Entries are reported in an unspecified order. The consumer must not modify this palette.
    ///
    /// @param consumer the consumer receiving each entry
    void getAll(EntryConsumer consumer);

    /// Returns the highest Y coordinate of the given column whose entry matches the predicate.
    ///
    /// The column is scanned downwards from `dimension() - 1`, and the scan stops at the first match.
    /// The predicate must not modify this palette.
    ///
    /// @param x         the local X coordinate of the column
    /// @param z         the local Z coordinate of the column
    /// @param predicate the predicate used to test each entry of the column
    /// @return the highest matching Y coordinate, or `-1` if no entry matches
    /// @throws IllegalArgumentException if a coordinate is outside `[0, dimension())`
    int height(int x, int z, EntryPredicate predicate);

    /// Stores a value at the given coordinates.
    ///
    /// The storage mode may change to fit the new value.
    ///
    /// @param x     the local X coordinate
    /// @param y     the local Y coordinate
    /// @param z     the local Z coordinate
    /// @param value the value to store
    /// @throws IllegalArgumentException if a coordinate is outside `[0, dimension())`
    void set(int x, int y, int z, int value);

    /// Sets every entry to the given value, releasing any allocated storage.
    ///
    /// @param value the value to store in every entry
    void fill(int value);

    /// Replaces the content of this palette with packed data, as stored on disk or received over the network.
    ///
    /// `palette` maps a packed index to the value it represents, and `values` holds one packed index
    /// per entry, in the same layout used by [#indexedValues()]. The width of a packed index is derived from
    /// the length of `palette`, so `values` must have been packed with that same width. Duplicate
    /// entries in `palette` are merged.
    ///
    /// @param palette the values addressed by the packed indices, must not be empty
    /// @param values  the packed indices, one per entry of this palette
    /// @throws IllegalArgumentException if `palette` is empty
    void load(int[] palette, long[] values);

    /// Adds the given offset to every stored value.
    ///
    /// @param offset the value added to every entry
    void offset(int offset);

    /// Replaces every entry equal to `oldValue` with `newValue`.
    ///
    /// @param oldValue the value to replace
    /// @param newValue the replacement value
    void replace(int oldValue, int newValue);

    /// Replaces every entry with the value returned by the supplier.
    ///
    /// The supplier is called exactly once per coordinate, in an unspecified order, and must not modify
    /// this palette.
    ///
    /// @param supplier the supplier providing the new value of each entry
    void setAll(EntrySupplier supplier);

    /// Replaces the entry at the given coordinates with the result of applying the operator to its current value.
    ///
    /// The operator must not modify this palette.
    ///
    /// @param x        the local X coordinate
    /// @param y        the local Y coordinate
    /// @param z        the local Z coordinate
    /// @param operator the operator applied to the current value
    /// @throws IllegalArgumentException if a coordinate is outside `[0, dimension())`
    void replace(int x, int y, int z, IntUnaryOperator operator);

    /// Replaces every entry with the result of applying the function to its current value.
    ///
    /// The function is called exactly once per coordinate, in an unspecified order, and must not modify
    /// this palette.
    ///
    /// @param function the function applied to each entry
    void replaceAll(EntryFunction function);

    /// Efficiently copies values from another palette with the given offset.
    ///
    /// Both palettes must have the same dimension.
    ///
    /// @param source  the source palette to copy from
    /// @param offsetX the X offset to apply when copying
    /// @param offsetY the Y offset to apply when copying
    /// @param offsetZ the Z offset to apply when copying
    /// @throws IllegalArgumentException if the palettes have different dimensions
    void copyFrom(Palette source, int offsetX, int offsetY, int offsetZ);

    /// Efficiently copies values from another palette starting at position (0, 0, 0).
    ///
    /// Both palettes must have the same dimension.
    ///
    /// This is a convenience method equivalent to calling `copyFrom(source, 0, 0, 0)`.
    ///
    /// @param source the source palette to copy from
    /// @throws IllegalArgumentException if the palettes have different dimensions
    void copyFrom(Palette source);

    /// Returns the number of entries in this palette that match the given value.
    ///
    /// @param value the value to count
    /// @return the number of entries matching the value
    int count(int value);

    /// Returns the number of entries whose value matches the predicate.
    ///
    /// Values are evaluated in an unspecified order and the same value may be evaluated more than once.
    ///
    /// @param predicate the predicate used to test stored values
    /// @return the number of entries whose value matches the predicate
    int count(IntPredicate predicate);

    /// Reports every value present in this palette and its occurrence count.
    ///
    /// Values are reported in an unspecified order and exactly once. The sum of all reported counts is
    /// [#maxSize()].
    ///
    /// @param consumer receives each value and its occurrence count
    void getAllCounts(ValueCountConsumer consumer);

    /// Checks if the palette contains the given value.
    ///
    /// @param value the value to check
    /// @return true if the palette contains the value, false otherwise
    boolean any(int value);

    /// Checks whether the predicate matches any value stored in this palette.
    ///
    /// Evaluation stops as soon as the predicate returns `true`. Values are evaluated in an unspecified
    /// order and the same value may be evaluated more than once.
    ///
    /// @param predicate the predicate used to test stored values
    /// @return `true` if any stored value matches the predicate
    boolean any(IntPredicate predicate);

    /// Checks whether every entry in this palette is the given value.
    ///
    /// @param value the value to check
    /// @return true if every entry is the value, false otherwise
    boolean all(int value);

    /// Checks whether the predicate matches every value stored in this palette.
    ///
    /// Evaluation stops as soon as the predicate returns `false`. Values are evaluated in an unspecified
    /// order and the same value may be evaluated more than once.
    ///
    /// @param predicate the predicate used to test stored values
    /// @return `true` if every stored value matches the predicate
    boolean all(IntPredicate predicate);

    /// Returns the number of bits used per entry.
    ///
    /// A width of `0` means single value mode. Any other width means the entries are packed, either as
    /// palette indices or as values, which cannot be told apart from the width alone.
    ///
    /// @return the current number of bits per entry
    int bitsPerEntry();

    /// Returns the side length of this palette. Coordinates are valid in `[0, dimension())`.
    ///
    /// @return the side length of this palette
    int dimension();

    /// Returns the maximum number of entries in this palette.
    default int maxSize() {
        return Palettes.maxSize(dimension());
    }

    /// Attempts to optimize the current [Palette]
    ///
    /// If plausible the only optimization will be performed is converting to a single value regardless of [Optimization]
    /// @param focus the optimization focus
    void optimize(Optimization focus);

    /// An optimization mode to use with [#optimize(Optimization)]
    enum Optimization {
        /// Will attempt to make indirect to save space.
        SIZE,
        /// Will attempt to make direct to reduce lookup.
        ///
        /// Has no effect when direct storage is unavailable, meaning its width does not exceed the largest
        /// indirect width.
        SPEED,
    }

    /// Compare palettes content independently of their storage format.
    ///
    /// @param palette the palette to compare with
    /// @return true if the palettes are equivalent, false otherwise
    boolean compare(Palette palette);

    /// Returns an independent copy of this palette, with the same configuration, content, and storage mode.
    ///
    /// @return a copy of this palette
    Palette clone();

    /// Resolves a packed index to the value it represents.
    ///
    /// The index is returned unchanged when the palette does not store indices.
    ///
    /// @param value the packed index to resolve
    /// @return the value represented by the index
    @ApiStatus.Internal
    int paletteIndexToValue(int value);

    /// Returns the packed index representing the given value, registering it if needed.
    ///
    /// This may change the storage mode and width of the palette, invalidating any index obtained earlier.
    /// The value is returned unchanged when the palette does not store indices.
    ///
    /// @param value the value to resolve
    /// @return the packed index representing the value
    @ApiStatus.Internal
    int valueToPaletteIndex(int value);

    /// Gets the single value stored in this palette.
    ///
    /// Only meaningful while [#bitsPerEntry()] is `0`. Other modes return the value the palette last
    /// held in single value mode, which is stale.
    ///
    /// @return the value every entry holds while in single value mode
    @ApiStatus.Internal
    int singleValue();

    /// Gets a snapshot of the packed value array, or `null` for a single-value palette.
    /// Mutating the returned array does not modify this palette.
    @ApiStatus.Internal
    long @Nullable [] indexedValues();

    /// Supplies the value of an entry from its coordinates.
    @FunctionalInterface
    interface EntrySupplier {
        /// Returns the value to store at the given coordinates.
        ///
        /// @param x the local X coordinate
        /// @param y the local Y coordinate
        /// @param z the local Z coordinate
        /// @return the value to store
        int get(int x, int y, int z);
    }

    /// Receives an entry of a palette.
    @FunctionalInterface
    interface EntryConsumer {
        /// Consumes the entry at the given coordinates.
        ///
        /// @param x     the local X coordinate
        /// @param y     the local Y coordinate
        /// @param z     the local Z coordinate
        /// @param value the stored value
        void accept(int x, int y, int z, int value);
    }

    /// Computes the new value of an entry from its coordinates and current value.
    @FunctionalInterface
    interface EntryFunction {
        /// Returns the value replacing the entry at the given coordinates.
        ///
        /// @param x     the local X coordinate
        /// @param y     the local Y coordinate
        /// @param z     the local Z coordinate
        /// @param value the current value
        /// @return the value to store
        int apply(int x, int y, int z, int value);
    }

    /// Tests an entry of a palette.
    @FunctionalInterface
    interface EntryPredicate {
        /// Tests the entry at the given coordinates.
        ///
        /// @param x     the local X coordinate
        /// @param y     the local Y coordinate
        /// @param z     the local Z coordinate
        /// @param value the stored value
        /// @return `true` if the entry matches
        boolean get(int x, int y, int z, int value);
    }

    /// Receives the occurrence count of a value stored in a palette.
    @FunctionalInterface
    interface ValueCountConsumer {
        /// Consumes a value and its positive occurrence count.
        ///
        /// @param value the stored value
        /// @param count the number of occurrences
        void accept(int value, int count);
    }

    /// Serializer for block palettes, meaning one chunk section worth of block states.
    NetworkBuffer.Type<Palette> BLOCK_SERIALIZER = serializer(BLOCK_DIMENSION, BLOCK_PALETTE_MIN_BITS, BLOCK_PALETTE_MAX_BITS, BLOCK_PALETTE_DIRECT_BITS);

    /// Creates a serializer for biome palettes whose direct storage can represent every entry in a registry.
    ///
    /// @param biomeCount the number of entries in the biome registry
    /// @return a serializer for biome palettes
    static NetworkBuffer.Type<Palette> biomeSerializer(int biomeCount) {
        return serializer(BIOME_DIMENSION, BIOME_PALETTE_MIN_BITS, BIOME_PALETTE_MAX_BITS, Palettes.directBits(biomeCount));
    }

    /// Creates a serializer for the vanilla paletted container format.
    ///
    /// A palette is written as a storage width byte, followed by the single value for a width of `0`, or by the
    /// value table for an indirect width, then the packed longs. Reading throws [IllegalArgumentException] when
    /// the incoming storage width or value table is invalid.
    ///
    /// A written palette must have been created with the same settings as the serializer, because the receiver derives
    /// the direct storage width from its own registry rather than from the width byte. Use
    /// [#copyFrom(Palette)] into a palette of the target settings to convert one that was built differently.
    ///
    /// @param dimension   the side length of the palettes to read and write
    /// @param minIndirect the smallest indirect storage width
    /// @param maxIndirect the largest indirect storage width
    /// @param directBits  the only accepted direct storage width
    /// @return a serializer for palettes of the given configuration
    static NetworkBuffer.Type<Palette> serializer(int dimension, int minIndirect, int maxIndirect, int directBits) {
        return new NetworkBuffer.Type<>() {
            @Override
            public void write(NetworkBuffer buffer, Palette palette) {
                final PaletteImpl value = (PaletteImpl) palette;
                assert value.dimension == dimension && value.minBitsPerEntry == minIndirect
                        && value.maxBitsPerEntry == maxIndirect && value.directBits == directBits
                        : "Palette must be created with the settings of its serializer";
                final byte bitsPerEntry = value.bitsPerEntry;
                buffer.write(BYTE, bitsPerEntry);
                if (bitsPerEntry == 0) {
                    buffer.write(VAR_INT, value.singleValue);
                } else {
                    final PaletteTable table = value.table;
                    if (table != null) {
                        final int size = table.size();
                        buffer.write(VAR_INT, size);
                        for (int index = 0; index < size; index++) {
                            buffer.write(VAR_INT, table.value(index));
                        }
                    }
                    assert value.values != null : "missing entries";
                    for (long l : value.values) buffer.write(LONG, l);
                }
            }

            @Override
            public Palette read(NetworkBuffer buffer) {
                final byte bitsPerEntry = buffer.read(BYTE);
                if (bitsPerEntry != 0 && (bitsPerEntry < minIndirect
                        || (bitsPerEntry > maxIndirect && bitsPerEntry != directBits)))
                    throw new IllegalArgumentException("Invalid bitsPerEntry: " + bitsPerEntry);
                final PaletteImpl result = new PaletteImpl((byte) dimension, (byte) minIndirect,
                        (byte) maxIndirect, (byte) directBits);
                result.bitsPerEntry = bitsPerEntry;
                if (bitsPerEntry == 0) {
                    // Single value palette
                    result.singleValue = buffer.read(VAR_INT);
                    return result;
                }
                int[] palette = null;
                if (bitsPerEntry <= maxIndirect) {
                    // Indirect palette
                    palette = buffer.read(VAR_INT_ARRAY);
                    if (palette.length == 0 || palette.length > Palettes.maxPaletteSize(bitsPerEntry))
                        throw new IllegalArgumentException("Invalid palette length: " + palette.length);
                }
                final long[] data = new long[Palettes.arrayLength(dimension, bitsPerEntry)];
                for (int i = 0; i < data.length; i++) data[i] = buffer.read(LONG);
                if (palette == null) {
                    result.values = data;
                } else {
                    Palettes.validateIndices(bitsPerEntry, dimension, data, palette.length);
                    result.loadIndirect(bitsPerEntry, palette, data);
                }
                return result;
            }
        };
    }
}
