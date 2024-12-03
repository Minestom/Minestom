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
public interface Palette {
    static Palette blocks() {
        return newPalette(16, 8, 4);
    }

    static Palette biomes() {
        return newPalette(4, 3, 1);
    }

    static Palette newPalette(int dimension, int maxBitsPerEntry, int bitsPerEntry) {
        return new AdaptivePalette((byte) dimension, (byte) maxBitsPerEntry, (byte) bitsPerEntry);
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

    NetworkBuffer.Type<Palette> BLOCK_SERIALIZER = serializer(16, 4, 8);
    NetworkBuffer.Type<Palette> BIOME_SERIALIZER = serializer(4, 1, 3);

    static NetworkBuffer.Type<Palette> serializer(int dimension, int minIndirect, int maxIndirect) {
        return new NetworkBuffer.Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, Palette value) {
                switch (value) {
                    case AdaptivePalette adaptive -> {
                        final SpecializedPalette optimized = adaptive.optimizedPalette();
                        adaptive.palette = optimized;
                        BLOCK_SERIALIZER.write(buffer, optimized);
                    }
                    case PaletteSingle single -> {
                        buffer.write(BYTE, (byte) 0);
                        buffer.write(VAR_INT, single.value());
                        buffer.write(VAR_INT, 0);
                    }
                    case PaletteIndirect indirect -> {
                        buffer.write(BYTE, (byte) value.bitsPerEntry());
                        if (indirect.bitsPerEntry() <= indirect.maxBitsPerEntry()) { // Palette index
                            buffer.write(VAR_INT.list(), indirect.paletteToValueList);
                        }
                        buffer.write(LONG_ARRAY, indirect.values);
                    }
                    default -> throw new UnsupportedOperationException("Unsupported palette type: " + value.getClass());
                }
            }

            @Override
            public Palette read(@NotNull NetworkBuffer buffer) {
                final byte bitsPerEntry = buffer.read(BYTE);
                if (bitsPerEntry == 0) {
                    // Single valued 0-0
                    final int value = buffer.read(VAR_INT);
                    return new PaletteSingle((byte) dimension, value);
                } else if (bitsPerEntry >= minIndirect && bitsPerEntry <= maxIndirect) {
                    // Indirect palette
                    final int[] palette = buffer.read(VAR_INT_ARRAY);
                    final long[] data = buffer.read(LONG_ARRAY);
                    return new PaletteIndirect(dimension, maxIndirect, bitsPerEntry,
                            Palettes.count(bitsPerEntry, data),
                            palette, data);
                } else {
                    // Direct palette
                    final long[] data = buffer.read(LONG_ARRAY);
                    return new PaletteIndirect(dimension, maxIndirect, bitsPerEntry,
                            Palettes.count(bitsPerEntry, data),
                            new int[0], data);
                }
            }
        };
    }
}
