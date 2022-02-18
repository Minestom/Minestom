package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

/**
 * Palette able to take any value anywhere. May consume more memory than required.
 */
final class FlexiblePalette implements SpecializedPalette, Cloneable {
    private static final ThreadLocal<int[]> WRITE_CACHE = ThreadLocal.withInitial(() -> new int[4096]);
    private static final int[] MAGIC_MASKS;
    private static final int[] VALUES_PER_LONG;

    static {
        final int entries = 16;
        MAGIC_MASKS = new int[entries];
        VALUES_PER_LONG = new int[entries];
        for (int i = 1; i < entries; i++) {
            MAGIC_MASKS[i] = Integer.MAX_VALUE >> (31 - i);
            VALUES_PER_LONG[i] = Long.SIZE / i;
        }
    }

    // Specific to this palette type
    private final AdaptivePalette adaptivePalette;
    private int bitsPerEntry;

    private boolean hasPalette;
    private int lastPaletteIndex = 1; // First index is air
    private int count = 0;

    private long[] values;
    // palette index = value
    IntArrayList paletteToValueList;
    // value = palette index
    private Int2IntOpenHashMap valueToPaletteMap;

    FlexiblePalette(AdaptivePalette adaptivePalette, int bitsPerEntry) {
        this.adaptivePalette = adaptivePalette;

        this.bitsPerEntry = bitsPerEntry;
        this.hasPalette = bitsPerEntry <= maxBitsPerEntry();

        this.paletteToValueList = new IntArrayList(1);
        this.paletteToValueList.add(0);
        this.valueToPaletteMap = new Int2IntOpenHashMap(1);
        this.valueToPaletteMap.put(0, 0);
        this.valueToPaletteMap.defaultReturnValue(-1);

        this.values = new long[valuesLength(bitsPerEntry)];
    }

    FlexiblePalette(AdaptivePalette adaptivePalette) {
        this(adaptivePalette, adaptivePalette.defaultBitsPerEntry);
    }

    @Override
    public int get(int x, int y, int z) {
        final long[] values = this.values;
        final int bitsPerEntry = this.bitsPerEntry;
        final int valuesPerLong = VALUES_PER_LONG[bitsPerEntry];
        final int dimension = dimension();

        final int sectionIdentifier = getSectionIndex(x % dimension, y % dimension, z % dimension);
        final int index = sectionIdentifier / valuesPerLong;
        final int bitIndex = sectionIdentifier % valuesPerLong * bitsPerEntry;
        final short value = (short) (values[index] >> bitIndex & MAGIC_MASKS[bitsPerEntry]);
        // Change to palette value and return
        return hasPalette ? paletteToValueList.getInt(value) : value;
    }

    @Override
    public void getAll(@NotNull EntryConsumer consumer) {
        retrieveAll(consumer, true);
    }

    @Override
    public void getAllPresent(@NotNull EntryConsumer consumer) {
        retrieveAll(consumer, false);
    }

    @Override
    public void set(int x, int y, int z, int value) {
        final boolean placedAir = value == 0;
        if (!placedAir) value = getPaletteIndex(value);
        final int bitsPerEntry = this.bitsPerEntry;
        final int valuesPerLong = VALUES_PER_LONG[bitsPerEntry];
        final int dimension = dimension();
        final long[] values = this.values;
        // Change to palette value
        final int sectionIndex = getSectionIndex(x % dimension, y % dimension, z % dimension);
        final int index = sectionIndex / valuesPerLong;
        final int bitIndex = (sectionIndex % valuesPerLong) * bitsPerEntry;

        long block = values[index];
        {
            final long clear = MAGIC_MASKS[bitsPerEntry];

            final long oldBlock = block >> bitIndex & clear;
            if (oldBlock == value)
                return; // Trying to place the same block
            final boolean currentAir = oldBlock == 0;

            final long indexClear = clear << bitIndex;
            block &= ~indexClear;
            block |= (long) value << bitIndex;

            if (currentAir != placedAir) {
                // Block count changed
                this.count += currentAir ? 1 : -1;
            }
            values[index] = block;
        }
    }

    @Override
    public void fill(int value) {
        if (value == 0) {
            Arrays.fill(values, 0);
            this.count = 0;
            return;
        }
        value = getPaletteIndex(value);
        final int bitsPerEntry = this.bitsPerEntry;
        final int valuesPerLong = VALUES_PER_LONG[bitsPerEntry];
        final long[] values = this.values;
        long block = 0;
        for (int i = 0; i < valuesPerLong; i++)
            block |= (long) value << i * bitsPerEntry;
        Arrays.fill(values, block);
        this.count = maxSize();
    }

