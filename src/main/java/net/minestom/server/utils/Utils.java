package net.minestom.server.utils;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.shorts.Short2ShortLinkedOpenHashMap;
import net.minestom.server.instance.palette.Section;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class Utils {

    private Utils() {
    }

    public static int getVarIntSize(int input) {
        return (input & 0xFFFFFF80) == 0
                ? 1 : (input & 0xFFFFC000) == 0
                ? 2 : (input & 0xFFE00000) == 0
                ? 3 : (input & 0xF0000000) == 0
                ? 4 : 5;
    }

    public static void writeVarInt(@NotNull ByteBuf buf, int value) {
        // Took from velocity
        if ((value & (0xFFFFFFFF << 7)) == 0) {
            buf.writeByte(value);
        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            int w = (value & 0x7F | 0x80) << 8 | (value >>> 7);
            buf.writeShort(w);
        } else if ((value & (0xFFFFFFFF << 21)) == 0) {
            int w = (value & 0x7F | 0x80) << 16 | ((value >>> 7) & 0x7F | 0x80) << 8 | (value >>> 14);
            buf.writeMedium(w);
        } else {
            int w = (value & 0x7F | 0x80) << 24 | ((value >>> 7) & 0x7F | 0x80) << 16
                    | ((value >>> 14) & 0x7F | 0x80) << 8 | ((value >>> 21) & 0x7F | 0x80);
            buf.writeInt(w);
            buf.writeByte(value >>> 28);
        }
    }

    public static void write3BytesVarInt(@NotNull ByteBuf buffer, int startIndex, int value) {
        final int indexCache = buffer.writerIndex();
        buffer.writerIndex(startIndex);
        final int w = (value & 0x7F | 0x80) << 16 | ((value >>> 7) & 0x7F | 0x80) << 8 | (value >>> 14);
        buffer.writeMedium(w);
        buffer.writerIndex(indexCache);
    }

    public static int writeEmpty3BytesVarInt(@NotNull ByteBuf buffer) {
        final int index = buffer.writerIndex();
        buffer.writeMedium(0);
        return index;
    }

    public static int readVarInt(ByteBuf buf) {
        int i = 0;
        final int maxRead = Math.min(5, buf.readableBytes());
        for (int j = 0; j < maxRead; j++) {
            final int k = buf.readByte();
            i |= (k & 0x7F) << j * 7;
            if ((k & 0x80) != 128) {
                return i;
            }
        }
        throw new RuntimeException("VarInt is too big");
    }

    public static long readVarLong(@NotNull ByteBuf buffer) {
        int numRead = 0;
        long result = 0;
        byte read;
        do {
            read = buffer.readByte();
            long value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 10) {
                throw new RuntimeException("VarLong is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    public static void writeVarLong(BinaryWriter writer, long value) {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            writer.writeByte(temp);
        } while (value != 0);
    }

    public static int[] uuidToIntArray(UUID uuid) {
        int[] array = new int[4];

        final long uuidMost = uuid.getMostSignificantBits();
        final long uuidLeast = uuid.getLeastSignificantBits();

        array[0] = (int) (uuidMost >> 32);
        array[1] = (int) uuidMost;

        array[2] = (int) (uuidLeast >> 32);
        array[3] = (int) uuidLeast;

        return array;
    }

    public static UUID intArrayToUuid(int[] array) {
        final long uuidMost = (long) array[0] << 32 | array[1] & 0xFFFFFFFFL;
        final long uuidLeast = (long) array[2] << 32 | array[3] & 0xFFFFFFFFL;

        return new UUID(uuidMost, uuidLeast);
    }

    public static void writeSectionBlocks(ByteBuf buffer, Section section) {
        /*short count = 0;
        for (short id : blocksId)
            if (id != 0)
                count++;*/

        final int bitsPerEntry = section.getBitsPerEntry();

        //buffer.writeShort(count);
        // TODO count blocks
        buffer.writeShort(200);
        buffer.writeByte((byte) bitsPerEntry);

        // Palette
        if (bitsPerEntry < 9) {
            // Palette has to exist
            final Short2ShortLinkedOpenHashMap paletteBlockMap = section.getPaletteBlockMap();
            writeVarInt(buffer, paletteBlockMap.size());
            for (short paletteValue : paletteBlockMap.values()) {
                writeVarInt(buffer, paletteValue);
            }
        }

        final long[] blocks = section.getBlocks();
        writeVarInt(buffer, blocks.length);
        for (long datum : blocks) {
            buffer.writeLong(datum);
        }
    }

    private static final int[] MAGIC = {
            -1, -1, 0, Integer.MIN_VALUE, 0, 0, 1431655765, 1431655765, 0, Integer.MIN_VALUE,
            0, 1, 858993459, 858993459, 0, 715827882, 715827882, 0, 613566756, 613566756,
            0, Integer.MIN_VALUE, 0, 2, 477218588, 477218588, 0, 429496729, 429496729, 0,
            390451572, 390451572, 0, 357913941, 357913941, 0, 330382099, 330382099, 0, 306783378,
            306783378, 0, 286331153, 286331153, 0, Integer.MIN_VALUE, 0, 3, 252645135, 252645135,
            0, 238609294, 238609294, 0, 226050910, 226050910, 0, 214748364, 214748364, 0,
            204522252, 204522252, 0, 195225786, 195225786, 0, 186737708, 186737708, 0, 178956970,
            178956970, 0, 171798691, 171798691, 0, 165191049, 165191049, 0, 159072862, 159072862,
            0, 153391689, 153391689, 0, 148102320, 148102320, 0, 143165576, 143165576, 0,
            138547332, 138547332, 0, Integer.MIN_VALUE, 0, 4, 130150524, 130150524, 0, 126322567,
            126322567, 0, 122713351, 122713351, 0, 119304647, 119304647, 0, 116080197, 116080197,
            0, 113025455, 113025455, 0, 110127366, 110127366, 0, 107374182, 107374182, 0,
            104755299, 104755299, 0, 102261126, 102261126, 0, 99882960, 99882960, 0, 97612893,
            97612893, 0, 95443717, 95443717, 0, 93368854, 93368854, 0, 91382282, 91382282,
            0, 89478485, 89478485, 0, 87652393, 87652393, 0, 85899345, 85899345, 0,
            84215045, 84215045, 0, 82595524, 82595524, 0, 81037118, 81037118, 0, 79536431,
            79536431, 0, 78090314, 78090314, 0, 76695844, 76695844, 0, 75350303, 75350303,
            0, 74051160, 74051160, 0, 72796055, 72796055, 0, 71582788, 71582788, 0,
            70409299, 70409299, 0, 69273666, 69273666, 0, 68174084, 68174084, 0, Integer.MIN_VALUE,
            0, 5};

    public static long[] encodeBlocks(int[] blocks, int bitsPerEntry) {
        final long maxEntryValue = (1L << bitsPerEntry) - 1;
        final char valuesPerLong = (char) (64 / bitsPerEntry);
        final int magicIndex = 3 * (valuesPerLong - 1);
        final long divideMul = Integer.toUnsignedLong(MAGIC[magicIndex]);
        final long divideAdd = Integer.toUnsignedLong(MAGIC[magicIndex + 1]);
        final int divideShift = MAGIC[magicIndex + 2];
        final int size = (blocks.length + valuesPerLong - 1) / valuesPerLong;

        long[] data = new long[size];

        for (int i = 0; i < blocks.length; i++) {
            final long value = blocks[i];
            final int cellIndex = (int) (i * divideMul + divideAdd >> 32L >> divideShift);
            final int bitIndex = (i - cellIndex * valuesPerLong) * bitsPerEntry;
            data[cellIndex] = data[cellIndex] & ~(maxEntryValue << bitIndex) | (value & maxEntryValue) << bitIndex;
        }

        return data;
    }

}
