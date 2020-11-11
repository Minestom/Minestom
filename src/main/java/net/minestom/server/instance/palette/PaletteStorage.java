package net.minestom.server.instance.palette;

import static net.minestom.server.instance.Chunk.*;

public class PaletteStorage {

    private int bitsPerEntry;
    private int valuesPerLong;

    protected long[][] sectionBlocks = new long[CHUNK_SECTION_COUNT][0];

    public PaletteStorage(int bitsPerEntry) {
        this.bitsPerEntry = bitsPerEntry;
        this.valuesPerLong = Long.SIZE / bitsPerEntry;
    }

    public void setBlockAt(int x, int y, int z, short blockId) {
        x %= 16;
        if (x < 0) {
            x = CHUNK_SIZE_X + x;
        }
        z %= 16;
        if (z < 0) {
            z = CHUNK_SIZE_Z + z;
        }

        int sectionY = y % CHUNK_SECTION_SIZE;
        int sectionIndex = (((sectionY * 16) + z) * 16) + x;

        final int index = sectionIndex / valuesPerLong;
        final int bitIndex = (sectionIndex % valuesPerLong) * bitsPerEntry;

        final int section = y / CHUNK_SECTION_SIZE;

        if (sectionBlocks[section].length == 0) {
            sectionBlocks[section] = new long[getSize()];
        }

        long[] sectionBlock = sectionBlocks[section];

        long block = sectionBlock[index];
        {
            if (blockId != 0) {
                long shiftCount = (long) bitIndex;
                long cacheMask = (1L << shiftCount) - 1L;
                long cache = block & cacheMask;

                /*System.out.println("blockId "+blockId);
                System.out.println("bitIndex "+bitIndex);
                System.out.println("block "+binary(block));
                System.out.println("mask "+binary(cacheMask));
                System.out.println("cache "+binary(cache));*/

                block = block >> shiftCount << shiftCount;
                //System.out.println("block "+binary(block));
                block = block | (long) blockId;
                //System.out.println("block2 "+binary(block));
                block = (block << shiftCount);
                //System.out.println("block3 "+binary(block));
                block = block | cache;
                //System.out.println("block4 "+binary(block));

                sectionBlock[index] = block;
            }

        }
    }

    public short getBlockAt(int x, int y, int z) {
        x %= 16;
        if (x < 0) {
            x = CHUNK_SIZE_X + x;
        }
        z %= 16;
        if (z < 0) {
            z = CHUNK_SIZE_Z + z;
        }

        int sectionY = y % CHUNK_SECTION_SIZE;
        int sectionIndex = (((sectionY * 16) + z) * 16) + x;

        final int index = sectionIndex / valuesPerLong;
        final int bitIndex = sectionIndex % valuesPerLong * bitsPerEntry;

        final int section = y / CHUNK_SECTION_SIZE;

        long[] blocks = sectionBlocks[section];

        if (blocks.length == 0) {
            return 0;
        }

        long value = blocks[index] >> bitIndex;
        long mask = (1L << ((long) bitIndex + (long) bitsPerEntry)) - 1L;

        long finalValue;

        System.out.println("index " + index);
        System.out.println("bitIndex " + bitIndex);
        System.out.println("test1 " + binary(value));
        System.out.println("test2 " + binary(mask));

        {
            mask = mask >> bitIndex << bitIndex;
            System.out.println("test3 " + binary(mask));
            finalValue = value & mask >> bitIndex;
        }

        System.out.println("final " + binary(finalValue));


        /*System.out.println("data " + index + " " + bitIndex + " " + sectionIndex);
        System.out.println("POS " + x + " " + y + " " + z);
        System.out.println("mask " + binary(mask));
        System.out.println("bin " + binary(blocks[index]));
        System.out.println("result " + ((blocks[index] >> bitIndex) & mask));*/
        return (short) (finalValue);
    }

    private int getSize() {
        final int blockCount = 16 * 16 * 16; // A whole chunk section
        final int arraySize = (blockCount + valuesPerLong - 1) / valuesPerLong;
        //System.out.println("size " + arraySize);
        return arraySize;
    }

    public int getBitsPerEntry() {
        return bitsPerEntry;
    }

    public long[][] getSectionBlocks() {
        return sectionBlocks;
    }

    public PaletteStorage copy() {
        PaletteStorage paletteStorage = new PaletteStorage(bitsPerEntry);
        paletteStorage.sectionBlocks = sectionBlocks.clone();

        return paletteStorage;
    }

    private static String binary(long value) {
        return "0b" + Long.toBinaryString(value);
    }

}
