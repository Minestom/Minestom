package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntUnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

/**
 * Palette is a data storage with three storage models used to store blocks and biomes
 * <br>
 * Single Value Mode {@code (bitsPerEntry == 0)}: All blocks have the same value.
 * No arrays allocated, value stored in count field.
 * <br>
 * Indirect Mode {@code (bitsPerEntry <= maxBitsPerEntry)}: Uses palette compression.
 * Values array stores palette indices, paletteToValueList and valueToPaletteMap
 * provide bidirectional mapping between indices and block values.
 * <br>
 * Direct Mode {@code (bitsPerEntry > maxBitsPerEntry)}: Stores block values directly.
 * No palette structures, values array contains actual block values using directBits.
 * <br>
 * You can optimize for space/speed using {@link #optimize(Optimization)}
 */
public sealed interface Palette permits PaletteImpl {
    int BLOCK_DIMENSION = 16;
    int BLOCK_PALETTE_MIN_BITS = 4;
    int BLOCK_PALETTE_MAX_BITS = 8;
    int BLOCK_PALETTE_DIRECT_BITS = 15;

    int BIOME_DIMENSION = 4;
    int BIOME_PALETTE_MIN_BITS = 1;
    int BIOME_PALETTE_MAX_BITS = 3;
    @ApiStatus.Internal
    int BIOME_PALETTE_DIRECT_BITS = 6; // Vary based on biome count, this is just a sensible default

    static Palette blocks(int bitsPerEntry) {
        return sized(BLOCK_DIMENSION, BLOCK_PALETTE_MIN_BITS, BLOCK_PALETTE_MAX_BITS, BLOCK_PALETTE_DIRECT_BITS, bitsPerEntry);
    }

    static Palette biomes(int bitsPerEntry) {
        return sized(BIOME_DIMENSION, BIOME_PALETTE_MIN_BITS, BIOME_PALETTE_MAX_BITS, BIOME_PALETTE_DIRECT_BITS, bitsPerEntry);
    }

    static Palette blocks() {
        return empty(BLOCK_DIMENSION, BLOCK_PALETTE_MIN_BITS, BLOCK_PALETTE_MAX_BITS, BLOCK_PALETTE_DIRECT_BITS);
    }

    static Palette biomes() {
        return empty(BIOME_DIMENSION, BIOME_PALETTE_MIN_BITS, BIOME_PALETTE_MAX_BITS, BIOME_PALETTE_DIRECT_BITS);
    }

    static Palette empty(int dimension, int minBitsPerEntry, int maxBitsPerEntry, int directBits) {
        return new PaletteImpl((byte) dimension, (byte) minBitsPerEntry, (byte) maxBitsPerEntry, (byte) directBits);
    }

    static Palette sized(int dimension, int minBitsPerEntry, int maxBitsPerEntry, int directBits, int bitsPerEntry) {
        return new PaletteImpl((byte) dimension, (byte) minBitsPerEntry, (byte) maxBitsPerEntry, (byte) directBits, (byte) bitsPerEntry);
    }

    int get(int x, int y, int z);

    void getAll(EntryConsumer consumer);

    void getAllPresent(EntryConsumer consumer);

    int height(int x, int z, EntryPredicate predicate);

    void set(int x, int y, int z, int value);

    void fill(int value);

    void load(int[] palette, long[] values);

    void offset(int offset);

    void replace(int oldValue, int newValue);

    void setAll(EntrySupplier supplier);

    void replace(int x, int y, int z, IntUnaryOperator operator);

    void replaceAll(EntryFunction function);

    /**
     * Efficiently copies values from another palette with the given offset.
     * <p>
     * Both palettes must have the same dimension.
     *
     * @param source  the source palette to copy from
     * @param offsetX the X offset to apply when copying
     * @param offsetY the Y offset to apply when copying
     * @param offsetZ the Z offset to apply when copying
     */
    void copyFrom(Palette source, int offsetX, int offsetY, int offsetZ);

    /**
     * Efficiently copies values from another palette starting at position (0, 0, 0).
     * <p>
     * Both palettes must have the same dimension.
     * <p>
     * This is a convenience method equivalent to calling {@code copyFrom(source, 0, 0, 0)}.
     *
     * @param source the source palette to copy from
     */
    void copyFrom(Palette source);

    /**
     * Returns the number of entries in this palette.
     */
    int count();

    /**
     * Returns the number of entries in this palette that match the given value.
     *
     * @param value the value to count
     * @return the number of entries matching the value
     */
    int count(int value);

    default boolean isEmpty() {
        return count() == 0;
    }

    /**
     * Checks if the palette contains the given value.
     *
     * @param value the value to check
     * @return true if the palette contains the value, false otherwise
     */
    boolean any(int value);

