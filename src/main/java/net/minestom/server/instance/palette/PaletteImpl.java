package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Chunk;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

final class PaletteImpl implements Palette, Cloneable {
    // Magic values generated with "Integer.MAX_VALUE >> (31 - bitsPerIndex)" for bitsPerIndex between 1 and 16
    private static final int[] MAGIC_MASKS =
            {0, 1, 3, 7,
                    15, 31, 63, 127, 255,
                    511, 1023, 2047, 4095,
                    8191, 16383, 32767};

    // Specific to this palette type
    private final int dimension;
    private final int size;
    private final int maxBitsPerEntry;

    private int bitsPerEntry;
    private final int bitsIncrement;

    private int valuesPerLong;
    private boolean hasPalette;
    private int lastPaletteIndex = 1; // First index is air

    private int count = 0;

    private long[] values = new long[0];
    // palette index = value
    private IntArrayList paletteToValueList;
    // value = palette index
    private Int2IntOpenHashMap valueToPaletteMap;

    PaletteImpl(int dimension, int maxBitsPerEntry, int bitsPerEntry, int bitsIncrement) {
        this.dimension = dimension;
        this.size = dimension * dimension * dimension;
        this.maxBitsPerEntry = maxBitsPerEntry;

        this.bitsPerEntry = bitsPerEntry;
        this.bitsIncrement = bitsIncrement;

        this.valuesPerLong = Long.SIZE / bitsPerEntry;
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
        if (values.length == 0) {
            // Section is not loaded, return default value
            return 0;
        }
        x %= dimension;
        y %= dimension;
        z %= dimension;
        final int sectionIdentifier = getSectionIndex(x, y, z);
        final int index = sectionIdentifier / valuesPerLong;
        final int bitIndex = sectionIdentifier % valuesPerLong * bitsPerEntry;
        final short value = (short) (values[index] >> bitIndex & MAGIC_MASKS[bitsPerEntry]);
        // Change to palette value and return
        return hasPalette ? paletteToValueList.getInt(value) : value;
    }

    @Override
    public void set(int x, int y, int z, int value) {
        if (x < 0 || y < 0 || z < 0) {
            throw new IllegalArgumentException("Coordinates must be positive");
        }
        final boolean placedAir = value == 0;
        if (values.length == 0) {
            if (placedAir) {
                // Section is empty and method is trying to place an air block, stop unnecessary computation
                return;
            }
            // Initialize the section
            this.values = new long[(size + valuesPerLong - 1) / valuesPerLong];
        }
        x %= dimension;
        y %= dimension;
        z %= dimension;
        // Change to palette value
        value = getPaletteIndex(value);
        final int sectionIndex = getSectionIndex(x, y, z);
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
        PaletteImpl palette = new PaletteImpl(size, maxBitsPerEntry, newBitsPerEntry, bitsIncrement);
        for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
            for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                    palette.set(x, y, z, get(x, y, z));
                }
            }
        }

        this.paletteToValueList = palette.paletteToValueList;
        this.lastPaletteIndex = palette.lastPaletteIndex;

        this.bitsPerEntry = palette.bitsPerEntry;

        this.valuesPerLong = palette.valuesPerLong;
        this.hasPalette = palette.hasPalette;

        this.values = palette.values;
        this.count = palette.count;
    }

    private int getPaletteIndex(int value) {
        if (!hasPalette) return value;
        final int lookup = valueToPaletteMap.getOrDefault(value, (short) -1);
        if (lookup != -1) return lookup;

        if (lastPaletteIndex >= maxPaletteSize(bitsPerEntry)) {
            // Palette is full, must resize
            resize(bitsPerEntry + bitsIncrement);
            if (!hasPalette) return value;
        }
        final int paletteIndex = lastPaletteIndex++;
        this.paletteToValueList.add(value);
        this.valueToPaletteMap.put(value, paletteIndex);
        return paletteIndex;
    }

    int getSectionIndex(int x, int y, int z) {
        y = Math.floorMod(y, dimension);
        return y << (dimension / 2) | z << (dimension / 4) | x;
    }

    static int maxPaletteSize(int bitsPerEntry) {
        return 1 << bitsPerEntry;
    }
}
