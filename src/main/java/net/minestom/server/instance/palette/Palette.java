package net.minestom.server.instance.palette;

import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntUnaryOperator;

/**
 * Represents a palette used to store blocks and biomes.
 * <p>
 * 0 is the default value.
 */
public interface Palette extends Writeable {
    static Palette blocks() {
        return newPalette(16, 8, 6, 1);
    }

    static Palette biomes() {
        return newPalette(4, 2, 1, 1);
    }

    static Palette newPalette(int dimension, int maxBitsPerEntry, int bitsPerEntry, int bitIncrement) {
        return new AdaptivePalette(dimension, maxBitsPerEntry, bitsPerEntry, bitIncrement);
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
}
