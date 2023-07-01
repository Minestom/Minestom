package net.minestom.server.instance.palette;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

/**
 * Palette containing a single value. Useful for both empty and full palettes.
 */
record FilledPalette(byte dim, int value) implements SpecializedPalette.Immutable {
    @Override
    public int get(int x, int y, int z) {
        return value;
    }

    @Override
    public void getAll(@NotNull EntryConsumer consumer) {
        final byte dimension = this.dim;
        final int value = this.value;
        for (byte y = 0; y < dimension; y++)
            for (byte z = 0; z < dimension; z++)
                for (byte x = 0; x < dimension; x++)
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
    public int dimension() {
        return dim;
    }

    @Override
    public @NotNull SpecializedPalette clone() {
        return this;
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BYTE, (byte) 0);
        writer.write(VAR_INT, value);
        writer.write(VAR_INT, 0);
    }
}
