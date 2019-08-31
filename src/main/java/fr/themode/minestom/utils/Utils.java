package fr.themode.minestom.utils;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.chat.Chat;
import fr.themode.minestom.item.ItemStack;

import java.io.UnsupportedEncodingException;

public class Utils {

    public static void writeString(Buffer buffer, String value) {
        byte[] bytes = new byte[0];
        try {
            bytes = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (bytes.length > 32767) {
            System.out.println("String too big (was " + value.length() + " bytes encoded, max " + 32767 + ")");
        } else {
            writeVarInt(buffer, bytes.length);
            buffer.putBytes(bytes);
        }
    }

    public static String readString(Buffer buffer) {
        int length = readVarInt(buffer);
        byte bytes[] = buffer.getBytes(length);
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeVarInt(Buffer buffer, int value) {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            buffer.putByte(temp);
        } while (value != 0);
    }

    public static int readVarInt(Buffer buffer) {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = buffer.getByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    // ??
    public static int lengthVarInt(int value) {
        int i = 0;
        do {
            i++;
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
        } while (value != 0);
        return i;
    }

    public static int lengthVarLong(long value) {
        int i = 0;
        do {
            i++;
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
        } while (value != 0);
        return i;
    }

    public static void writeVarLong(Buffer buffer, long value) {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            buffer.putByte(temp);
        } while (value != 0);
    }

    public static long readVarLong(Buffer buffer) {
        int numRead = 0;
        long result = 0;
        byte read;
        do {
            read = buffer.getByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 10) {
                throw new RuntimeException("VarLong is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    public static void writePosition(Buffer buffer, int x, int y, int z) {
        buffer.putLong(SerializerUtils.positionToLong(x, y, z));
    }

    public static void writePosition(Buffer buffer, BlockPosition blockPosition) {
        writePosition(buffer, blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }

    public static BlockPosition readPosition(Buffer buffer) {
        return SerializerUtils.longToBlockPosition(buffer.getLong());
    }

    public static void writeItemStack(Buffer buffer, ItemStack itemStack) {
        if (itemStack == null) {
            buffer.putBoolean(false);
        } else {
            buffer.putBoolean(true);
            Utils.writeVarInt(buffer, itemStack.getMaterial().getId());
            buffer.putByte(itemStack.getAmount());

            buffer.putByte((byte) 0x0A); // Compound
            buffer.putShort((short) 0);

            // Unbreakable
            if (itemStack.isUnbreakable()) {
                buffer.putByte((byte) 0x03); // Integer
                buffer.putString("Unbreakable");
                buffer.putInt(1);
            }

            // Display
            buffer.putByte((byte) 0x0A); // Compound
            buffer.putString("display");

            if (itemStack.getDisplayName() != null) {
                buffer.putByte((byte) 0x08);
                buffer.putString("Name");
                buffer.putString(Chat.rawText(itemStack.getDisplayName()));
            }

            // TODO lore
            buffer.putByte((byte) 0x08);
            buffer.putString("Lore");
            buffer.putString(Chat.rawText("a line"));

            buffer.putByte((byte) 0); // End display compound

            buffer.putByte((byte) 0); // End nbt
        }

    }

    public static void writeBlocks(Buffer buffer, short[] blocksId, int bitsPerEntry) {
        short count = 0;
        for (short id : blocksId)
            if (id != 0)
                count++;

        buffer.putShort(count);
        buffer.putByte((byte) bitsPerEntry);
        int[] blocksData = new int[16 * 16 * 16];
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int sectionIndex = (((y * 16) + x) * 16) + z;
                    int index = y << 8 | z << 4 | x;
                    blocksData[index] = blocksId[sectionIndex];
                }
            }
        }
        long[] data = encodeBlocks(blocksData, 14);
        writeVarInt(buffer, data.length);
        for (int i = 0; i < data.length; i++) {
            buffer.putLong(data[i]);
        }
    }

    public static long[] encodeBlocks(int[] blocks, int bitsPerEntry) {
        long maxEntryValue = (1L << bitsPerEntry) - 1;

        int length = (int) Math.ceil(blocks.length * bitsPerEntry / 64.0);
        long[] data = new long[length];

        for (int index = 0; index < blocks.length; index++) {
            int value = blocks[index];
            int bitIndex = index * bitsPerEntry;
            int startIndex = bitIndex / 64;
            int endIndex = ((index + 1) * bitsPerEntry - 1) / 64;
            int startBitSubIndex = bitIndex % 64;
            data[startIndex] = data[startIndex] & ~(maxEntryValue << startBitSubIndex) | ((long) value & maxEntryValue) << startBitSubIndex;
            if (startIndex != endIndex) {
                int endBitSubIndex = 64 - startBitSubIndex;
                data[endIndex] = data[endIndex] >>> endBitSubIndex << endBitSubIndex | ((long) value & maxEntryValue) >> endBitSubIndex;
            }
        }

        return data;
    }

}
