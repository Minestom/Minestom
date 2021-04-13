package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.shorts.Short2ShortLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ShortOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.clone.PublicCloneable;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.instance.Chunk.CHUNK_SECTION_SIZE;

public class Section implements PublicCloneable<Section> {

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

    private long[] blocks;

    // chunk section - palette index = block id
    private Short2ShortLinkedOpenHashMap paletteBlockMap;
    // chunk section - block id = palette index
    private Short2ShortOpenHashMap blockPaletteMap;

    private int bitsPerEntry;
    private final int bitsIncrement;

    private int valuesPerLong;
    private boolean hasPalette;

    protected Section(int bitsPerEntry, int bitsIncrement) {
        this.bitsPerEntry = bitsPerEntry;
        this.bitsIncrement = bitsIncrement;

        this.valuesPerLong = Long.SIZE / bitsPerEntry;
        this.hasPalette = bitsPerEntry <= PALETTE_MAXIMUM_BITS;

        clear();
    }

    public void setBlockAt(int x, int y, int z, short blockId) {
        if (blocks.length == 0) {
            if (blockId == 0) {
                // Section is empty and method is trying to place an air block, stop unnecessary computation
                return;
            }

            // Initialize the section
            blocks = new long[getSize(valuesPerLong)];
        }

        // Change to palette value
        blockId = getPaletteIndex(blockId);

        final int sectionIndex = getSectionIndex(x, y, z);

        final int index = sectionIndex / valuesPerLong;

        final int bitIndex = (sectionIndex % valuesPerLong) * bitsPerEntry;

        long block = blocks[index];
        {
            final long clear = MAGIC_MASKS[bitsPerEntry];

            block |= clear << bitIndex;
            block ^= clear << bitIndex;
            block |= (long) blockId << bitIndex;

            blocks[index] = block;
        }
    }

    public short getBlockAt(int x, int y, int z) {
        if (blocks.length == 0) {
            // Section is not loaded, can only be air
            return Block.AIR.getBlockId();
        }

        final int sectionIdentifier = getSectionIndex(x, y, z);

        final int index = sectionIdentifier / valuesPerLong;
        final int bitIndex = sectionIdentifier % valuesPerLong * bitsPerEntry;

        final long value = blocks[index] >> bitIndex & MAGIC_MASKS[bitsPerEntry];

        // Change to palette value and return
        return hasPalette ?
                paletteBlockMap.get((short) value) :
                (short) value;
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

        Section section = new Section(newBitsPerEntry, bitsIncrement);
        section.paletteBlockMap = paletteBlockMap;
        section.blockPaletteMap = blockPaletteMap;

        for (int y = 0; y < Chunk.CHUNK_SIZE_Y; y++) {
            for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                    final short blockId = getBlockAt(x, y, z);
                    section.setBlockAt(x, y, z, blockId);
                }
            }
        }

        this.bitsPerEntry = section.bitsPerEntry;

        this.valuesPerLong = section.valuesPerLong;
        this.hasPalette = section.hasPalette;

        this.blocks = section.blocks;
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
        this.paletteBlockMap = createPaletteBlockMap();
        this.blockPaletteMap = createBlockPaletteMap();
    }

    public long[] getBlocks() {
        return blocks;
    }

    public Short2ShortLinkedOpenHashMap getPaletteBlockMap() {
        return paletteBlockMap;
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
        if (!hasPalette) {
            return blockId;
        }

        if (!blockPaletteMap.containsKey(blockId)) {
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
     * Gets the maximum number of blocks that the current palette (could be the global one) can take.
     *
     * @return the number of blocks possible in the palette
     */
    private int getMaxPaletteSize() {
        return 1 << bitsPerEntry;
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
     * Gets the array length of one section based on the number of values which can be stored in one long.
     *
     * @param valuesPerLong the number of values per long
     * @return the array length based on {@code valuesPerLong}
     */
    private static int getSize(int valuesPerLong) {
        return (BLOCK_COUNT + valuesPerLong - 1) / valuesPerLong;
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

    @NotNull
    @Override
    public Section clone() {
        try {
            Section section = (Section) super.clone();
            section.blocks = blocks.clone();
            section.paletteBlockMap = paletteBlockMap.clone();
            section.blockPaletteMap = blockPaletteMap.clone();
            return section;
        } catch (CloneNotSupportedException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            throw new IllegalStateException("Weird thing happened");
        }
    }
}
