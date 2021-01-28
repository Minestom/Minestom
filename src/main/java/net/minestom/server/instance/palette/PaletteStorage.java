package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.shorts.Short2ShortLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Chunk;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.clone.PublicCloneable;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.instance.Chunk.CHUNK_SECTION_COUNT;
import static net.minestom.server.instance.Chunk.CHUNK_SECTION_SIZE;

/**
 * Used to efficiently store blocks with an optional palette.
 * <p>
 * The format used is the one described in the {@link net.minestom.server.network.packet.server.play.ChunkDataPacket},
 * the reason is that it allows us to write the packet much faster.
 */
public class PaletteStorage implements PublicCloneable<PaletteStorage> {

    /**
     * The maximum bits per entry value.
     */
    private final static int MAXIMUM_BITS_PER_ENTRY = 15;

    /**
     * The minimum bits per entry value.
     */
    private final static int MINIMUM_BITS_PER_ENTRY = 4;

    /**
     * The maximum bits per entry value which allow for a data palette.
     */
    private final static int PALETTE_MAXIMUM_BITS = 8;

    /**
     * The number of blocks that should be in one chunk section.
     */
    private final static int BLOCK_COUNT = CHUNK_SECTION_SIZE * CHUNK_SECTION_SIZE * CHUNK_SECTION_SIZE;

    private int bitsPerEntry;
    private final int bitsIncrement;

    private int valuesPerLong;
    private boolean hasPalette;

    private long[][] sectionBlocks;

    // chunk section - palette index = block id
    private Short2ShortLinkedOpenHashMap[] paletteBlockMaps;
    // chunk section - block id = palette index
    private Short2ShortOpenHashMap[] blockPaletteMaps;

    /**
     * Creates a new palette storage.
     *
     * @param bitsPerEntry  the number of bits used for one entry (block)
     * @param bitsIncrement the number of bits to add per-block once the palette array is filled
     */
    public PaletteStorage(int bitsPerEntry, int bitsIncrement) {
        Check.argCondition(bitsPerEntry > MAXIMUM_BITS_PER_ENTRY, "The maximum bits per entry is 15");
        bitsPerEntry = fixBitsPerEntry(bitsPerEntry);

        this.bitsPerEntry = bitsPerEntry;
        this.bitsIncrement = bitsIncrement;

        this.valuesPerLong = Long.SIZE / bitsPerEntry;
        this.hasPalette = bitsPerEntry <= PALETTE_MAXIMUM_BITS;

        init();
    }

