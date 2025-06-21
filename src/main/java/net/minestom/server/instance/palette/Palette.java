package net.minestom.server.instance.palette;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntUnaryOperator;

import static net.minestom.server.network.NetworkBuffer.*;

/**
 * Represents a palette used to store blocks and biomes.
 * <p>
 * 0 is the default value.
 */
public sealed interface Palette permits PaletteImpl {
    static Palette blocks(int bitsPerEntry) {
        return newPalette(PaletteImpl.BLOCK_DIMENSION, PaletteImpl.BLOCK_PALETTE_MIN_BITS, PaletteImpl.BLOCK_PALETTE_MAX_BITS, bitsPerEntry);
    }

    static Palette biomes(int bitsPerEntry) {
        return newPalette(PaletteImpl.BIOME_DIMENSION, PaletteImpl.BIOME_PALETTE_MIN_BITS, PaletteImpl.BIOME_PALETTE_MAX_BITS, bitsPerEntry);
    }

    static Palette blocks() {
        return newPalette(PaletteImpl.BLOCK_DIMENSION, PaletteImpl.BLOCK_PALETTE_MIN_BITS, PaletteImpl.BLOCK_PALETTE_MAX_BITS);
    }

    static Palette biomes() {
        return newPalette(PaletteImpl.BIOME_DIMENSION, PaletteImpl.BIOME_PALETTE_MIN_BITS, PaletteImpl.BIOME_PALETTE_MAX_BITS);
    }

    static Palette newPalette(int dimension, int minBitsPerEntry, int maxBitsPerEntry) {
        return new PaletteImpl((byte) dimension, (byte) minBitsPerEntry, (byte) maxBitsPerEntry);
    }

    static Palette newPalette(int dimension, int minBitsPerEntry, int maxBitsPerEntry,
                              int bitsPerEntry) {
        return new PaletteImpl((byte) dimension, (byte) minBitsPerEntry, (byte) maxBitsPerEntry, (byte) bitsPerEntry);
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

    int maxBitsPerEntry();

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

    @NotNull Palette clone();

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

    NetworkBuffer.Type<Palette> BLOCK_SERIALIZER = serializer(PaletteImpl.BLOCK_DIMENSION, PaletteImpl.BLOCK_PALETTE_MIN_BITS, PaletteImpl.BLOCK_PALETTE_MAX_BITS);
    NetworkBuffer.Type<Palette> BIOME_SERIALIZER = serializer(PaletteImpl.BIOME_DIMENSION, PaletteImpl.BIOME_PALETTE_MIN_BITS, PaletteImpl.BIOME_PALETTE_MAX_BITS);

    static NetworkBuffer.Type<Palette> serializer(int dimension, int minIndirect, int maxIndirect) {
        //noinspection unchecked
        return (NetworkBuffer.Type) new NetworkBuffer.Type<PaletteImpl>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, PaletteImpl value) {
                final byte bitsPerEntry = value.bitsPerEntry;
                buffer.write(BYTE, bitsPerEntry);
                if (bitsPerEntry == 0) {
                    buffer.write(VAR_INT, value.count);
                } else {
                    if (value.hasPalette()) {
                        buffer.write(VAR_INT.list(), value.paletteToValueList);
                    }
                    for (long l : value.values) {
                        buffer.write(LONG, l);
                    }
                }
            }

            @Override
            public PaletteImpl read(@NotNull NetworkBuffer buffer) {
                final byte bitsPerEntry = buffer.read(BYTE);
                if (bitsPerEntry == 0) {
                    // Single value palette
                    final int value = buffer.read(VAR_INT);
                    PaletteImpl palette = new PaletteImpl((byte) dimension, (byte) minIndirect, (byte) maxIndirect);
                    palette.count = value;
                    return palette;
                } else if (bitsPerEntry >= minIndirect && bitsPerEntry <= maxIndirect) {
                    // Indirect palette
                    final int[] palette = buffer.read(VAR_INT_ARRAY);
                    int entriesPerLong = 64 / bitsPerEntry;
                    final long[] data = new long[(dimension * dimension * dimension) / entriesPerLong + 1];
                    for (int i = 0; i < data.length; i++) {
                        data[i] = buffer.read(LONG);
                    }
                    return new PaletteImpl((byte) dimension, (byte) minIndirect, (byte) maxIndirect, bitsPerEntry,
                            Palettes.count(bitsPerEntry, data),
                            palette, data);
                } else {
                    // Direct palette
                    final long[] data = buffer.read(LONG_ARRAY);
                    return new PaletteImpl((byte) dimension, (byte) minIndirect, (byte) maxIndirect, bitsPerEntry,
                            Palettes.count(bitsPerEntry, data),
                            new int[0], data);
                }
            }
        };
    }
}
