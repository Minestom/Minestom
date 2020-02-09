package fr.themode.minestom.utils;

import com.github.simplenet.Client;
import com.github.simplenet.packet.Packet;
import fr.themode.minestom.chat.Chat;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.ConnectionUtils;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.utils.buffer.BufferWrapper;
import fr.themode.minestom.utils.consumer.StringConsumer;

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

    public static void writeVarIntBuffer(BufferWrapper buffer, int value) {
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

            // Damage
            packet.putByte((byte) 0x02);
            packet.putString("Damage");
            packet.putShort(itemStack.getDamage());

            // Display
            packet.putByte((byte) 0x0A); // Compound
            packet.putString("display");

            if (itemStack.getDisplayName() != null) {
                packet.putByte((byte) 0x08);
                packet.putString("Name");
                packet.putString(Chat.legacyTextString(itemStack.getDisplayName()));
            }

            // TODO lore
            /*packet.putByte((byte) 0x08);
            packet.putString("Lore");
            packet.putString(Chat.rawText("a line"));*/

            packet.putByte((byte) 0); // End display compound


            packet.putByte((byte) 0); // End nbt
        }
    }

    public static void readItemStack(PacketReader reader, Consumer<ItemStack> consumer) {
        // FIXME: need finishing
        reader.readBoolean(present -> {
            if (!present) {
                consumer.accept(ItemStack.AIR_ITEM); // Consume air item if empty
                return;
            }

            reader.readVarInt(id -> {

                reader.readByte(count -> {

                    reader.readByte(nbt -> { // FIXME: assume that there is no NBT data
                        consumer.accept(new ItemStack(id, count));
                    });

                });

            });


        });
    }

    public static void writeBlocks(BufferWrapper buffer, short[] blocksId, int bitsPerEntry) {
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
        long[] data = encodeBlocks(blocksData, bitsPerEntry);
        buffer.putVarInt(data.length);
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
