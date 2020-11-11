package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.shorts.Short2ShortLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortOpenHashMap;
import net.minestom.server.instance.Chunk;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.chunk.ChunkUtils;

import static net.minestom.server.instance.Chunk.CHUNK_SECTION_COUNT;
import static net.minestom.server.instance.Chunk.CHUNK_SECTION_SIZE;

public class PaletteStorage {

    private final static int MAXIMUM_BITS_PER_ENTRY = 15;
    private final static int PALETTE_MAXIMUM_BITS = 8;
    private final static int BLOCK_COUNT = CHUNK_SECTION_SIZE * CHUNK_SECTION_SIZE * CHUNK_SECTION_SIZE;

    private int bitsPerEntry;
    private int bitsIncrement;

    private int valuesPerLong;
    private boolean hasPalette;

    private long[][] sectionBlocks = new long[CHUNK_SECTION_COUNT][0];

    // palette index = block id
    private Short2ShortLinkedOpenHashMap paletteBlockMap = new Short2ShortLinkedOpenHashMap(CHUNK_SECTION_SIZE);
    // block id = palette index
    private Short2ShortOpenHashMap blockPaletteMap = new Short2ShortOpenHashMap(CHUNK_SECTION_SIZE);

    {
        // Default value
        this.paletteBlockMap.put((short) 0, (short) 0);
        this.blockPaletteMap.put((short) 0, (short) 0);
    }

    public PaletteStorage(int bitsPerEntry, int bitsIncrement) {
        // Change the bitsPerEntry to be valid
        if (bitsPerEntry < 4) {
            bitsPerEntry = 4;
        } else if (MathUtils.isBetween(bitsPerEntry, 9, 14) || bitsPerEntry > MAXIMUM_BITS_PER_ENTRY) {
            bitsPerEntry = MAXIMUM_BITS_PER_ENTRY;
        }

        this.bitsPerEntry = bitsPerEntry;
        this.bitsIncrement = bitsIncrement;

        this.valuesPerLong = Long.SIZE / bitsPerEntry;
        this.hasPalette = bitsPerEntry <= PALETTE_MAXIMUM_BITS;
    }

    private synchronized void resize(int newBitsPerEntry) {
        PaletteStorage paletteStorageCache = new PaletteStorage(newBitsPerEntry, bitsIncrement);
        paletteStorageCache.paletteBlockMap = paletteBlockMap;
        paletteStorageCache.blockPaletteMap = blockPaletteMap;

        for (int y = 0; y < Chunk.CHUNK_SIZE_Y; y++) {
            for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                    final short blockId = getBlockAt(x, y, z);
                    paletteStorageCache.setBlockAt(x, y, z, blockId);
                }
            }
        }

        this.bitsPerEntry = newBitsPerEntry;

        this.valuesPerLong = paletteStorageCache.valuesPerLong;
        this.hasPalette = paletteStorageCache.hasPalette;

        this.sectionBlocks = paletteStorageCache.sectionBlocks;

    }

    public void setBlockAt(int x, int y, int z, short blockId) {
        PaletteStorage.setBlockAt(this, x, y, z, blockId);
    }

    public short getBlockAt(int x, int y, int z) {
        return PaletteStorage.getBlockAt(this, x, y, z);
    }

    public int getBitsPerEntry() {
        return bitsPerEntry;
    }

    public short[] getPalette() {
        return paletteBlockMap.values().toShortArray();
    }

    public long[][] getSectionBlocks() {
        return sectionBlocks;
    }

    public PaletteStorage copy() {
        PaletteStorage paletteStorage = new PaletteStorage(bitsPerEntry, bitsIncrement);
        paletteStorage.sectionBlocks = sectionBlocks.clone();

        paletteStorage.paletteBlockMap.clear();
        paletteStorage.blockPaletteMap.clear();

        paletteStorage.paletteBlockMap.putAll(paletteBlockMap);
        paletteStorage.blockPaletteMap.putAll(blockPaletteMap);

        return paletteStorage;
    }

    private short getPaletteIndex(short blockId) {
        if (!hasPalette) {
            return blockId;
        }

        if (!blockPaletteMap.containsKey(blockId)) {

            boolean resize = false;
            if (paletteBlockMap.size() >= getMaxPaletteSize()) {
                resize = true;
                // System.out.println("test " + paletteBlockMap.size() + " " + hashCode());
                resize(bitsPerEntry + bitsIncrement);
            }

            if (resize) {
                // System.out.println("new size " + paletteBlockMap.size() + " " + hashCode());
            }
            final short paletteIndex = (short) (paletteBlockMap.lastShortKey() + 1);
            this.paletteBlockMap.put(paletteIndex, blockId);
            this.blockPaletteMap.put(blockId, paletteIndex);
            return paletteIndex;
        }

        return blockPaletteMap.get(blockId);
    }

    private int getMaxPaletteSize() {
        return 1 << bitsPerEntry;
    }

    private static void setBlockAt(PaletteStorage paletteStorage, int x, int y, int z, short blockId) {
        x = toChunkCoordinate(x);
        z = toChunkCoordinate(z);

        // Change to palette value
        blockId = paletteStorage.getPaletteIndex(blockId);

        final int sectionIndex = getSectionIndex(x, y % CHUNK_SECTION_SIZE, z);

        final int valuesPerLong = paletteStorage.valuesPerLong;
        final int bitsPerEntry = paletteStorage.bitsPerEntry;

        final int index = sectionIndex / valuesPerLong;
        final int bitIndex = (sectionIndex % valuesPerLong) * bitsPerEntry;

        final int section = ChunkUtils.getSectionAt(y);

        if (paletteStorage.sectionBlocks[section].length == 0) {
            if (blockId == 0) {
                return;
            }

            paletteStorage.sectionBlocks[section] = new long[getSize(valuesPerLong)];
        }

        long[] sectionBlock = paletteStorage.sectionBlocks[section];

        long block = sectionBlock[index];
        {
            final long clear = Integer.MAX_VALUE >> (31 - bitsPerEntry);

            block |= clear << bitIndex;
            block ^= clear << bitIndex;
            block |= (long) blockId << bitIndex;

            sectionBlock[index] = block;

        }
    }

    private static short getBlockAt(PaletteStorage paletteStorage, int x, int y, int z) {
        x = toChunkCoordinate(x);
        z = toChunkCoordinate(z);

        final int sectionIndex = getSectionIndex(x, y % CHUNK_SECTION_SIZE, z);

        final int valuesPerLong = paletteStorage.valuesPerLong;
        final int bitsPerEntry = paletteStorage.bitsPerEntry;

        final int index = sectionIndex / valuesPerLong;
        final int bitIndex = sectionIndex % valuesPerLong * bitsPerEntry;

        final int section = ChunkUtils.getSectionAt(y);

        final long[] blocks = paletteStorage.sectionBlocks[section];

        if (blocks.length == 0) {
            return 0;
        }

        long mask = Integer.MAX_VALUE >> (31 - bitsPerEntry);
        long value = blocks[index] >> bitIndex & mask;

        // Change to palette value
        final short blockId = paletteStorage.hasPalette ?
                paletteStorage.paletteBlockMap.get((short) value) :
                (short) value;

        return blockId;
    }

    private static int getSize(int valuesPerLong) {
        return (BLOCK_COUNT + valuesPerLong - 1) / valuesPerLong;
    }

    private static int toChunkCoordinate(int xz) {
        xz %= 16;
        if (xz < 0) {
            xz += CHUNK_SECTION_SIZE;
        }

        return xz;
    }

    private static int getSectionIndex(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

}
