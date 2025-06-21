package net.minestom.server.instance.palette;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntUnaryOperator;

import static net.minestom.server.instance.palette.PaletteImpl.*;
import static net.minestom.server.network.NetworkBuffer.*;

/**
 * Represents a palette used to store blocks and biomes.
 * <p>
 * 0 is the default value.
 */
public sealed interface Palette permits PaletteImpl {
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

    void getAll(@NotNull EntryConsumer consumer);

    void getAllPresent(@NotNull EntryConsumer consumer);

    void set(int x, int y, int z, int value);

    void fill(int value);

    void setAll(@NotNull EntrySupplier supplier);

    void replace(int x, int y, int z, @NotNull IntUnaryOperator operator);

    void replaceAll(@NotNull EntryFunction function);

    /**
     * Returns the number of entries in this palette.
     */
    int count();

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

    void optimize(Optimization focus);

    enum Optimization {
        SIZE,
        SPEED,
    }

    /**
     * Compare palettes content independently of their storage format.
     *
     * @param palette the palette to compare with
     * @return true if the palettes are equivalent, false otherwise
     */
    boolean compare(@NotNull Palette palette);

    @NotNull Palette clone();

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

    NetworkBuffer.Type<Palette> BLOCK_SERIALIZER = serializer(BLOCK_DIMENSION, BLOCK_PALETTE_MIN_BITS, BLOCK_PALETTE_MAX_BITS, BLOCK_PALETTE_DIRECT_BITS);

    static NetworkBuffer.Type<Palette> biomeSerializer(int biomeCount) {
        final int directBits = MathUtils.bitsToRepresent(biomeCount);
        return serializer(BIOME_DIMENSION, BIOME_PALETTE_MIN_BITS, BIOME_PALETTE_MAX_BITS, directBits);
    }

    static NetworkBuffer.Type<Palette> serializer(int dimension, int minIndirect, int maxIndirect, int directBits) {
        //noinspection unchecked
        return (NetworkBuffer.Type) new NetworkBuffer.Type<PaletteImpl>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, PaletteImpl value) {
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
            public PaletteImpl read(@NotNull NetworkBuffer buffer) {
                final byte bitsPerEntry = buffer.read(BYTE);
                if (bitsPerEntry == 0) {
                    // Single value palette
                    final int value = buffer.read(VAR_INT);
                    PaletteImpl palette = new PaletteImpl((byte) dimension, (byte) minIndirect, (byte) maxIndirect, (byte) directBits);
                    palette.count = value;
                    return palette;
                } else if (bitsPerEntry >= minIndirect && bitsPerEntry <= maxIndirect) {
                    // Indirect palette
                    final int[] palette = buffer.read(VAR_INT_ARRAY);
                    int entriesPerLong = 64 / bitsPerEntry;
                    final long[] data = new long[(dimension * dimension * dimension) / entriesPerLong + 1];
                    for (int i = 0; i < data.length; i++) data[i] = buffer.read(LONG);
                    return new PaletteImpl((byte) dimension, (byte) minIndirect, (byte) maxIndirect, (byte) directBits, bitsPerEntry,
                            Palettes.count(bitsPerEntry, data),
                            palette, data);
                } else {
                    // Direct palette
                    final int length = Palettes.arrayLength(dimension, bitsPerEntry);
                    final long[] data = new long[length];
                    for (int i = 0; i < length; i++) data[i] = buffer.read(LONG);
                    return new PaletteImpl((byte) dimension, (byte) minIndirect, (byte) maxIndirect, (byte) directBits, bitsPerEntry,
                            Palettes.count(bitsPerEntry, data),
                            new int[0], data);
                }
            }
        };
    }
}
