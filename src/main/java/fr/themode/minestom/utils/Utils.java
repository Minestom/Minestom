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
import java.util.ArrayList;
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

    public static void readStringVarIntSized(Client client, StringConsumer consumer) {
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

    public static void readStringShortSized(Client client, StringConsumer consumer) {

        client.readShort(length -> {
            client.readBytes(length, bytes -> {
                try {
                    consumer.accept(new String(bytes, "UTF-8"), length);
                } catch (UnsupportedEncodingException e) {
                    consumer.accept(null, length);
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
        if (itemStack == null || itemStack.isAir()) {
            packet.putBoolean(false);
        } else {
            packet.putBoolean(true);
            Utils.writeVarInt(packet, itemStack.getMaterialId());
            packet.putByte(itemStack.getAmount());

            if (!itemStack.hasNbtTag()) {
                packet.putByte((byte) 0x00); // No nbt
                return;
            }

            packet.putByte((byte) 0x0A); // Compound
            packet.putShort((short) 0); // Empty compound name

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
            boolean hasDisplayName = itemStack.hasDisplayName();
            boolean hasLore = itemStack.hasLore();

            if (hasDisplayName || hasLore) {
                packet.putByte((byte) 0x0A); // Start display compound
                packet.putString("display");

                if (hasDisplayName) {
                    packet.putByte((byte) 0x08);
                    packet.putString("Name");
                    packet.putString(Chat.legacyTextString(itemStack.getDisplayName()));
                }

                if (hasLore) {
                    ArrayList<String> lore = itemStack.getLore();

                    packet.putByte((byte) 0x09);
                    packet.putString("Lore");
                    packet.putByte((byte) 0x08);
                    packet.putInt(lore.size());
                    for (String line : lore) {
                        packet.putString(Chat.legacyTextString(line));
                    }
                }

                packet.putByte((byte) 0); // End display compound
            }
            // End display

            packet.putByte((byte) 0); // End nbt
        }
    }

    public static void readItemStack(PacketReader reader, Consumer<ItemStack> consumer) {
        reader.readBoolean(present -> {
            if (!present) {
                consumer.accept(ItemStack.AIR_ITEM); // Consume air item if empty
                return;
            }

            reader.readVarInt(id -> {

                if (id == -1) {
                    // Drop mode
                    consumer.accept(ItemStack.AIR_ITEM);
                }

                reader.readByte(count -> {
                    ItemStack item = new ItemStack(id, count);
                    reader.readByte(nbt -> { // Should be compound start (0x0A) or 0 if there isn't NBT data
                        if (nbt == 0x00) {
                            consumer.accept(item);
                            return;
                        } else if (nbt == 0x0A) {
                            reader.readShort(compoundName -> { // Ignored, should be empty (main compound name)
                                NbtReaderUtils.readItemStackNBT(reader, consumer, item);
                            });

                        }
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
