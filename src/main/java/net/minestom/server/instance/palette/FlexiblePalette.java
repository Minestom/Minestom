package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.MathUtils;
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

    // Specific to this palette type
    private final AdaptivePalette adaptivePalette;
    private byte bitsPerEntry;
    private int count;

    private long[] values;
    // palette index = value
    IntArrayList paletteToValueList;
    // value = palette index
    private Int2IntOpenHashMap valueToPaletteMap;

    FlexiblePalette(AdaptivePalette adaptivePalette, byte bitsPerEntry) {
        this.adaptivePalette = adaptivePalette;

        this.bitsPerEntry = bitsPerEntry;

        this.paletteToValueList = new IntArrayList(1);
        this.paletteToValueList.add(0);
        this.valueToPaletteMap = new Int2IntOpenHashMap(1);
        this.valueToPaletteMap.put(0, 0);
        this.valueToPaletteMap.defaultReturnValue(-1);

        final int valuesPerLong = 64 / bitsPerEntry;
        this.values = new long[(maxSize() + valuesPerLong - 1) / valuesPerLong];
    }

    FlexiblePalette(AdaptivePalette adaptivePalette) {
        this(adaptivePalette, adaptivePalette.defaultBitsPerEntry);
    }

    @Override
    public int get(int x, int y, int z) {
        final int bitsPerEntry = this.bitsPerEntry;
        final int sectionIndex = getSectionIndex(x, y, z);
        final int valuesPerLong = 64 / bitsPerEntry;
        final int index = sectionIndex / valuesPerLong;
        final int bitIndex = (sectionIndex - index * valuesPerLong) * bitsPerEntry;
        final int value = (int) (values[index] >> bitIndex) & ((1 << bitsPerEntry) - 1);
        // Change to palette value and return
        return hasPalette() ? paletteToValueList.getInt(value) : value;
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
        value = getPaletteIndex(value);
        final int bitsPerEntry = this.bitsPerEntry;
        final long[] values = this.values;
        // Change to palette value
        final int valuesPerLong = 64 / bitsPerEntry;
        final int sectionIndex = getSectionIndex(x, y, z);
        final int index = sectionIndex / valuesPerLong;
        final int bitIndex = (sectionIndex - index * valuesPerLong) * bitsPerEntry;

        final long block = values[index];
        final long clear = (1L << bitsPerEntry) - 1;
        final long oldBlock = block >> bitIndex & clear;
        if (oldBlock == value)
            return; // Trying to place the same block
        values[index] = block & ~(clear << bitIndex) | (value & clear) << bitIndex;
        // Check if block count needs to be updated
        final boolean currentAir = oldBlock == 0;
        if (currentAir != (value == 0)) {
            if (currentAir) count++;
            else count--;
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
        final int valuesPerLong = 64 / bitsPerEntry;
        final long[] values = this.values;
        long block = 0;
        for (int i = 0; i < valuesPerLong; i++)
            block |= (long) value << i * bitsPerEntry;
        Arrays.fill(values, block);
        this.count = maxSize();
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
        int[] cache = WRITE_CACHE.get();
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
        writer.writeByte(bitsPerEntry);
        if (bitsPerEntry <= maxBitsPerEntry()) { // Palette index
            writer.writeVarIntList(paletteToValueList, BinaryWriter::writeVarInt);
        }
        writer.writeLongArray(values);
    }

    private void retrieveAll(@NotNull EntryConsumer consumer, boolean consumeEmpty) {
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
            } else {
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
    }

    private void updateAll(int[] paletteValues) {
        final int size = maxSize();
        assert paletteValues.length >= size;
        final int bitsPerEntry = this.bitsPerEntry;
        final int valuesPerLong = 64 / bitsPerEntry;
        final long[] values = this.values;
        final int magicMask = (1 << bitsPerEntry) - 1;
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

    void resize(byte newBitsPerEntry) {
        newBitsPerEntry = newBitsPerEntry > maxBitsPerEntry() ? 15 : newBitsPerEntry;
        FlexiblePalette palette = new FlexiblePalette(adaptivePalette, newBitsPerEntry);
        palette.paletteToValueList = paletteToValueList;
        palette.valueToPaletteMap = valueToPaletteMap;
        getAll(palette::set);
        this.bitsPerEntry = palette.bitsPerEntry;
        this.values = palette.values;
        assert this.count == palette.count;
    }

    private int getPaletteIndex(int value) {
        if (!hasPalette()) return value;
        final int lastPaletteIndex = this.paletteToValueList.size();
        final byte bpe = this.bitsPerEntry;
        if (lastPaletteIndex >= maxPaletteSize(bpe)) {
            // Palette is full, must resize
            resize((byte) (bpe + 1));
            return getPaletteIndex(value);
        }
        final int lookup = valueToPaletteMap.putIfAbsent(value, lastPaletteIndex);
        if (lookup != -1) return lookup;
        this.paletteToValueList.add(value);
        return lastPaletteIndex;
    }

    boolean hasPalette() {
        return bitsPerEntry <= maxBitsPerEntry();
    }

    int getSectionIndex(int x, int y, int z) {
        final int dimensionMask = dimension() - 1;
        y &= dimensionMask;
        z &= dimensionMask;
        x &= dimensionMask;
        final int dimensionBitCount = MathUtils.bitsToRepresent(dimensionMask);
        return y << (dimensionBitCount << 1) | z << dimensionBitCount | x;
    }

    static int maxPaletteSize(int bitsPerEntry) {
        return 1 << bitsPerEntry;
    }
}
