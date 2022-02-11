package net.minestom.server.instance.palette;

import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntUnaryOperator;

/**
 * Palette that switches between its backend based on the use case.
 */
final class AdaptivePalette implements Palette {
    final int dimension;
    final int dimensionBitCount;
    final int maxBitsPerEntry;
    final int defaultBitsPerEntry;
    final int bitsIncrement;

    private SpecializedPalette palette;

    AdaptivePalette(int dimension, int maxBitsPerEntry, int bitsPerEntry, int bitsIncrement) {
        this.dimensionBitCount = validateDimension(dimension);

        this.dimension = dimension;
        this.maxBitsPerEntry = maxBitsPerEntry;
        this.defaultBitsPerEntry = bitsPerEntry;
        this.bitsIncrement = bitsIncrement;

        this.palette = new FilledPalette(dimension, 0);
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
        this.palette = new FilledPalette(dimension, value);
    }

    @Override
    public void setAll(@NotNull EntrySupplier supplier) {
        SpecializedPalette newPalette = new FlexiblePalette(this);
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

    @Override
    public void write(@NotNull BinaryWriter writer) {
        optimizedPalette().write(writer);
    }

    Palette optimizedPalette() {
        var currentPalette = this.palette;
        if (currentPalette instanceof FlexiblePalette flexiblePalette) {
            final int count = flexiblePalette.count();
            if (count == 0) {
                return (this.palette = new FilledPalette(dimension, 0));
            } else if (count == flexiblePalette.maxSize()) {
                var palette = flexiblePalette.paletteToValueList;
                if (palette.size() == 2 && palette.getInt(0) == 0) {
                    // first element is air, second should be the value the palette is filled with
                    return (this.palette = new FilledPalette(dimension, palette.getInt(1)));
                }
            }
        }
        return currentPalette;
    }

    Palette flexiblePalette() {
        SpecializedPalette currentPalette = this.palette;
        if (currentPalette instanceof FilledPalette filledPalette) {
            currentPalette = new FlexiblePalette(this);
            currentPalette.fill(filledPalette.value());
            this.palette = currentPalette;
        }
        return currentPalette;
    }

    private static int validateDimension(int dimension) {
        if (dimension <= 1) {
            throw new IllegalArgumentException("Dimension must be greater 1");
        }
        double log2 = Math.log(dimension) / Math.log(2);
        if ((int) Math.ceil(log2) != (int) Math.floor(log2)) {
            throw new IllegalArgumentException("Dimension must be a power of 2");
        }
        return (int) log2;
    }
}
