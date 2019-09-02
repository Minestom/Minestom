package fr.themode.minestom.utils;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.chat.Chat;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.ConnectionUtils;
import fr.themode.minestom.utils.consumer.StringConsumer;
import simplenet.Client;
import simplenet.packet.Packet;

import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;

public class Utils {

    public static void writeString(Packet packet, String value) {
        byte[] bytes = new byte[0];
        try {
            bytes = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (bytes.length > 32767) {
            System.out.println("String too big (was " + value.length() + " bytes encoded, max " + 32767 + ")");
        } else {
            writeVarInt(packet, bytes.length);
            packet.putBytes(bytes);
        }
    }

    public static void readString(Client client, StringConsumer consumer) {
        ConnectionUtils.readVarInt(client, length -> {
            int stringLength = Utils.lengthVarInt(length) + length;
            client.readBytes(length, bytes -> {
                try {
                    consumer.accept(new String(bytes, "UTF-8"), stringLength);
                } catch (UnsupportedEncodingException e) {
                    consumer.accept(null, stringLength);
                    e.printStackTrace();
                }
            });
        });
    }

    public static void writeVarIntBuffer(Buffer buffer, int value) {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            buffer.putByte(temp);
        } while (value != 0);
    }

    public static void writeVarInt(Packet packet, int value) {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            packet.putByte(temp);
        } while (value != 0);
    }

    public static int readVarInt(Client client) {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = client.readByte();
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

    public static void writeVarLong(Packet packet, long value) {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            packet.putByte(temp);
        } while (value != 0);
    }

    public static long readVarLong(Client client) {
        int numRead = 0;
        long result = 0;
        byte read;
        do {
            read = client.readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 10) {
                throw new RuntimeException("VarLong is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    public static void writePosition(Packet packet, int x, int y, int z) {
        packet.putLong(SerializerUtils.positionToLong(x, y, z));
    }

    public static void writePosition(Packet packet, BlockPosition blockPosition) {
        writePosition(packet, blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }

    public static void readPosition(Client client, Consumer<BlockPosition> consumer) {
        client.readLong(value -> {
            consumer.accept(SerializerUtils.longToBlockPosition(value));
        });
    }

    public static void writeItemStack(Packet packet, ItemStack itemStack) {
        if (itemStack == null) {
            packet.putBoolean(false);
        } else {
            packet.putBoolean(true);
            Utils.writeVarInt(packet, itemStack.getMaterial().getId());
            packet.putByte(itemStack.getAmount());

            packet.putByte((byte) 0x0A); // Compound
            packet.putShort((short) 0);

            // Unbreakable
            if (itemStack.isUnbreakable()) {
                packet.putByte((byte) 0x03); // Integer
                packet.putString("Unbreakable");
                packet.putInt(1);
            }

            // Display
            packet.putByte((byte) 0x0A); // Compound
            packet.putString("display");

            if (itemStack.getDisplayName() != null) {
                packet.putByte((byte) 0x08);
                packet.putString("Name");
                packet.putString(Chat.rawText(itemStack.getDisplayName()));
            }

            // TODO lore
            packet.putByte((byte) 0x08);
            packet.putString("Lore");
            packet.putString(Chat.rawText("a line"));

            packet.putByte((byte) 0); // End display compound

            packet.putByte((byte) 0); // End nbt
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
        writeVarIntBuffer(buffer, data.length);
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
