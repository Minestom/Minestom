package net.minestom.server.utils;

import io.netty.buffer.ByteBuf;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.instance.Chunk;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.potion.PotionType;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.buffer.BufferWrapper;
import org.jglrxavpok.hephaistos.nbt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class Utils {

    private final static Logger LOGGER = LoggerFactory.getLogger(Utils.class);

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

    private static void writeEnchant(NBTCompound nbt, String listName, Map<Enchantment, Short> enchantmentMap) {
        NBTList<NBTCompound> enchantList = new NBTList<>(NBTTypes.TAG_Compound);
        for (Map.Entry<Enchantment, Short> entry : enchantmentMap.entrySet()) {
            Enchantment enchantment = entry.getKey();
            short level = entry.getValue();

            enchantList.add(new NBTCompound()
                    .setShort("lvl", level)
                    .setString("id", "minecraft:" + enchantment.name().toLowerCase())
            );
        }
        nbt.set(listName, enchantList);
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

        try {
            NBT itemNBT = reader.readTag();
            if(itemNBT instanceof NBTCompound) { // can also be a TAG_End if no data
                NBTCompound nbt = (NBTCompound) itemNBT;
                if(nbt.containsKey("Damage")) item.setDamage(nbt.getShort("Damage"));
                if(nbt.containsKey("Unbreakable")) item.setUnbreakable(nbt.getInt("Unbreakable") == 1);
                if(nbt.containsKey("HideFlags")) item.setHideFlag(nbt.getInt("HideFlags"));
                if(nbt.containsKey("Potion")) item.addPotionType(Registries.getPotionType(nbt.getString("Potion")));
                if(nbt.containsKey("display")) {
                    NBTCompound display = nbt.getCompound("display");
                    if(display.containsKey("Name")) item.setDisplayName(ColoredText.of(display.getString("Name")));
                    if(display.containsKey("Lore")) {
                        NBTList<NBTString> loreList = display.getList("Lore");
                        ArrayList<ColoredText> lore = new ArrayList<>();
                        for(NBTString s : loreList) {
                            lore.add(ColoredText.of(s.getValue()));
                        }
                        item.setLore(lore);
                    }
                }

                if(nbt.containsKey("Enchantments")) {
                    loadEnchantments(nbt.getList("Enchantments"), item::setEnchantment);
                }
                if(nbt.containsKey("StoredEnchantments")) {
                    loadEnchantments(nbt.getList("StoredEnchantments"), item::setStoredEnchantment);
                }
                if(nbt.containsKey("AttributeModifiers")) {
                    NBTList<NBTCompound> attributes = nbt.getList("AttributeModifiers");
                    for (NBTCompound attributeNBT : attributes) {
                        // TODO: 1.16 changed how UUIDs are stored, is this part affected?
                        long uuidMost = attributeNBT.getLong("UUIDMost");
                        long uuidLeast = attributeNBT.getLong("UUIDLeast");
                        UUID uuid = new UUID(uuidMost, uuidLeast);
                        double value = attributeNBT.getDouble("Amount");
                        String slot = attributeNBT.getString("Slot");
                        String attributeName = attributeNBT.getString("AttributeName");
                        int operation = attributeNBT.getInt("Operation");
                        String name = attributeNBT.getString("Name");

                        final Attribute attribute = Attribute.fromKey(attributeName);
                        // Wrong attribute name, stop here
                        if (attribute == null)
                            break;
                        final AttributeOperation attributeOperation = AttributeOperation.byId(operation);
                        // Wrong attribute operation, stop here
                        if (attributeOperation == null)
                            break;
                        final AttributeSlot attributeSlot = AttributeSlot.valueOf(slot.toUpperCase());
                        // Wrong attribute slot, stop here
                        if (attributeSlot == null)
                            break;

                        // Add attribute
                        final ItemAttribute itemAttribute =
                                new ItemAttribute(uuid, name, attribute, attributeOperation, value, attributeSlot);
                        item.addAttribute(itemAttribute);
                    }
                }
            }
        } catch (IOException | NBTException e) {
            e.printStackTrace();
        }

        return item;
    }

    @FunctionalInterface
    private interface EnchantmentSetter {
        void applyEnchantment(Enchantment name, short level);
    }

    private static void loadEnchantments(NBTList<NBTCompound> enchantments, EnchantmentSetter setter) {
        for(NBTCompound enchantment : enchantments) {
            short level = enchantment.getShort("lvl");
            String id = enchantment.getString("id");
            Enchantment enchant = Registries.getEnchantment(id);
            if(enchant != null) {
                setter.applyEnchantment(enchant, level);
            } else {
                LOGGER.warn("Unknown enchantment type: "+id);
            }
        }
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

    public static void writeItemStack(PacketWriter packet, ItemStack itemStack) {
        if (itemStack == null || itemStack.isAir()) {
            packet.writeBoolean(false);
        } else {
            packet.writeBoolean(true);
            packet.writeVarInt(itemStack.getMaterialId());
            packet.writeByte(itemStack.getAmount());

            if (!itemStack.hasNbtTag()) {
                packet.writeByte((byte) NBTTypes.TAG_End); // No nbt
                return;
            }

            NBTCompound itemNBT = new NBTCompound();

            // Unbreakable
            if (itemStack.isUnbreakable()) {
                itemNBT.setInt("Unbreakable", 1);
            }

            // Start damage
            {
                itemNBT.setShort("Damage", itemStack.getDamage());
            }
            // End damage

            // Display
            boolean hasDisplayName = itemStack.hasDisplayName();
            boolean hasLore = itemStack.hasLore();

            if (hasDisplayName || hasLore) {
                NBTCompound displayNBT = new NBTCompound();
                if (hasDisplayName) {
                    final String name = itemStack.getDisplayName().toString();
                    displayNBT.setString("Name", name);
                }

                if (hasLore) {
                    final ArrayList<ColoredText> lore = itemStack.getLore();

                    final NBTList<NBTString> loreNBT = new NBTList<>(NBTTypes.TAG_String);
                    for (ColoredText line : lore) {
                        loreNBT.add(new NBTString(line.toString()));
                    }
                    displayNBT.set("Lore", loreNBT);
                }

                itemNBT.set("display", displayNBT);
            }
            // End display

            // Start enchantment
            {
                Map<Enchantment, Short> enchantmentMap = itemStack.getEnchantmentMap();
                if (!enchantmentMap.isEmpty()) {
                    writeEnchant(itemNBT, "Enchantments", enchantmentMap);
                }

                Map<Enchantment, Short> storedEnchantmentMap = itemStack.getStoredEnchantmentMap();
                if (!storedEnchantmentMap.isEmpty()) {
                    writeEnchant(itemNBT, "StoredEnchantments", storedEnchantmentMap);
                }
            }
            // End enchantment

            // Start attribute
            {
                List<ItemAttribute> itemAttributes = itemStack.getAttributes();
                if (!itemAttributes.isEmpty()) {
                    NBTList<NBTCompound> attributesNBT = new NBTList<>(NBTTypes.TAG_Compound);

                    for (ItemAttribute itemAttribute : itemAttributes) {
                        UUID uuid = itemAttribute.getUuid();

                        attributesNBT.add(
                                new NBTCompound()
                                        .setLong("UUIDMost", uuid.getMostSignificantBits())
                                        .setLong("UUIDLeast", uuid.getLeastSignificantBits())
                                        .setDouble("Amount", itemAttribute.getValue())
                                        .setString("Slot", itemAttribute.getSlot().name().toLowerCase())
                                        .setString("itemAttribute", itemAttribute.getAttribute().getKey())
                                        .setInt("Operation", itemAttribute.getOperation().getId())
                                        .setString("Name", itemAttribute.getInternalName())
                        );
                    }
                    itemNBT.set("AttributeModifiers", attributesNBT);
                }
            }
            // End attribute

            // Start potion
            {
                Set<PotionType> potionTypes = itemStack.getPotionTypes();
                if (!potionTypes.isEmpty()) {
                    for (PotionType potionType : potionTypes) {
                        itemNBT.setString("Potion", potionType.getNamespaceID());
                    }
                }
            }
            // End potion

            // Start hide flags
            {
                int hideFlag = itemStack.getHideFlag();
                if (hideFlag != 0) {
                    itemNBT.setInt("HideFlags", hideFlag);
                }
            }
            // End hide flags

            // Start custom model data
            {
                int customModelData = itemStack.getCustomModelData();
                if (customModelData != 0) {
                    itemNBT.setInt("CustomModelData", customModelData);
                }
            }
            // End custom model data
            packet.writeNBT("", itemNBT);
        }
    }

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
