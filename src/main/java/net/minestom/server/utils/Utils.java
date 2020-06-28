package net.minestom.server.utils;

import io.netty.buffer.ByteBuf;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.instance.Chunk;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.potion.PotionType;
import net.minestom.server.utils.buffer.BufferWrapper;
import net.minestom.server.utils.item.NbtReaderUtils;
import net.minestom.server.utils.nbt.NBT;
import net.minestom.server.utils.nbt.NbtWriter;

import java.util.*;

public class Utils {

    public static int getVarIntSize(int input) {
        return (input & 0xFFFFFF80) == 0
                ? 1 : (input & 0xFFFFC000) == 0
                ? 2 : (input & 0xFFE00000) == 0
                ? 3 : (input & 0xF0000000) == 0
                ? 4 : 5;
    }

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

    public static void writeVarLong(PacketWriter writer, long value) {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            writer.writeByte(temp);
        } while (value != 0);
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

            NbtWriter mainWriter = new NbtWriter(packet);

            mainWriter.writeCompound("", writer -> {
                // Unbreakable
                if (itemStack.isUnbreakable()) {
                    writer.writeInt("Unbreakable", 1);
                }

                // Start damage
                {
                    writer.writeShort("Damage", itemStack.getDamage());
                }
                // End damage

                // Display
                boolean hasDisplayName = itemStack.hasDisplayName();
                boolean hasLore = itemStack.hasLore();

                if (hasDisplayName || hasLore) {
                    writer.writeCompound("display", displayWriter -> {
                        if (hasDisplayName) {
                            final String name = itemStack.getDisplayName().toString();
                            displayWriter.writeString("Name", name);
                        }

                        if (hasLore) {
                            final ArrayList<ColoredText> lore = itemStack.getLore();

                            displayWriter.writeList("Lore", NBT.NBT_STRING, lore.size(), () -> {
                                for (ColoredText line : lore) {
                                    packet.writeShortSizedString(line.toString());
                                }
                            });

                        }
                    });
                }
                // End display

                // Start enchantment
                {
                    Map<Enchantment, Short> enchantmentMap = itemStack.getEnchantmentMap();
                    if (!enchantmentMap.isEmpty()) {
                        writeEnchant(writer, "Enchantments", enchantmentMap);
                    }

                    Map<Enchantment, Short> storedEnchantmentMap = itemStack.getStoredEnchantmentMap();
                    if (!storedEnchantmentMap.isEmpty()) {
                        writeEnchant(writer, "StoredEnchantments", storedEnchantmentMap);
                    }
                }
                // End enchantment

                // Start attribute
                {
                    List<ItemAttribute> itemAttributes = itemStack.getAttributes();
                    if (!itemAttributes.isEmpty()) {
                        packet.writeByte((byte) 0x09); // Type id (list)
                        packet.writeShortSizedString("AttributeModifiers");

                        packet.writeByte((byte) 0x0A); // Compound
                        packet.writeInt(itemAttributes.size());

                        for (ItemAttribute itemAttribute : itemAttributes) {
                            UUID uuid = itemAttribute.getUuid();

                            writer.writeLong("UUIDMost", uuid.getMostSignificantBits());

                            writer.writeLong("UUIDLeast", uuid.getLeastSignificantBits());

                            writer.writeDouble("Amount", itemAttribute.getValue());

                            writer.writeString("Slot", itemAttribute.getSlot().name().toLowerCase());

                            writer.writeString("itemAttribute", itemAttribute.getAttribute().getKey());

                            writer.writeInt("Operation", itemAttribute.getOperation().getId());

                            writer.writeString("Name", itemAttribute.getInternalName());
                        }
                        packet.writeByte((byte) 0x00); // End compound
                    }
                }
                // End attribute

                // Start potion
                {
                    Set<PotionType> potionTypes = itemStack.getPotionTypes();
                    if (!potionTypes.isEmpty()) {
                        for (PotionType potionType : potionTypes) {
                            packet.writeByte((byte) 0x08); // type id (string)
                            packet.writeShortSizedString("Potion");
                            packet.writeShortSizedString("minecraft:" + potionType.name().toLowerCase());
                        }
                    }
                }
                // End potion

                // Start hide flags
                {
                    int hideFlag = itemStack.getHideFlag();
                    if (hideFlag != 0) {
                        writer.writeInt("HideFlags", hideFlag);
                    }
                }
            });
        }
    }

    private static void writeEnchant(NbtWriter writer, String listName, Map<Enchantment, Short> enchantmentMap) {
        writer.writeList(listName, NBT.NBT_COMPOUND, enchantmentMap.size(), () -> {
            for (Map.Entry<Enchantment, Short> entry : enchantmentMap.entrySet()) {
                Enchantment enchantment = entry.getKey();
                short level = entry.getValue();

                writer.writeShort("lvl", level);

                writer.writeString("id", "minecraft:" + enchantment.name().toLowerCase());

            }
        });
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

    /*public static long[] encodeBlocks(int[] blocks, int bitsPerEntry) {
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
    }*/

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
            0, 5 };

    public static long[] encodeBlocks(int[] blocks, int bitsPerEntry) {
        long maxEntryValue = (1L << bitsPerEntry) - 1;
        char valuesPerLong = (char) (64 / bitsPerEntry);
        int magicIndex = 3 * (valuesPerLong - 1);
        long divideMul = Integer.toUnsignedLong(MAGIC[magicIndex]);
        long divideAdd = Integer.toUnsignedLong(MAGIC[magicIndex + 1]);
        int divideShift = MAGIC[magicIndex + 2];
        int size = (blocks.length + valuesPerLong - 1) / valuesPerLong;

        long[] data = new long[size];

        for (int i = 0; i < blocks.length; i++) {
            long value = blocks[i];
            int cellIndex = (int) (i * divideMul + divideAdd >> 32L >> divideShift);
            int bitIndex = (i - cellIndex * valuesPerLong) * bitsPerEntry;
            data[cellIndex] = data[cellIndex] & ~(maxEntryValue << bitIndex) | (value & maxEntryValue) << bitIndex;
        }

        return data;
    }
}
