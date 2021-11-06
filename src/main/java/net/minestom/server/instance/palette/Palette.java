package net.minestom.server.instance.palette;

import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a palette used to store blocks & biomes.
 * <p>
 * 0 is the default value.
 */
public sealed interface Palette extends Writeable permits PaletteImpl {
    static Palette blocks() {
        return new PaletteImpl(16 * 16 * 16, 8, 6, 1);
    }

    static Palette biomes() {
        return new PaletteImpl(4 * 4 * 4, 2, 2, 1);
    }

    int get(int x, int y, int z);

    void set(int x, int y, int z, int value);

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

    @NotNull Palette clone();
}
