package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.shorts.Short2ShortLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortOpenHashMap;
import net.minestom.server.instance.Chunk;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.instance.Chunk.CHUNK_SECTION_COUNT;
import static net.minestom.server.instance.Chunk.CHUNK_SECTION_SIZE;

/**
 * Used to efficiently store blocks with an optional palette.
 * <p>
 * The format used is the one described in the {@link net.minestom.server.network.packet.server.play.ChunkDataPacket},
 * the reason is that it allows us to write the packet much faster.
 */
public class PaletteStorage {

    private final static int MAXIMUM_BITS_PER_ENTRY = 15;
    private final static int PALETTE_MAXIMUM_BITS = 8;
    private final static int BLOCK_COUNT = CHUNK_SECTION_SIZE * CHUNK_SECTION_SIZE * CHUNK_SECTION_SIZE;

    private int bitsPerEntry;
    private final int bitsIncrement;

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
        Check.argCondition(bitsPerEntry > MAXIMUM_BITS_PER_ENTRY, "The maximum bits per entry is 15");
        // Change the bitsPerEntry to be valid
        if (bitsPerEntry < 4) {
            bitsPerEntry = 4;
        } else if (MathUtils.isBetween(bitsPerEntry, 9, 14)) {
            bitsPerEntry = MAXIMUM_BITS_PER_ENTRY;
        }

        this.bitsPerEntry = bitsPerEntry;
        this.bitsIncrement = bitsIncrement;

        this.valuesPerLong = Long.SIZE / bitsPerEntry;
        this.hasPalette = bitsPerEntry <= PALETTE_MAXIMUM_BITS;
    }

    public void setBlockAt(int x, int y, int z, short blockId) {
        PaletteStorage.setBlockAt(this, x, y, z, blockId);
    }

    public short getBlockAt(int x, int y, int z) {
        return PaletteStorage.getBlockAt(this, x, y, z);
    }

    /**
     * Gets the number of bits that the palette currently take per block.
     *
     * @return the bits per entry
     */
    public int getBitsPerEntry() {
        return bitsPerEntry;
    }

    /**
     * Gets the palette with the index and the block id as the value.
     *
     * @return the palette
     */
    public short[] getPalette() {
        return paletteBlockMap.values().toShortArray();
    }

    /**
     * Gets the sections of this object,
     * the first array representing the chunk section and the second the block position from {@link #getSectionIndex(int, int, int)}.
     *
     * @return the section blocks
     */
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

    /**
     * Retrieves the palette index for the specified block id.
     * <p>
     * Also responsible for resizing the palette when full.
     *
     * @param blockId the block id to convert
     * @return the palette index of {@code blockId}
     */
    private short getPaletteIndex(short blockId) {
        if (!hasPalette) {
            return blockId;
        }

        if (!blockPaletteMap.containsKey(blockId)) {
            // Resize the palette if full
            if (paletteBlockMap.size() >= getMaxPaletteSize()) {
                resize(bitsPerEntry + bitsIncrement);
            }

            final short paletteIndex = (short) (paletteBlockMap.lastShortKey() + 1);
            this.paletteBlockMap.put(paletteIndex, blockId);
            this.blockPaletteMap.put(blockId, paletteIndex);
            return paletteIndex;
        }

        return blockPaletteMap.get(blockId);
    }

    /**
     * Resizes the array.
     * <p>
     * Will create a new palette storage to set all the current blocks, and the data will be transferred to 'this'.
     *
     * @param newBitsPerEntry the new bits per entry count
     */
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

    /**
     * Gets the maximum number of blocks that the current palette (could be the global one) can take.
     *
     * @return the number of blocks possible in the palette
     */
    private int getMaxPaletteSize() {
        return 1 << bitsPerEntry;
    }

    // Magic values generated with "Integer.MAX_VALUE >> (31 - bitsPerIndex)" for bitsPerIndex between 4 and 15
    private static final int[] MAGIC_MASKS =
            {0, 0, 0, 0,
                    15, 31, 63, 127, 255,
                    511, 1023, 2047, 4095,
                    8191, 16383, 32767};

    private static void setBlockAt(@NotNull PaletteStorage paletteStorage, int x, int y, int z, short blockId) {
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
                // Section is empty and method is trying to place an air block, stop unnecessary computation
                return;
            }

            // Initialize the section
            paletteStorage.sectionBlocks[section] = new long[getSize(valuesPerLong)];
        }

        final long[] sectionBlock = paletteStorage.sectionBlocks[section];

        long block = sectionBlock[index];
        {
            final long clear = MAGIC_MASKS[bitsPerEntry];

            block |= clear << bitIndex;
            block ^= clear << bitIndex;
            block |= (long) blockId << bitIndex;

            sectionBlock[index] = block;
        }
    }

    private static short getBlockAt(@NotNull PaletteStorage paletteStorage, int x, int y, int z) {
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
            // Section is not loaded, can only be air
            return 0;
        }

        final long value = blocks[index] >> bitIndex & MAGIC_MASKS[bitsPerEntry];

        // Change to palette value and return
        return paletteStorage.hasPalette ?
                paletteStorage.paletteBlockMap.get((short) value) :
                (short) value;
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

    public static int getSectionIndex(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

}
