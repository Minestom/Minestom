package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Chunk;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.instance.Chunk.CHUNK_SECTION_SIZE;

final class PaletteImpl implements Palette {
    // Magic values generated with "Integer.MAX_VALUE >> (31 - bitsPerIndex)" for bitsPerIndex between 4 and 15
    private static final int[] MAGIC_MASKS =
            {0, 0, 0, 0,
                    15, 31, 63, 127, 255,
                    511, 1023, 2047, 4095,
                    8191, 16383, 32767};

    // Specific to this palette type
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
    private int[] paletteToValueArray;
    // value = palette index
    private Int2IntOpenHashMap valueToPaletteMap = new Int2IntOpenHashMap();

    PaletteImpl(int size, int maxBitsPerEntry, int bitsPerEntry, int bitsIncrement) {
        this.size = size;
        this.maxBitsPerEntry = maxBitsPerEntry;

        this.bitsPerEntry = bitsPerEntry;
        this.bitsIncrement = bitsIncrement;

        this.valuesPerLong = Long.SIZE / bitsPerEntry;
        this.hasPalette = bitsPerEntry <= maxBitsPerEntry;

        this.paletteToValueArray = new int[1 << bitsPerEntry];

        this.valueToPaletteMap.put(0, 0);
    }

    @Override
    public int get(int x, int y, int z) {
        if (values.length == 0) {
            // Section is not loaded, can only be air
            return -1;
        }
        final int sectionIdentifier = getSectionIndex(x, y, z);
        final int index = sectionIdentifier / valuesPerLong;
        final int bitIndex = sectionIdentifier % valuesPerLong * bitsPerEntry;
        final short value = (short) (values[index] >> bitIndex & MAGIC_MASKS[bitsPerEntry]);
        // Change to palette value and return
        return hasPalette ? paletteToValueArray[value] : value;
    }

    @Override
    public void set(int x, int y, int z, int value) {
        final boolean placedAir = value == 0;
        if (values.length == 0) {
            if (placedAir) {
                // Section is empty and method is trying to place an air block, stop unnecessary computation
                return;
            }
            // Initialize the section
            this.values = new long[(size + valuesPerLong - 1) / valuesPerLong];
        }
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
    public int count() {
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
    public int size() {
        return size;
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
            palette.paletteToValueArray = paletteToValueArray.clone();
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
            final int paletteSize = lastPaletteIndex + 1;
            writer.writeVarInt(paletteSize);
            for (int i = 0; i < paletteSize; i++) {
                writer.writeVarInt(paletteToValueArray[i]);
            }
        }
        // Raw
        writer.writeVarInt(values.length);
        for (long datum : values) {
            writer.writeLong(datum);
        }
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

        this.paletteToValueArray = palette.paletteToValueArray;
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

        if (lastPaletteIndex >= paletteToValueArray.length) {
            // Palette is full, must resize
            resize(bitsPerEntry + bitsIncrement);
            if (!hasPalette) return value;
        }
        final int paletteIndex = lastPaletteIndex++;
        this.paletteToValueArray[paletteIndex] = value;
        this.valueToPaletteMap.put(value, paletteIndex);
        return paletteIndex;
    }

    static int getSectionIndex(int x, int y, int z) {
        y = Math.floorMod(y, CHUNK_SECTION_SIZE);
        return y << 8 | z << 4 | x;
    }
}
