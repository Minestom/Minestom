package net.minestom.server.instance.palette;

import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a palette used to store blocks & biomes.
 * <p>
 * 0 is the default value.
 */
public interface Palette extends Writeable {
    static Palette blocks() {
        return new PaletteImpl(16 * 16 * 16, 8, 8, 2);
    }

    static Palette biomes() {
        return new PaletteImpl(4 * 4 * 4, 2, 2, 1);
    }

    int get(int x, int y, int z);

    void set(int x, int y, int z, int value);

    int count();

    /**
     * Returns the number of bits used per entry.
     */
    int bitsPerEntry();

    int maxBitsPerEntry();

    /**
     * Returns the number of entries in this palette.
     */
    int size();

    long[] data();

    @NotNull Palette clone();
}
