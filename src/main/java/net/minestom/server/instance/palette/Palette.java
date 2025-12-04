package net.minestom.server.instance.palette;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

/**
 * Palette is a data storage with three storage models used to store blocks and biomes
 * <br>
 * Single Value Mode {@code (bitsPerEntry == 0)}: All blocks have the same value.
 * No arrays allocated, value stored in count field.
 * <br>
 * Indirect Mode {@code (bitsPerEntry <= maxBitsPerEntry)}: Uses palette compression.
 * Values array stores palette indices, paletteIndexMap
 * provides bidirectional mapping between indices and block values.
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
    @ApiStatus.Internal
    int BLOCK_PALETTE_DIRECT_BITS = Palettes.paletteBits(Block.statesCount());

    int BIOME_DIMENSION = 4;
    int BIOME_PALETTE_MIN_BITS = 1;
    int BIOME_PALETTE_MAX_BITS = 3;

    @ApiStatus.Internal
    static int biomePaletteDirectBits() {
        final var process = MinecraftServer.process();
        // 7 == Vanilla biome direct bits as of 1.21.10
        final int biomeDirectBits = process != null ? Palettes.paletteBits(process.biome().size()) : 7;
        if (BiomePaletteHolder.BIOME_DIRECT_BITS == 0) {
            BiomePaletteHolder.BIOME_DIRECT_BITS = biomeDirectBits;
        } else if (biomeDirectBits != BiomePaletteHolder.BIOME_DIRECT_BITS) {
            throw new IllegalStateException("Biome direct bits must not change after chunk creation");
        }
        return biomeDirectBits;
    }

    static Palette blocks(int bitsPerEntry) {
        return sized(BLOCK_DIMENSION, BLOCK_PALETTE_MIN_BITS, BLOCK_PALETTE_MAX_BITS, BLOCK_PALETTE_DIRECT_BITS, bitsPerEntry);
    }

    static Palette biomes(int bitsPerEntry) {
        return sized(BIOME_DIMENSION, BIOME_PALETTE_MIN_BITS, BIOME_PALETTE_MAX_BITS, biomePaletteDirectBits(), bitsPerEntry);
    }

    static Palette blocks() {
        return empty(BLOCK_DIMENSION, BLOCK_PALETTE_MIN_BITS, BLOCK_PALETTE_MAX_BITS, BLOCK_PALETTE_DIRECT_BITS);
    }

    static Palette biomes() {
        return empty(BIOME_DIMENSION, BIOME_PALETTE_MIN_BITS, BIOME_PALETTE_MAX_BITS, biomePaletteDirectBits());
    }

    @ApiStatus.Internal
    static Palette empty(int dimension, int minBitsPerEntry, int maxBitsPerEntry, int directBits) {
        return new PaletteImpl((byte) dimension, (byte) minBitsPerEntry, (byte) maxBitsPerEntry, (byte) directBits);
    }

    @ApiStatus.Internal
    static Palette sized(int dimension, int minBitsPerEntry, int maxBitsPerEntry, int directBits, int bitsPerEntry) {
        return new PaletteImpl((byte) dimension, (byte) minBitsPerEntry, (byte) maxBitsPerEntry, (byte) directBits, (byte) bitsPerEntry);
    }

    int get(int x, int y, int z);

    void getAll(EntryConsumer consumer);

    void getAllPresent(EntryConsumer consumer);

    void set(int x, int y, int z, int value);

    void setAll(EntrySupplier supplier);

    void replace(int oldValue, int newValue);

    void replace(int x, int y, int z, IntUnaryOperator operator);

    void replaceAll(EntryFunction function);

    void fill(int value);

    /**
     * Efficiently fills a cuboid from {@code (minX, minY, minZ)} to {@code (maxX, maxY, maxZ)} (inclusive).
     *
     * @param value The value to fill with
     * @throws IllegalArgumentException if {@code (minX, minY, minZ)} or {@code (maxX, maxY, maxZ)} are out of bounds,
     * or if coordinates are out of order.
     */
    void fill(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int value);

    /**
     * Efficiently fills a cuboid from {@code min} to {@code max} (inclusive).
     *
     * @param min The minimum coordinates for the cuboid
     * @param max The maximum coordinates for the cuboid (inclusive)
     * @param value The value to fill with
     * @throws IllegalArgumentException if {@code min} or {@code max} are out of bounds,
     * or if coordinates are out of order.
     */
    default void fill(Point min, Point max, int value) {
        fill(min.blockX(), min.blockY(), min.blockZ(), max.blockX(), max.blockY(), max.blockZ(), value);
    }

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
     * Efficiently loads values from the anvil file format.
     * <p>
     * All indices are the same length.
     * This length is set to the minimum amount of bits required to represent the largest index in the palette,
     * and then set to a minimum of minBitsPerEntry.
     * Indices are not packed across multiple longs,
     * meaning that if there is no more space in a given long for the whole next index,
     * it starts instead at the least significant bit of the next long.
     * (for more info, see: <a href="https://minecraft.wiki/w/Chunk_format">the Minecraft wiki page</a>)
     *
     * @param palette The palette to use
     * @param values The paletted values
     */
    void load(int[] palette, long[] values);

    /**
     * @return the number of entries that are not equal to 0 in this palette.
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
     * Gets the highest y position where predicate returns true, or -1 if none matched.
     *
     * @param x the x coordinate to check at
     * @param z the z coordinate to check at
     * @return the highest y position where predicate returns true, or -1 if none matched.
     */
    int height(int x, int z, IntPredicate predicate);

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

    @ApiStatus.Internal
    final class BiomePaletteHolder {
        private static int BIOME_DIRECT_BITS = 0;
        @Nullable
        private static NetworkBuffer.Type<Palette> BIOME_SERIALIZER = null;
    }

    NetworkBuffer.Type<Palette> BLOCK_SERIALIZER = serializer(BLOCK_DIMENSION, BLOCK_PALETTE_MIN_BITS, BLOCK_PALETTE_MAX_BITS, BLOCK_PALETTE_DIRECT_BITS);

    static NetworkBuffer.Type<Palette> biomeSerializer() {
        if (BiomePaletteHolder.BIOME_SERIALIZER != null) return BiomePaletteHolder.BIOME_SERIALIZER;
        final var biomeSerializer = serializer(BIOME_DIMENSION, BIOME_PALETTE_MIN_BITS, BIOME_PALETTE_MAX_BITS, biomePaletteDirectBits());
        BiomePaletteHolder.BIOME_SERIALIZER = biomeSerializer;
        return biomeSerializer;
    }

    @ApiStatus.Internal
    static NetworkBuffer.Type<Palette> serializer(int dimension, int minIndirect, int maxIndirect, int directBits) {
        return new NetworkBuffer.Type<>() {
            @Override
            public void write(NetworkBuffer buffer, Palette palette) {
                final PaletteImpl value = (PaletteImpl) palette;
                if (!value.isPaletteType(dimension, minIndirect, maxIndirect, directBits)) {
                    throw new IllegalArgumentException("Palettes must be serialized with the proper serializer");
                }
                final byte bitsPerEntry = value.bitsPerEntry;
                buffer.write(BYTE, bitsPerEntry);
                if (bitsPerEntry == 0) {
                    buffer.write(VAR_INT, value.count);
                } else if (value.isDirect()) {
                    if (value.bitsPerEntry != directBits) {
                        value.writeValuesResized(buffer, directBits);
                    } else {
                        for (final long l : value.values) buffer.write(LONG, l);
                    }
                } else {
                    final int paletteSize = value.paletteIndexMap.size();
                    buffer.write(VAR_INT, paletteSize);
                    for (int index = 0; index < paletteSize; index++) {
                        buffer.write(VAR_INT, value.paletteIndexMap.indexToValue(index));
                    }
                    for (final long l : value.values) buffer.write(LONG, l);
                }
            }

            @Override
            public Palette read(NetworkBuffer buffer) {
                final byte bitsPerEntry = buffer.read(BYTE);
                final PaletteImpl result = new PaletteImpl((byte) dimension, (byte) minIndirect, (byte) maxIndirect, (byte) directBits);
                result.bitsPerEntry = bitsPerEntry;
                if (bitsPerEntry == 0) {
                    // Single value palette
                    result.count = buffer.read(VAR_INT);
                    return result;
                }
                if (!result.isDirect()) {
                    // Indirect palette
                    if (bitsPerEntry < minIndirect)
                        throw new IllegalArgumentException("Palette bits per entry out of bounds");
                    final int[] palette = buffer.read(VAR_INT_ARRAY);
                    for (final int value : palette) Palettes.validateValue(value, directBits);
                    result.paletteIndexMap = new PaletteIndexMap(palette);
                } else if (bitsPerEntry != directBits) {
                    throw new IllegalArgumentException("Palette bits per entry out of bounds");
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
