package net.minestom.server.instance.palette;

import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a palette used to store blocks and biomes.
 * <p>
 * 0 is the default value.
 */
public sealed interface Palette extends Writeable permits PaletteImpl {
    static Palette blocks() {
        return newPalette(16, 8, 6, 1);
    }

    static Palette biomes() {
        return newPalette(4, 2, 2, 1);
    }

    static Palette newPalette(int dimension, int maxBitsPerEntry, int bitsPerEntry, int bitIncrement) {
        return new PaletteImpl(dimension, maxBitsPerEntry, bitsPerEntry, bitIncrement);
    }

    int get(int x, int y, int z);

    void getAll(@NotNull EntryConsumer consumer);

    void set(int x, int y, int z, int value);

    void fill(int value);

    void setAll(@NotNull EntrySupplier supplier);

    /**
     * Returns the number of entries in this palette.
     */
    int size();

    /**
     * Returns the number of bits used per entry.
     */
    int bitsPerEntry();

    /**
     * Returns the payload of this palette.
     * <p>
     * The size of each element is defined by {@link #bitsPerEntry()}.
     *
     * @return the palette payload
     */
    long[] data();

    int maxBitsPerEntry();

    /**
     * Returns the maximum number of entries in this palette.
     */
    int maxSize();

    int dimension();

    @NotNull Palette clone();

    @FunctionalInterface
    interface EntrySupplier {
        int get(int x, int y, int z);
    }

    @FunctionalInterface
    interface EntryConsumer {
        void accept(int x, int y, int z, int value);
    }
}
