package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.shorts.Short2ShortOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Chunk;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.clone.PublicCloneable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.instance.Chunk.CHUNK_SECTION_SIZE;

/**
 * Represents a palette storing a complete chunk section.
 * <p>
 * 0 is always interpreted as being air, reason being that the block array will be filled with it during initialization.
 */
@ApiStatus.Internal
public final class Palette implements PublicCloneable<Palette> {

    /**
     * The maximum bits per entry value.
     */
    public final static int MAXIMUM_BITS_PER_ENTRY = 15;

    /**
     * The minimum bits per entry value.
     */
    public final static int MINIMUM_BITS_PER_ENTRY = 4;

    /**
     * The maximum bits per entry value which allow for a data palette.
     */
    public final static int PALETTE_MAXIMUM_BITS = 8;

    /**
     * The number of blocks that should be in one chunk section.
     */
    public final static int BLOCK_COUNT = CHUNK_SECTION_SIZE * CHUNK_SECTION_SIZE * CHUNK_SECTION_SIZE;

    // Magic values generated with "Integer.MAX_VALUE >> (31 - bitsPerIndex)" for bitsPerIndex between 4 and 15
    private static final int[] MAGIC_MASKS =
            {0, 0, 0, 0,
                    15, 31, 63, 127, 255,
                    511, 1023, 2047, 4095,
                    8191, 16383, 32767};

    private static final Short2ShortOpenHashMap MAP_TEMPLATE = new Short2ShortOpenHashMap(CHUNK_SECTION_SIZE);

    static {
        MAP_TEMPLATE.put((short) 0, (short) 0);
    }

    private long[] blocks;

    // palette index = block id
    private short[] paletteBlockArray;
    // block id = palette index
    private Short2ShortOpenHashMap blockPaletteMap;

    private int bitsPerEntry;
    private final int bitsIncrement;

    private int valuesPerLong;
    private boolean hasPalette;
    private int lastPaletteIndex;

    private short blockCount = 0;

    public Palette(int bitsPerEntry, int bitsIncrement) {
        this.bitsPerEntry = bitsPerEntry;
        this.bitsIncrement = bitsIncrement;

        this.valuesPerLong = Long.SIZE / bitsPerEntry;
        this.hasPalette = bitsPerEntry <= PALETTE_MAXIMUM_BITS;

        clear();
    }

    public void setBlockAt(int x, int y, int z, short blockId) {
        final boolean placedAir = blockId == 0;
        if (blocks.length == 0) {
            if (placedAir) {
                // Section is empty and method is trying to place an air block, stop unnecessary computation
                return;
            }
            // Initialize the section
            this.blocks = new long[(BLOCK_COUNT + valuesPerLong - 1) / valuesPerLong];
        }

        // Change to palette value
        blockId = getPaletteIndex(blockId);

        final int sectionIndex = getSectionIndex(x, y, z);

        final int index = sectionIndex / valuesPerLong;

        final int bitIndex = (sectionIndex % valuesPerLong) * bitsPerEntry;

        long block = blocks[index];
        {
            final long clear = MAGIC_MASKS[bitsPerEntry];

            final long oldBlock = block >> bitIndex & clear;
            if (oldBlock == blockId)
                return; // Trying to place the same block
            final boolean currentAir = oldBlock == 0;

            final long indexClear = clear << bitIndex;
            block |= indexClear;
            block ^= indexClear;
            block |= (long) blockId << bitIndex;

            if (currentAir != placedAir) {
                // Block count changed
                this.blockCount += (short) (currentAir ? 1 : -1);
            }
            blocks[index] = block;
        }
    }

    public short getBlockAt(int x, int y, int z) {
        if (blocks.length == 0) {
            // Section is not loaded, can only be air
            return -1;
        }
        final int sectionIdentifier = getSectionIndex(x, y, z);

        final int index = sectionIdentifier / valuesPerLong;
        final int bitIndex = sectionIdentifier % valuesPerLong * bitsPerEntry;

        final short value = (short) (blocks[index] >> bitIndex & MAGIC_MASKS[bitsPerEntry]);
        // Change to palette value and return
        return hasPalette ? paletteBlockArray[value] : value;
    }

