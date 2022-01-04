package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

final class PaletteImpl implements Palette, Cloneable {
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
    private final int dimension;
    private final int dimensionBitCount;
    private final int size;
    private final int maxBitsPerEntry;
    private final int bitsIncrement;

    private int bitsPerEntry;

    private boolean hasPalette;
    private int lastPaletteIndex = 1; // First index is air

    private int count = 0;

    private long[] values = new long[0];
    // palette index = value
    private IntArrayList paletteToValueList;
    // value = palette index
    private Int2IntOpenHashMap valueToPaletteMap;

    PaletteImpl(int dimension, int maxBitsPerEntry, int bitsPerEntry, int bitsIncrement) {
        this.dimensionBitCount = validateDimension(dimension);

        this.dimension = dimension;
        this.size = dimension * dimension * dimension;
        this.maxBitsPerEntry = maxBitsPerEntry;
        this.bitsIncrement = bitsIncrement;

        this.bitsPerEntry = bitsPerEntry;

        this.hasPalette = bitsPerEntry <= maxBitsPerEntry;

        this.paletteToValueList = new IntArrayList(1);
        this.paletteToValueList.add(0);
        this.valueToPaletteMap = new Int2IntOpenHashMap(1);
        this.valueToPaletteMap.put(0, 0);
    }

    @Override
    public int get(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0) {
            throw new IllegalArgumentException("Coordinates must be positive");
        }
        final long[] values = this.values;
        if (values.length == 0) {
            // Section is not loaded, return default value
            return 0;
        }
        final int bitsPerEntry = this.bitsPerEntry;
        final int valuesPerLong = VALUES_PER_LONG[bitsPerEntry];

        final int sectionIdentifier = getSectionIndex(x % dimension, y % dimension, z % dimension);
        final int index = sectionIdentifier / valuesPerLong;
        final int bitIndex = sectionIdentifier % valuesPerLong * bitsPerEntry;
        final short value = (short) (values[index] >> bitIndex & MAGIC_MASKS[bitsPerEntry]);
        // Change to palette value and return
        return hasPalette ? paletteToValueList.getInt(value) : value;
    }

    @Override
    public void getAll(@NotNull EntryConsumer consumer) {
        // TODO optimize
        for (int x = 0; x < dimension; x++) {
            for (int y = 0; y < dimension; y++) {
                for (int z = 0; z < dimension; z++) {
                    consumer.accept(x, y, z, get(x, y, z));
                }
            }
        }
    }

    @Override
    public void set(int x, int y, int z, int value) {
        if (x < 0 || y < 0 || z < 0) {
            throw new IllegalArgumentException("Coordinates must be positive");
        }
        final boolean placedAir = value == 0;
        if (!placedAir) value = getPaletteIndex(value);
        final int bitsPerEntry = this.bitsPerEntry;
        final int valuesPerLong = VALUES_PER_LONG[bitsPerEntry];
        long[] values = this.values;
        if (values.length == 0) {
            if (placedAir) {
                // Section is empty and method is trying to place an air block, stop unnecessary computation
                return;
            }
            // Initialize the section
            this.values = values = new long[(size + valuesPerLong - 1) / valuesPerLong];
        }
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
            block |= indexClear;
            block ^= indexClear;
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
        final boolean placedAir = value == 0;
        if (!placedAir) value = getPaletteIndex(value);
        final int bitsPerEntry = this.bitsPerEntry;
        final int valuesPerLong = VALUES_PER_LONG[bitsPerEntry];
        long[] values = this.values;
        if (values.length == 0) {
            if (placedAir) {
                // Section is empty and method is trying to place an air block, stop unnecessary computation
                return;
            }
            // Initialize the section
            this.values = values = new long[(size + valuesPerLong - 1) / valuesPerLong];
        }

        long block = 0;
        for (int i = 0; i < valuesPerLong; i++) {
            block |= (long) value << i * bitsPerEntry;
        }
        Arrays.fill(values, block);
        this.count = placedAir ? 0 : maxSize();
    }

    @Override
    public void setAll(@NotNull EntrySupplier supplier) {
        // TODO optimize
        for (int x = 0; x < dimension; x++) {
            for (int y = 0; y < dimension; y++) {
                for (int z = 0; z < dimension; z++) {
                    set(x, y, z, supplier.get(x, y, z));
                }
            }
        }
    }

    @Override
    public int size() {
        return count;
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
    public int maxSize() {
        return size;
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @Override
    public long[] data() {
        return values;
    }

    @Override
    public @NotNull Palette clone() {
        try {
            PaletteImpl palette = (PaletteImpl) super.clone();
            palette.values = values.clone();
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
        // Palette
        if (bitsPerEntry < 9) {
            // Palette has to exist
            writer.writeVarIntList(paletteToValueList, BinaryWriter::writeVarInt);
        }
        // Raw
        writer.writeLongArray(values);
    }

    private int fixBitsPerEntry(int bitsPerEntry) {
        return bitsPerEntry > maxBitsPerEntry ? 15 : bitsPerEntry;
    }

    private void resize(int newBitsPerEntry) {
        newBitsPerEntry = fixBitsPerEntry(newBitsPerEntry);
        PaletteImpl palette = new PaletteImpl(dimension, maxBitsPerEntry, newBitsPerEntry, bitsIncrement);
        for (int x = 0; x < dimension; x++) {
            for (int y = 0; y < dimension; y++) {
                for (int z = 0; z < dimension; z++) {
                    palette.set(x, y, z, get(x, y, z));
                }
            }
        }

        this.bitsPerEntry = palette.bitsPerEntry;

        this.hasPalette = palette.hasPalette;
        this.lastPaletteIndex = palette.lastPaletteIndex;
        this.count = palette.count;

        this.values = palette.values;
        this.paletteToValueList = palette.paletteToValueList;
        this.valueToPaletteMap = palette.valueToPaletteMap;
    }

    private int getPaletteIndex(int value) {
        if (!hasPalette) return value;
        final int lookup = valueToPaletteMap.getOrDefault(value, (short) -1);
        if (lookup != -1) return lookup;

        if (lastPaletteIndex >= maxPaletteSize(bitsPerEntry)) {
            // Palette is full, must resize
            resize(bitsPerEntry + bitsIncrement);
            return getPaletteIndex(value);
        }
        final int paletteIndex = lastPaletteIndex++;
        this.paletteToValueList.add(value);
        this.valueToPaletteMap.put(value, paletteIndex);
        return paletteIndex;
    }

    int getSectionIndex(int x, int y, int z) {
        return y << (dimensionBitCount << 1) | z << dimensionBitCount | x;
    }

    static int maxPaletteSize(int bitsPerEntry) {
        return 1 << bitsPerEntry;
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