    @Override
    public void setAll(@NotNull EntrySupplier supplier) {
        int[] cache = sizeCache(maxSize());
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
                        value = getPaletteIndex(value);
                        count++;
                    }
                    cache[index++] = value;
                }
            }
        }
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
        final int oldValue = get(x, y, z);
        final int newValue = operator.applyAsInt(oldValue);
        if (oldValue != newValue) set(x, y, z, newValue);
    }

    @Override
    public void replaceAll(@NotNull EntryFunction function) {
        int[] cache = sizeCache(maxSize());
        AtomicInteger arrayIndex = new AtomicInteger();
        AtomicInteger count = new AtomicInteger();
        getAll((x, y, z, value) -> {
            final int newValue = function.apply(x, y, z, value);
            final int index = arrayIndex.getPlain();
            arrayIndex.setPlain(index + 1);
            cache[index] = newValue != value ? getPaletteIndex(newValue) : value;
            if (newValue != 0) count.setPlain(count.getPlain() + 1);
        });
        // Update palette content
        updateAll(cache);
        this.count = count.getPlain();
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public int bitsPerEntry() {
        return bitsPerEntry;
    }

    @Override
    public int maxBitsPerEntry() {
        return adaptivePalette.maxBitsPerEntry();
    }

    @Override
    public int dimension() {
        return adaptivePalette.dimension();
    }

    @Override
    public @NotNull SpecializedPalette clone() {
        try {
            FlexiblePalette palette = (FlexiblePalette) super.clone();
            palette.values = values != null ? values.clone() : null;
            palette.paletteToValueList = paletteToValueList.clone();
            palette.valueToPaletteMap = valueToPaletteMap.clone();
            palette.count = count;
            return palette;
        } catch (CloneNotSupportedException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            throw new IllegalStateException("Weird thing happened");
        }
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte((byte) bitsPerEntry);
        if (bitsPerEntry <= maxBitsPerEntry()) { // Palette index
            writer.writeVarIntList(paletteToValueList, BinaryWriter::writeVarInt);
        }
        writer.writeLongArray(values);
    }

    private int fixBitsPerEntry(int bitsPerEntry) {
        return bitsPerEntry > maxBitsPerEntry() ? 15 : bitsPerEntry;
    }

    private void retrieveAll(@NotNull EntryConsumer consumer, boolean consumeEmpty) {
        final long[] values = this.values;
        final int dimension = this.dimension();
        final int bitsPerEntry = this.bitsPerEntry;
        final int magicMask = MAGIC_MASKS[bitsPerEntry];
        final int valuesPerLong = VALUES_PER_LONG[bitsPerEntry];
        final int size = maxSize();
        final int dimensionMinus = dimension - 1;
        final int[] ids = hasPalette ? paletteToValueList.elements() : null;
        final int dimensionBitCount = adaptivePalette.dimensionBitCount;
        final int shiftedDimensionBitCount = dimensionBitCount << 1;
        for (int i = 0; i < values.length; i++) {
            final long value = values[i];
            int index = i * valuesPerLong;
            final int maxIndex = Math.min(index + valuesPerLong, size);
            if (value == 0) {
                // No values in this long, skip
                if (consumeEmpty) {
                    for (; index < maxIndex; index++) {
                        final int y = index >> shiftedDimensionBitCount;
                        final int z = index >> dimensionBitCount & dimensionMinus;
                        final int x = index & dimensionMinus;
                        consumer.accept(x, y, z, 0);
                    }
                }
                continue;
            }
            int bitIndex = 0;
            for (; index < maxIndex; index++) {
                final short paletteIndex = (short) (value >> bitIndex & magicMask);
                if (paletteIndex != 0 || consumeEmpty) {
                    final int result = ids != null ? ids[paletteIndex] : paletteIndex;
                    final int y = index >> shiftedDimensionBitCount;
                    final int z = index >> dimensionBitCount & dimensionMinus;
                    final int x = index & dimensionMinus;
                    consumer.accept(x, y, z, result);
                }
                bitIndex += bitsPerEntry;
            }
        }
    }

    private void updateAll(int[] paletteValues) {
        final int size = maxSize();
        assert paletteValues.length >= size;
        final int bitsPerEntry = this.bitsPerEntry;
        final int valuesPerLong = VALUES_PER_LONG[bitsPerEntry];
        final long[] values = this.values;
        final int magicMask = MAGIC_MASKS[bitsPerEntry];
        for (int i = 0; i < values.length; i++) {
            long block = values[i];
            int index = i * valuesPerLong;
            final int maxIndex = Math.min(index + valuesPerLong, size);
            int bitIndex = 0;
            for (; index < maxIndex; index++) {
                block &= ~((long) magicMask << bitIndex);
                block |= (long) paletteValues[index] << bitIndex;
                bitIndex += bitsPerEntry;
            }
            values[i] = block;
        }
    }

    private void resize(int newBitsPerEntry) {
        FlexiblePalette palette = new FlexiblePalette(adaptivePalette, fixBitsPerEntry(newBitsPerEntry));
        palette.lastPaletteIndex = lastPaletteIndex;
        palette.paletteToValueList = paletteToValueList;
        palette.valueToPaletteMap = valueToPaletteMap;
        getAll(palette::set);
        this.bitsPerEntry = palette.bitsPerEntry;
        this.hasPalette = palette.hasPalette;
        this.lastPaletteIndex = palette.lastPaletteIndex;
        this.count = palette.count;
        this.values = palette.values;
    }

    private int getPaletteIndex(int value) {
        if (!hasPalette) return value;
        final int lastPaletteIndex = this.lastPaletteIndex;
        if (lastPaletteIndex >= maxPaletteSize(bitsPerEntry)) {
            // Palette is full, must resize
            resize(bitsPerEntry + adaptivePalette.bitsIncrement);
            return getPaletteIndex(value);
        }
        final int lookup = valueToPaletteMap.putIfAbsent(value, lastPaletteIndex);
        if (lookup != -1) return lookup;
        this.lastPaletteIndex = lastPaletteIndex + 1;
        this.paletteToValueList.add(value);
        return lastPaletteIndex;
    }

    int getSectionIndex(int x, int y, int z) {
        final int dimensionBitCount = adaptivePalette.dimensionBitCount;
        return y << (dimensionBitCount << 1) | z << dimensionBitCount | x;
    }

    static int[] sizeCache(int size) {
        int[] cache = WRITE_CACHE.get();
        if (cache.length < size) {
            cache = new int[size];
            WRITE_CACHE.set(cache);
        }
        return cache;
    }

    int valuesLength(int bitsPerEntry) {
        int valuesPerLong = VALUES_PER_LONG[bitsPerEntry];
        return (maxSize() + valuesPerLong - 1) / valuesPerLong;
    }

    static int maxPaletteSize(int bitsPerEntry) {
        return 1 << bitsPerEntry;
    }
}
