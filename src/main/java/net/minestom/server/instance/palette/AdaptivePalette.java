package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntUnaryOperator;

/**
 * Palette that switches between its backend based on the use case.
 */
final class AdaptivePalette implements Palette, Cloneable {
    final byte dimension, defaultBitsPerEntry, maxBitsPerEntry;
    SpecializedPalette palette;

    AdaptivePalette(byte dimension, byte maxBitsPerEntry, byte bitsPerEntry) {
        validateDimension(dimension);
        this.dimension = dimension;
        this.maxBitsPerEntry = maxBitsPerEntry;
        this.defaultBitsPerEntry = bitsPerEntry;
        this.palette = new PaletteSingle(dimension, 0);
    }

    @Override
    public int get(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0) {
            throw new IllegalArgumentException("Coordinates must be positive");
        }
        return palette.get(x, y, z);
    }

    @Override
    public void getAll(@NotNull EntryConsumer consumer) {
        this.palette.getAll(consumer);
    }

    @Override
    public void getAllPresent(@NotNull EntryConsumer consumer) {
        this.palette.getAllPresent(consumer);
    }

    @Override
    public void set(int x, int y, int z, int value) {
        if (x < 0 || y < 0 || z < 0) {
            throw new IllegalArgumentException("Coordinates must be positive");
        }
        flexiblePalette().set(x, y, z, value);
    }

    @Override
    public void fill(int value) {
        this.palette = new PaletteSingle(dimension, value);
    }

    @Override
    public void setAll(@NotNull EntrySupplier supplier) {
        SpecializedPalette newPalette = new PaletteIndirect(this);
        newPalette.setAll(supplier);
        this.palette = newPalette;
    }

    @Override
    public void replace(int x, int y, int z, @NotNull IntUnaryOperator operator) {
        if (x < 0 || y < 0 || z < 0) {
            throw new IllegalArgumentException("Coordinates must be positive");
        }
        flexiblePalette().replace(x, y, z, operator);
    }

    @Override
    public void replaceAll(@NotNull EntryFunction function) {
        flexiblePalette().replaceAll(function);
    }

    @Override
    public int count() {
        return palette.count();
    }

    @Override
    public int bitsPerEntry() {
        return palette.bitsPerEntry();
    }

    @Override
    public int maxBitsPerEntry() {
        return maxBitsPerEntry;
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @Override
    public @NotNull Palette clone() {
        try {
            AdaptivePalette adaptivePalette = (AdaptivePalette) super.clone();
            adaptivePalette.palette = palette.clone();
            return adaptivePalette;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    SpecializedPalette optimizedPalette() {
        var currentPalette = this.palette;
        if (currentPalette instanceof PaletteIndirect paletteIndirect) {
            final int count = paletteIndirect.count();
            if (count == 0) {
                return new PaletteSingle(dimension, 0);
            } else {
                // Find all entries and compress the palette
                IntSet entries = new IntOpenHashSet(paletteIndirect.paletteToValueList.size());
                paletteIndirect.getAll((x, y, z, value) -> entries.add(value));
                final int currentBitsPerEntry = paletteIndirect.bitsPerEntry();
                final int bitsPerEntry;
                if (entries.size() == 1) {
                    return new PaletteSingle(dimension, entries.iterator().nextInt());
                } else if (currentBitsPerEntry > defaultBitsPerEntry &&
                        (bitsPerEntry = MathUtils.bitsToRepresent(entries.size() - 1)) < currentBitsPerEntry) {
                    paletteIndirect.resize((byte) bitsPerEntry);
                    return paletteIndirect;
                }
            }
        }
        return currentPalette;
    }

    Palette flexiblePalette() {
        SpecializedPalette currentPalette = this.palette;
        if (currentPalette instanceof PaletteSingle paletteSingle) {
            currentPalette = new PaletteIndirect(this);
            currentPalette.fill(paletteSingle.value());
            this.palette = currentPalette;
        }
        return currentPalette;
    }

    private static void validateDimension(int dimension) {
        if (dimension <= 1 || (dimension & dimension - 1) != 0)
            throw new IllegalArgumentException("Dimension must be a positive power of 2");
    }
}
