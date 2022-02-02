package net.minestom.server.instance.palette;

import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

/**
 * Palette containing a single value. Useful for both empty and full palettes.
 */
record FilledPalette(int dimension, int value) implements SpecializedPalette.Immutable {
    @Override
    public int get(int x, int y, int z) {
        return value;
    }

    @Override
    public void getAll(@NotNull EntryConsumer consumer) {
        final int dimension = dimension();
        for (int y = 0; y < dimension; y++)
            for (int z = 0; z < dimension; z++)
                for (int x = 0; x < dimension; x++)
                    consumer.accept(x, y, z, value);
    }

    @Override
    public void getAllPresent(@NotNull EntryConsumer consumer) {
        if (value != 0) getAll(consumer);
    }

    @Override
    public int count() {
        return value != 0 ? maxSize() : 0;
    }

    @Override
    public @NotNull SpecializedPalette clone() {
        return this;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte((byte) 1); // bitsPerEntry
        // Palette
        writer.writeVarInt(1);
        writer.writeVarInt(value);
        // Data
        final int length = maxSize() / 64;
        writer.writeVarInt(length);
        // TODO: may be possible to write everything in one call instead of a loop
        for (int i = 0; i < length; i++) writer.writeLong(0);
    }
}