    private void init() {
        this.sectionBlocks = new long[CHUNK_SECTION_COUNT][0];

        this.paletteBlockMaps = new Short2ShortLinkedOpenHashMap[CHUNK_SECTION_COUNT];
        this.blockPaletteMaps = new Short2ShortOpenHashMap[CHUNK_SECTION_COUNT];
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
     * @param section the chunk section to get the palette from
     * @return the palette
     */
    @Nullable
    public short[] getPalette(int section) {
        Short2ShortLinkedOpenHashMap paletteBlockMap = paletteBlockMaps[section];
        return paletteBlockMap != null ? paletteBlockMap.values().toShortArray() : null;
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

    /**
     * Loops through all the sections and blocks to find unused array (empty chunk section)
     * <p>
     * Useful after clearing one or multiple sections of a chunk. Can be unnecessarily expensive if the chunk
     * is composed of almost-empty sections since the loop will not stop until a non-air block is discovered.
     */
    public synchronized void clean() {
        for (int i = 0; i < sectionBlocks.length; i++) {
            long[] section = sectionBlocks[i];

            if (section.length != 0) {
                boolean canClear = true;
                for (long blockGroup : section) {
                    if (blockGroup != 0) {
                        canClear = false;
                        break;
                    }
                }
                if (canClear) {
                    sectionBlocks[i] = new long[0];
                }

            }

        }
    }

    /**
     * Clears all the data in the palette and data array.
     */
    public void clear() {
        init();
    }

    /**
     * @deprecated use {@link #clone()}
     */
    @Deprecated
    @NotNull
    public PaletteStorage copy() {
        return clone();
    }

    @NotNull
    @Override
    public PaletteStorage clone() {
        try {
            PaletteStorage paletteStorage = (PaletteStorage) super.clone();
            paletteStorage.sectionBlocks = sectionBlocks.clone();

            paletteStorage.paletteBlockMaps = paletteBlockMaps.clone();
            paletteStorage.blockPaletteMaps = blockPaletteMaps.clone();
            return paletteStorage;
        } catch (CloneNotSupportedException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            throw new IllegalStateException("Weird thing happened");
        }
    }

    /**
     * Retrieves the palette index for the specified block id.
     * <p>
     * Also responsible for resizing the palette when full.
     *
     * @param section the chunk section
     * @param blockId the block id to convert
     * @return the palette index of {@code blockId}
     */
    private short getPaletteIndex(int section, short blockId) {
        if (!hasPalette) {
            return blockId;
        }

        Short2ShortOpenHashMap blockPaletteMap = blockPaletteMaps[section];
        if (blockPaletteMap == null) {
            blockPaletteMap = createBlockPaletteMap();
            blockPaletteMaps[section] = blockPaletteMap;
        }

        if (!blockPaletteMap.containsKey(blockId)) {
            Short2ShortLinkedOpenHashMap paletteBlockMap = paletteBlockMaps[section];
            if (paletteBlockMap == null) {
                paletteBlockMap = createPaletteBlockMap();
                paletteBlockMaps[section] = paletteBlockMap;
            }

            // Resize the palette if full
            if (paletteBlockMap.size() >= getMaxPaletteSize()) {
                resize(bitsPerEntry + bitsIncrement);
            }

            final short paletteIndex = (short) (paletteBlockMap.lastShortKey() + 1);
            paletteBlockMap.put(paletteIndex, blockId);
            blockPaletteMap.put(blockId, paletteIndex);
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
    private void resize(int newBitsPerEntry) {
        // FIXME: artifacts when resizing
        newBitsPerEntry = fixBitsPerEntry(newBitsPerEntry);

        PaletteStorage paletteStorageCache = new PaletteStorage(newBitsPerEntry, bitsIncrement);
        paletteStorageCache.paletteBlockMaps = paletteBlockMaps;
        paletteStorageCache.blockPaletteMaps = blockPaletteMaps;

        for (int y = 0; y < Chunk.CHUNK_SIZE_Y; y++) {
            for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                    final short blockId = getBlockAt(x, y, z);
                    paletteStorageCache.setBlockAt(x, y, z, blockId);
                }
            }
        }

        this.bitsPerEntry = paletteStorageCache.bitsPerEntry;

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
        if (!MathUtils.isBetween(y, 0, Chunk.CHUNK_SIZE_Y - 1)) {
            return;
        }

        final int section = ChunkUtils.getSectionAt(y);

        final int valuesPerLong = paletteStorage.valuesPerLong;

        if (paletteStorage.sectionBlocks[section].length == 0) {
            if (blockId == 0) {
                // Section is empty and method is trying to place an air block, stop unnecessary computation
                return;
            }

            // Initialize the section
            paletteStorage.sectionBlocks[section] = new long[getSize(valuesPerLong)];
        }

        // Convert world coordinates to chunk coordinates
        x = toChunkCoordinate(x);
        z = toChunkCoordinate(z);

        // Change to palette value
        blockId = paletteStorage.getPaletteIndex(section, blockId);

        final int sectionIndex = getSectionIndex(x, y, z);

        final int index = sectionIndex / valuesPerLong;
        final int bitsPerEntry = paletteStorage.bitsPerEntry;

        final int bitIndex = (sectionIndex % valuesPerLong) * bitsPerEntry;

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
        if (y < 0 || y >= Chunk.CHUNK_SIZE_Y) {
            return 0;
        }

        final int section = ChunkUtils.getSectionAt(y);
        final long[] blocks;

        // Retrieve the longs and check if the section is empty
        {
            blocks = paletteStorage.sectionBlocks[section];

            if (blocks.length == 0) {
                // Section is not loaded, can only be air
                return 0;
            }
        }

        x = toChunkCoordinate(x);
        z = toChunkCoordinate(z);

        final int sectionIndex = getSectionIndex(x, y, z);

        final int valuesPerLong = paletteStorage.valuesPerLong;
        final int bitsPerEntry = paletteStorage.bitsPerEntry;

        final int index = sectionIndex / valuesPerLong;
        final int bitIndex = sectionIndex % valuesPerLong * bitsPerEntry;

        final long value = blocks[index] >> bitIndex & MAGIC_MASKS[bitsPerEntry];

        // Change to palette value and return
        return paletteStorage.hasPalette ?
                paletteStorage.paletteBlockMaps[section].get((short) value) :
                (short) value;
    }

    private static Short2ShortLinkedOpenHashMap createPaletteBlockMap() {
        Short2ShortLinkedOpenHashMap map = new Short2ShortLinkedOpenHashMap(CHUNK_SECTION_SIZE);
        map.put((short) 0, (short) 0);

        return map;
    }

    private static Short2ShortOpenHashMap createBlockPaletteMap() {
        Short2ShortOpenHashMap map = new Short2ShortOpenHashMap(CHUNK_SECTION_SIZE);
        map.put((short) 0, (short) 0);

        return map;
    }

    /**
     * Gets the array length of one section based on the number of values which can be stored in one long.
     *
     * @param valuesPerLong the number of values per long
     * @return the array length based on {@code valuesPerLong}
     */
    private static int getSize(int valuesPerLong) {
        return (BLOCK_COUNT + valuesPerLong - 1) / valuesPerLong;
    }

    /**
     * Converts a world coordinate to a chunk one.
     *
     * @param xz the world coordinate
     * @return the chunk coordinate of {@code xz}
     */
    private static int toChunkCoordinate(int xz) {
        xz %= 16;
        if (xz < 0) {
            xz += CHUNK_SECTION_SIZE;
        }

        return xz;
    }

    /**
     * Gets the index of the block on the section array based on the block position.
     *
     * @param x the chunk X
     * @param y the chunk Y
     * @param z the chunk Z
     * @return the section index of the position
     */
    public static int getSectionIndex(int x, int y, int z) {
        y %= CHUNK_SECTION_SIZE;
        return y << 8 | z << 4 | x;
    }

    /**
     * Fixes invalid bitsPerEntry values.
     * <p>
     * See https://wiki.vg/Chunk_Format#Direct
     *
     * @param bitsPerEntry the bits per entry value before fixing
     * @return the fixed bits per entry value
     */
    private static int fixBitsPerEntry(int bitsPerEntry) {
        if (bitsPerEntry < MINIMUM_BITS_PER_ENTRY) {
            return MINIMUM_BITS_PER_ENTRY;
        } else if (MathUtils.isBetween(bitsPerEntry, 9, 14)) {
            return MAXIMUM_BITS_PER_ENTRY;
        }
        return bitsPerEntry;
    }

}
