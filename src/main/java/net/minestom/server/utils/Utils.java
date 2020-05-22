package net.minestom.server.utils;

import io.netty.buffer.ByteBuf;
import net.minestom.server.chat.Chat;
import net.minestom.server.instance.Chunk;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.buffer.BufferWrapper;

import java.util.ArrayList;
import java.util.Map;

public class Utils {

    public static void writeVarIntBuf(ByteBuf buffer, int value) {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            buffer.writeByte(temp);
        } while (value != 0);
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

    public static void writeVarInt(PacketWriter writer, int value) {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            writer.writeByte(temp);
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

    public static int readVarInt(ByteBuf buffer) {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = buffer.readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    public static void writeItemStack(PacketWriter packet, ItemStack itemStack) {
        if (itemStack == null || itemStack.isAir()) {
            packet.writeBoolean(false);
        } else {
            packet.writeBoolean(true);
            packet.writeVarInt(itemStack.getMaterialId());
            packet.writeByte(itemStack.getAmount());

            if (!itemStack.hasNbtTag()) {
                packet.writeByte((byte) 0x00); // No nbt
                return;
            }

            packet.writeByte((byte) 0x0A); // Compound
            packet.writeShort((short) 0); // Empty compound name

            // Unbreakable
            if (itemStack.isUnbreakable()) {
                packet.writeByte((byte) 0x03); // Integer
                packet.writeShortSizedString("Unbreakable");
                packet.writeInt(1);
            }

            // Start damage
            {
                packet.writeByte((byte) 0x02);
                packet.writeShortSizedString("Damage");
                packet.writeShort(itemStack.getDamage());
            }
            // End damage

            // Display
            boolean hasDisplayName = itemStack.hasDisplayName();
            boolean hasLore = itemStack.hasLore();

            if (hasDisplayName || hasLore) {
                packet.writeByte((byte) 0x0A); // Start display compound
                packet.writeShortSizedString("display");

                if (hasDisplayName) {
                    packet.writeByte((byte) 0x08);
                    packet.writeShortSizedString("Name");
                    packet.writeShortSizedString(Chat.toJsonString(Chat.fromLegacyText(itemStack.getDisplayName())));
                }

                if (hasLore) {
                    ArrayList<String> lore = itemStack.getLore();

                    packet.writeByte((byte) 0x09);
                    packet.writeShortSizedString("Lore");
                    packet.writeByte((byte) 0x08);
                    packet.writeInt(lore.size());
                    for (String line : lore) {
                        packet.writeShortSizedString(Chat.toJsonString(Chat.fromLegacyText(line)));
                    }
                }

                packet.writeByte((byte) 0); // End display compound
            }
            // End display

            // Start enchantment
            // FIXME: something is broken, enchants are basically ignored...
            {
                Map<Enchantment, Short> enchantmentMap = itemStack.getEnchantmentMap();
                if (!enchantmentMap.isEmpty()) {
                    packet.writeByte((byte) 0x09); // Type id (list)
                    packet.writeShortSizedString("StoredEnchantments");

                    packet.writeByte((byte) 0x0A); // Compound
                    packet.writeInt(enchantmentMap.size()); // Map size

                    for (Map.Entry<Enchantment, Short> entry : enchantmentMap.entrySet()) {
                        Enchantment enchantment = entry.getKey();
                        short level = entry.getValue();

                        packet.writeByte((byte) 0x02); // Type id (short)
                        packet.writeShortSizedString("lvl");
                        packet.writeShort(level);

                        packet.writeByte((byte) 0x08); // Type id (string)
                        packet.writeShortSizedString("id");
                        packet.writeShortSizedString(enchantment.toMinecraftNamespaceId());

                    }

                    packet.writeByte((byte) 0); // End enchantment compound

                }
            }
            // End enchantment

            // Start hide flags
            /*{
                int hideFlag = itemStack.getHideFlag();
                if (hideFlag != 0) {
                    packet.writeByte((byte) 3); // Type id (int)
                    packet.writeShortSizedString("HideFlags");
                    packet.writeInt(hideFlag);
                }
            }*/

            packet.writeByte((byte) 0); // End nbt
        }
    }

    public static ItemStack readItemStack(PacketReader reader) {
        boolean present = reader.readBoolean();

        if (!present) {
            return ItemStack.getAirItem();
        }

        int id = reader.readVarInt();
        if (id == -1) {
            // Drop mode
            return ItemStack.getAirItem();
        }

        byte count = reader.readByte();

        ItemStack item = new ItemStack((short) id, count);

        byte nbt = reader.readByte(); // Should be compound start (0x0A) or 0 if there isn't NBT data

        if (nbt == 0x00) {
            return item;
        } else if (nbt == 0x0A) {
            reader.readShort(); // Ignored, should be empty (main compound name)
            NbtReaderUtils.readItemStackNBT(reader, item);
        }

        return item;
    }

    public static void writeBlocks(BufferWrapper buffer, short[] blocksId, int bitsPerEntry) {
        short count = 0;
        for (short id : blocksId)
            if (id != 0)
                count++;

        buffer.putShort(count);
        buffer.putByte((byte) bitsPerEntry);
        int[] blocksData = new int[Chunk.CHUNK_SIZE_X * Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SIZE_Z];
        for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
            for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
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