    /**
     * Returns the number of bits used per entry.
     */
    int bitsPerEntry();

    int dimension();

    /**
     * Returns the maximum number of entries in this palette.
     */
    default int maxSize() {
        final int dimension = dimension();
        return dimension * dimension * dimension;
    }

    /**
     * Attempts to optimize the current {@link Palette}
     * <br>
     * If plausible the only optimization will be performed is converting to a single value regardless of {@link Optimization}
     * @param focus the optimization focus
     */
    void optimize(Optimization focus);

    /**
     * An optimization mode to use with {@link #optimize(Optimization)}
     */
    enum Optimization {
        /**
         * Will attempt to make indirect to save space.
         */
        SIZE,
        /**
         * Will attempt to make direct to reduce lookup.
         */
        SPEED,
    }

    /**
     * Compare palettes content independently of their storage format.
     *
     * @param palette the palette to compare with
     * @return true if the palettes are equivalent, false otherwise
     */
    boolean compare(Palette palette);

    Palette clone();

    @ApiStatus.Internal
    int paletteIndexToValue(int value);

    @ApiStatus.Internal
    int valueToPaletteIndex(int value);

    /**
     * Gets the single value of this palette if it is a single value palette, otherwise returns -1.
     */
    @ApiStatus.Internal
    int singleValue();

    /**
     * Gets the value array if it has one, otherwise returns null (i.e. single value palette).
     */
    @ApiStatus.Internal
    long @Nullable [] indexedValues();

    @FunctionalInterface
    interface EntrySupplier {
        int get(int x, int y, int z);
    }

    @FunctionalInterface
    interface EntryConsumer {
        void accept(int x, int y, int z, int value);
    }

    @FunctionalInterface
    interface EntryFunction {
        int apply(int x, int y, int z, int value);
    }

    @FunctionalInterface
    interface EntryPredicate {
        boolean get(int x, int y, int z, int value);
    }

    NetworkBuffer.Type<Palette> BLOCK_SERIALIZER = serializer(BLOCK_DIMENSION, BLOCK_PALETTE_MIN_BITS, BLOCK_PALETTE_MAX_BITS, BLOCK_PALETTE_DIRECT_BITS);

    static NetworkBuffer.Type<Palette> biomeSerializer(int biomeCount) {
        final int directBits = MathUtils.bitsToRepresent(biomeCount);
        return serializer(BIOME_DIMENSION, BIOME_PALETTE_MIN_BITS, BIOME_PALETTE_MAX_BITS, directBits);
    }

    static NetworkBuffer.Type<Palette> serializer(int dimension, int minIndirect, int maxIndirect, int directBits) {
        return new NetworkBuffer.Type<>() {
            @Override
            public void write(NetworkBuffer buffer, Palette palette) {
                PaletteImpl value = (PaletteImpl) palette;
                // Temporary fix for biome direct bits depending on the number of registered biomes
                if (directBits != value.directBits && !value.hasPalette()) {
                    PaletteImpl tmp = new PaletteImpl((byte) dimension, (byte) minIndirect, (byte) maxIndirect, (byte) directBits);
                    tmp.setAll(value::get);
                    value = tmp;
                }
                final byte bitsPerEntry = value.bitsPerEntry;
                buffer.write(BYTE, bitsPerEntry);
                if (bitsPerEntry == 0) {
                    buffer.write(VAR_INT, value.count);
                } else {
                    if (value.hasPalette()) {
                        buffer.write(VAR_INT.list(), value.paletteToValueList);
                    }
                    for (long l : value.values) buffer.write(LONG, l);
                }
            }

            @Override
            public Palette read(NetworkBuffer buffer) {
                final byte bitsPerEntry = buffer.read(BYTE);
                PaletteImpl result = new PaletteImpl((byte) dimension, (byte) minIndirect, (byte) maxIndirect, (byte) directBits);
                result.bitsPerEntry = bitsPerEntry;
                if (bitsPerEntry == 0) {
                    // Single value palette
                    result.count = buffer.read(VAR_INT);
                    return result;
                }
                if (result.hasPalette()) {
                    // Indirect palette
                    final int[] palette = buffer.read(VAR_INT_ARRAY);
                    result.paletteToValueList = new IntArrayList(palette);
                    result.valueToPaletteMap = new Int2IntOpenHashMap(palette.length);
                    for (int i = 0; i < palette.length; i++) {
                        result.valueToPaletteMap.put(palette[i], i);
                    }
                }
                final long[] data = new long[Palettes.arrayLength(dimension, bitsPerEntry)];
                for (int i = 0; i < data.length; i++) data[i] = buffer.read(LONG);
                result.values = data;
                result.recount();
                return result;
            }
        };
    }
}
