package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

import static net.minestom.server.instance.palette.Palettes.*;

final class PaletteImpl implements Palette {
    private static final ThreadLocal<int[]> WRITE_CACHE = ThreadLocal.withInitial(() -> new int[4096]);
    private static final int DIRECT_BITS = 15;
    final byte dimension, maxBitsPerEntry;

    byte bitsPerEntry = 0;
    int count = 0; // Serve as the single value if bitsPerEntry == 0

    long[] values;
    // palette index = value
    IntArrayList paletteToValueList;
    // value = palette index
    private Int2IntOpenHashMap valueToPaletteMap;

    PaletteImpl(byte dimension, byte maxBitsPerEntry) {
        validateDimension(dimension);
        this.dimension = dimension;
        this.maxBitsPerEntry = maxBitsPerEntry;
    }

    PaletteImpl(byte dimension, byte maxBitsPerEntry, byte bitsPerEntry,
                int count, int[] palette, long[] values) {
        this(dimension, maxBitsPerEntry);
        this.bitsPerEntry = bitsPerEntry;

        this.count = count;
        this.values = values;

        if (hasPalette()) {
            this.paletteToValueList = new IntArrayList(palette.clone());
            this.valueToPaletteMap = new Int2IntOpenHashMap(palette.length);
            this.valueToPaletteMap.defaultReturnValue(-1);
            for (int i = 0; i < palette.length; i++) {
                this.valueToPaletteMap.put(palette[i], i);
            }
        }
    }

    PaletteImpl(byte dimension, byte maxBitsPerEntry, byte bitsPerEntry) {
        this(dimension, maxBitsPerEntry, bitsPerEntry,
                0, new int[]{0}, new long[arrayLength(dimension, bitsPerEntry)]
        );
    }

    @Override
    public int get(int x, int y, int z) {
        validateCoord(x, y, z);
        if (bitsPerEntry == 0) return count;
        final int value = read(dimension(), bitsPerEntry, values, x, y, z);
        // Change to palette value and return
        return hasPalette() ? paletteToValueList.elements()[value] : value;
    }

    @Override
    public void getAll(@NotNull EntryConsumer consumer) {
        if (bitsPerEntry == 0) {
            Palettes.getAllFill(dimension, count, consumer);
        } else {
            retrieveAll(consumer, true);
        }
    }

    @Override
    public void getAllPresent(@NotNull EntryConsumer consumer) {
        if (bitsPerEntry == 0) {
            if (count != 0) Palettes.getAllFill(dimension, count, consumer);
        } else {
            retrieveAll(consumer, false);
        }
    }

    @Override
    public void set(int x, int y, int z, int value) {
        validateCoord(x, y, z);
        value = paletteIndexToValue(value);
        final int oldValue = Palettes.write(dimension(), bitsPerEntry, values, x, y, z, value);
        // Check if block count needs to be updated
        final boolean currentAir = oldValue == 0;
        if (currentAir != (value == 0)) this.count += currentAir ? 1 : -1;
    }

    @Override
    public void fill(int value) {
        this.bitsPerEntry = 0;
        this.count = value;
        this.values = null;
        this.paletteToValueList = null;
        this.valueToPaletteMap = null;
    }

    @Override
    public void setAll(@NotNull EntrySupplier supplier) {
        int[] cache = WRITE_CACHE.get();
        final int dimension = dimension();
        // Fill cache with values
        int fillValue = -1;
        int count = 0;
        int index = 0;
        for (int y = 0; y < dimension; y++) {
            for (int z = 0; z < dimension; z++) {
                for (int x = 0; x < dimension; x++) {
                    int value = supplier.get(x, y, z);
                    // Support for fill fast exit if the supplier returns a constant value
                    if (fillValue != -2) {
                        if (fillValue == -1) {
                            fillValue = value;
                        } else if (fillValue != value) {
                            fillValue = -2;
                        }
                    }
                    // Set value in cache
                    if (value != 0) {
                        value = paletteIndexToValue(value);
                        count++;
                    }
                    cache[index++] = value;
                }
            }
        }
        assert index == maxSize();
        // Update palette content
        if (fillValue < 0) {
            updateAll(cache);
            this.count = count;
        } else {
            fill(fillValue);
        }
    }

    @Override
    public void replace(int x, int y, int z, @NotNull IntUnaryOperator operator) {
        validateCoord(x, y, z);
        final int oldValue = get(x, y, z);
        final int newValue = operator.applyAsInt(oldValue);
        if (oldValue != newValue) set(x, y, z, newValue);
    }

    @Override
    public void replaceAll(@NotNull EntryFunction function) {
        int[] cache = WRITE_CACHE.get();
        AtomicInteger arrayIndex = new AtomicInteger();
        AtomicInteger count = new AtomicInteger();
        getAll((x, y, z, value) -> {
            final int newValue = function.apply(x, y, z, value);
            final int index = arrayIndex.getPlain();
            arrayIndex.setPlain(index + 1);
            cache[index] = newValue != value ? paletteIndexToValue(newValue) : value;
            if (newValue != 0) count.setPlain(count.getPlain() + 1);
        });
        assert arrayIndex.getPlain() == maxSize();
        // Update palette content
        updateAll(cache);
        this.count = count.getPlain();
    }

    @Override
    public int count() {
        if (bitsPerEntry == 0) {
            return count == 0 ? 0 : maxSize();
        } else {
            return count;
        }
    }