    /**
     * Resizes the array.
     * <p>
     * Will create a new palette storage to set all the current blocks, and the data will be transferred to 'this'.
     *
     * @param newBitsPerEntry the new bits per entry count
     */
    public void resize(int newBitsPerEntry) {
        newBitsPerEntry = fixBitsPerEntry(newBitsPerEntry);

        Palette palette = new Palette(newBitsPerEntry, bitsIncrement);

        for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
            for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                    palette.setBlockAt(x, y, z, getBlockAt(x, y, z));
                }
            }
        }

        this.paletteBlockArray = palette.paletteBlockArray;
        this.lastPaletteIndex = palette.lastPaletteIndex;

        this.bitsPerEntry = palette.bitsPerEntry;

        this.valuesPerLong = palette.valuesPerLong;
        this.hasPalette = palette.hasPalette;

        this.blocks = palette.blocks;
        this.blockCount = palette.blockCount;
    }

    /**
     * Loops through all the sections and blocks to find unused array (empty chunk section)
     * <p>
     * Useful after clearing one or multiple sections of a chunk. Can be unnecessarily expensive if the chunk
     * is composed of almost-empty sections since the loop will not stop until a non-air block is discovered.
     */
    public synchronized void clean() {
        if (blocks.length != 0) {
            boolean canClear = true;
            for (long blockGroup : blocks) {
                if (blockGroup != 0) {
                    canClear = false;
                    break;
                }
            }
            if (canClear) {
                this.blocks = new long[0];
            }
        }
    }

    public void clear() {
        this.blocks = new long[0];
        this.paletteBlockArray = new short[1 << bitsPerEntry];
        this.blockPaletteMap = MAP_TEMPLATE.clone();
        this.blockCount = 0;
        this.lastPaletteIndex = 1; // First index is air
    }

    public long[] getBlocks() {
        return blocks;
    }

    public void setBlocks(long[] blocks) {
        this.blocks = blocks;
    }

    /**
     * Get the amount of non-air blocks in this section.
     *
     * @return The amount of blocks in this section.
     */
    public short getBlockCount() {
        return blockCount;
    }

    public void setBlockCount(short blockCount) {
        this.blockCount = blockCount;
    }

    public short[] getPaletteBlockArray() {
        return paletteBlockArray;
    }

    public int getLastPaletteIndex() {
        return lastPaletteIndex;
    }

    public Short2ShortOpenHashMap getBlockPaletteMap() {
        return blockPaletteMap;
    }

    public int getBitsPerEntry() {
        return bitsPerEntry;
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
        if (!hasPalette) return blockId;
        final short value = blockPaletteMap.getOrDefault(blockId, (short) -1);
        if (value != -1) return value;

        if (lastPaletteIndex >= paletteBlockArray.length) {
            // Palette is full, must resize
            resize(bitsPerEntry + bitsIncrement);
            if (!hasPalette) return blockId;
        }
        final short paletteIndex = (short) lastPaletteIndex++;
        this.paletteBlockArray[paletteIndex] = blockId;
        this.blockPaletteMap.put(blockId, paletteIndex);
        return paletteIndex;
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
        y = Math.floorMod(y, CHUNK_SECTION_SIZE);
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

    @Override
    public @NotNull Palette clone() {
        try {
            Palette palette = (Palette) super.clone();
            palette.blocks = blocks.clone();
            palette.paletteBlockArray = paletteBlockArray.clone();
            palette.blockPaletteMap = blockPaletteMap.clone();
            palette.blockCount = blockCount;
            return palette;
        } catch (CloneNotSupportedException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            throw new IllegalStateException("Weird thing happened");
        }
    }
}