    @Override
    public int bitsPerEntry() {
        return bitsPerEntry;
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
    public void optimize(Optimization focus) {
        final int bitsPerEntry = this.bitsPerEntry;
        if (bitsPerEntry == 0) {
            // Already optimized (single value)
            return;
        }

        // Count unique values
        IntSet uniqueValues = new IntOpenHashSet();
        getAll((x, y, z, value) -> {uniqueValues.add(value);});
        final int uniqueCount = uniqueValues.size();

        // If only one unique value, use fill for maximum optimization
        if (uniqueCount == 1) {
            fill(uniqueValues.iterator().nextInt());
            return;
        }

        if (focus == Optimization.SPEED) {
            // Speed optimization - use direct storage (15 bits)
            resize((byte) DIRECT_BITS);
        } else if (focus == Optimization.SIZE) {
            // Size optimization - calculate minimum bits needed for unique values
            final byte optimalBits = (byte) MathUtils.bitsToRepresent(uniqueCount - 1);
            if (optimalBits < bitsPerEntry) {
                resize(optimalBits);
            }
        }
    }

    @Override
    public @NotNull Palette clone() {
        PaletteImpl clone = new PaletteImpl(dimension, maxBitsPerEntry);
        clone.bitsPerEntry = this.bitsPerEntry;
        clone.count = this.count;
        if (bitsPerEntry == 0) return clone;
        clone.values = values.clone();
        if (hasPalette()) {
            clone.paletteToValueList = new IntArrayList(paletteToValueList);
            clone.valueToPaletteMap = new Int2IntOpenHashMap(valueToPaletteMap);
        }
        return clone;
    }

    private void retrieveAll(@NotNull EntryConsumer consumer, boolean consumeEmpty) {
        if (!consumeEmpty && count == 0) return;
        final long[] values = this.values;
        final int dimension = this.dimension();
        final int bitsPerEntry = this.bitsPerEntry;
        final int magicMask = (1 << bitsPerEntry) - 1;
        final int valuesPerLong = 64 / bitsPerEntry;
        final int size = maxSize();
        final int dimensionMinus = dimension - 1;
        final int[] ids = hasPalette() ? paletteToValueList.elements() : null;
        final int dimensionBitCount = MathUtils.bitsToRepresent(dimensionMinus);
        final int shiftedDimensionBitCount = dimensionBitCount << 1;
        for (int i = 0; i < values.length; i++) {
            final long value = values[i];
            final int startIndex = i * valuesPerLong;
            final int endIndex = Math.min(startIndex + valuesPerLong, size);
            for (int index = startIndex; index < endIndex; index++) {
                final int bitIndex = (index - startIndex) * bitsPerEntry;
                final int paletteIndex = (int) (value >> bitIndex & magicMask);
                if (consumeEmpty || paletteIndex != 0) {
                    final int y = index >> shiftedDimensionBitCount;
                    final int z = index >> dimensionBitCount & dimensionMinus;
                    final int x = index & dimensionMinus;
                    final int result = ids != null && paletteIndex < ids.length ? ids[paletteIndex] : paletteIndex;
                    consumer.accept(x, y, z, result);
                }
            }
        }
    }

    private void updateAll(int[] paletteValues) {
        final int size = maxSize();
        assert paletteValues.length >= size;
        final int bitsPerEntry = this.bitsPerEntry;
        final int valuesPerLong = 64 / bitsPerEntry;
        final long clear = (1L << bitsPerEntry) - 1L;
        final long[] values = this.values;
        for (int i = 0; i < values.length; i++) {
            long block = values[i];
            final int startIndex = i * valuesPerLong;
            final int endIndex = Math.min(startIndex + valuesPerLong, size);
            for (int index = startIndex; index < endIndex; index++) {
                final int bitIndex = (index - startIndex) * bitsPerEntry;
                block = block & ~(clear << bitIndex) | ((long) paletteValues[index] << bitIndex);
            }
            values[i] = block;
        }
    }

    void resize(byte newBitsPerEntry) {
        if (newBitsPerEntry > maxBitsPerEntry()) newBitsPerEntry = DIRECT_BITS;
        PaletteImpl palette = new PaletteImpl(dimension, maxBitsPerEntry, newBitsPerEntry);
        if (paletteToValueList != null) palette.paletteToValueList = paletteToValueList;
        if (valueToPaletteMap != null) palette.valueToPaletteMap = valueToPaletteMap;
        getAll(palette::set);
        this.bitsPerEntry = palette.bitsPerEntry;
        this.values = palette.values;
        this.paletteToValueList = palette.paletteToValueList;
        this.valueToPaletteMap = palette.valueToPaletteMap;
        assert values != null;
    }

    private int paletteIndexToValue(int value) {
        if (!hasPalette()) return value;
        if (values == null) resize((byte) 1);
        final int lastPaletteIndex = this.paletteToValueList.size();
        final byte bpe = this.bitsPerEntry;
        if (lastPaletteIndex >= maxPaletteSize(bpe)) {
            // Palette is full, must resize
            resize((byte) (bpe + 1));
            return paletteIndexToValue(value);
        }
        final int lookup = valueToPaletteMap.putIfAbsent(value, lastPaletteIndex);
        if (lookup != -1) return lookup;
        this.paletteToValueList.add(value);
        assert lastPaletteIndex < maxPaletteSize(bpe);
        return lastPaletteIndex;
    }

    boolean hasPalette() {
        return bitsPerEntry <= maxBitsPerEntry();
    }

    private static void validateCoord(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0)
            throw new IllegalArgumentException("Coordinates must be non-negative");
    }

    private static void validateDimension(int dimension) {
        if (dimension <= 1 || (dimension & dimension - 1) != 0)
            throw new IllegalArgumentException("Dimension must be a positive power of 2");
    }
}
