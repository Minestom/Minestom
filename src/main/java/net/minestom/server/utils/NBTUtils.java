package net.minestom.server.utils;

import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.chat.ChatParser;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.data.Data;
import net.minestom.server.data.DataType;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.item.metadata.ItemMeta;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// for lack of a better name
public final class NBTUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(NBTUtils.class);

    private NBTUtils() {

    }

    /**
     * Loads all the items from the 'items' list into the given inventory
     *
     * @param items       the items to save
     * @param destination the inventory destination
     */
    public static void loadAllItems(@NotNull NBTList<NBTCompound> items, @NotNull Inventory destination) {
        destination.clear();
        for (NBTCompound tag : items) {
            Material item = Registries.getMaterial(tag.getString("id"));
            if (item == Material.AIR) {
                item = Material.STONE;
            }
            ItemStack stack = new ItemStack(item, tag.getByte("Count"));
            if (tag.containsKey("tag")) {
                loadDataIntoItem(stack, tag.getCompound("tag"));
            }
            destination.setItemStack(tag.getByte("Slot"), stack);
        }
    }

    public static void saveAllItems(@NotNull NBTList<NBTCompound> list, @NotNull Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            final ItemStack stack = inventory.getItemStack(i);
            NBTCompound nbt = new NBTCompound();

            NBTCompound tag = new NBTCompound();
            saveDataIntoNBT(stack, tag);

            nbt.set("tag", tag);
            nbt.setByte("Slot", (byte) i);
            nbt.setByte("Count", stack.getAmount());
            nbt.setString("id", stack.getMaterial().getName());

            list.add(nbt);
        }
    }

    public static void writeEnchant(@NotNull NBTCompound nbt, @NotNull String listName,
                                    @NotNull Map<Enchantment, Short> enchantmentMap) {
        NBTList<NBTCompound> enchantList = new NBTList<>(NBTTypes.TAG_Compound);
        for (Map.Entry<Enchantment, Short> entry : enchantmentMap.entrySet()) {
            final Enchantment enchantment = entry.getKey();
            final short level = entry.getValue();

            enchantList.add(new NBTCompound()
                    .setShort("lvl", level)
                    .setString("id", "minecraft:" + enchantment.name().toLowerCase())
            );
        }
        nbt.set(listName, enchantList);
    }

    @Nullable
    public static ItemStack readItemStack(@NotNull BinaryReader reader) {
        final boolean present = reader.readBoolean();

        if (!present) {
            return ItemStack.getAirItem();
        }

        final int id = reader.readVarInt();
        if (id == -1) {
            // Drop mode
            return ItemStack.getAirItem();
        }

        final Material material = Material.fromId((short) id);
        final byte count = reader.readByte();
        ItemStack item = new ItemStack(material, count);

        try {
            final NBT itemNBT = reader.readTag();
            if (itemNBT instanceof NBTCompound) { // can also be a TAG_End if no data
                NBTCompound nbt = (NBTCompound) itemNBT;
                loadDataIntoItem(item, nbt);
            }
        } catch (IOException | NBTException e) {
            e.printStackTrace();
        }

        return item;
    }

    public static void loadDataIntoItem(@NotNull ItemStack item, @NotNull NBTCompound nbt) {
        if (nbt.containsKey("Damage")) item.setDamage(nbt.getInt("Damage"));
        if (nbt.containsKey("Unbreakable")) item.setUnbreakable(nbt.getInt("Unbreakable") == 1);
        if (nbt.containsKey("HideFlags")) item.setHideFlag(nbt.getInt("HideFlags"));
        if (nbt.containsKey("display")) {
            final NBTCompound display = nbt.getCompound("display");
            if (display.containsKey("Name")) {
                final String rawName = display.getString("Name");
                final ColoredText displayName = ChatParser.toColoredText(rawName);
                item.setDisplayName(displayName);
            }
            if (display.containsKey("Lore")) {
                NBTList<NBTString> loreList = display.getList("Lore");
                List<JsonMessage> lore = new ArrayList<>();
                for (NBTString s : loreList) {
                    lore.add(ChatParser.toColoredText(s.getValue()));
                }
                item.setLore(lore);
            }
        }

        // Enchantments
        if (nbt.containsKey("Enchantments")) {
            loadEnchantments(nbt.getList("Enchantments"), item::setEnchantment);
        }

        // Attributes
        if (nbt.containsKey("AttributeModifiers")) {
            NBTList<NBTCompound> attributes = nbt.getList("AttributeModifiers");
            for (NBTCompound attributeNBT : attributes) {
                final UUID uuid;
                {
                    final int[] uuidArray = attributeNBT.getIntArray("UUID");
                    uuid = Utils.intArrayToUuid(uuidArray);
                }
                final double value = attributeNBT.getDouble("Amount");
                final String slot = attributeNBT.getString("Slot");
                final String attributeName = attributeNBT.getString("AttributeName");
                final int operation = attributeNBT.getInt("Operation");
                final String name = attributeNBT.getString("Name");

                final Attribute attribute = Attribute.fromKey(attributeName);
                // Wrong attribute name, stop here
                if (attribute == null)
                    break;
                final AttributeOperation attributeOperation = AttributeOperation.fromId(operation);
                // Wrong attribute operation, stop here
                if (attributeOperation == null) {
                    break;
                }
                final AttributeSlot attributeSlot = AttributeSlot.valueOf(slot.toUpperCase());
                // Wrong attribute slot, stop here
                if (attributeSlot == null) {
                    break;
                }

                // Add attribute
                final ItemAttribute itemAttribute =
                        new ItemAttribute(uuid, name, attribute, attributeOperation, value, attributeSlot);
                item.addAttribute(itemAttribute);
            }
        }

        // Hide flags
        {
            if (nbt.containsKey("HideFlags")) {
                item.setHideFlag(nbt.getInt("HideFlags"));
            }
        }

        // Custom model data
        {
            if (nbt.containsKey("CustomModelData")) {
                item.setCustomModelData(nbt.getInt("CustomModelData"));
            }
        }

        // Meta specific field
        final ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.read(nbt);
        }

        // Ownership
        {
            if (nbt.containsKey(ItemStack.OWNERSHIP_DATA_KEY)) {
                final String identifierString = nbt.getString(ItemStack.OWNERSHIP_DATA_KEY);
                final UUID identifier = UUID.fromString(identifierString);
                final Data data = ItemStack.DATA_OWNERSHIP.getOwnObject(identifier);
                if (data != null) {
                    item.setData(data);
                }
            }
        }
    }

    public static void loadEnchantments(NBTList<NBTCompound> enchantments, EnchantmentSetter setter) {
        for (NBTCompound enchantment : enchantments) {
            final short level = enchantment.getAsShort("lvl");
            final String id = enchantment.getString("id");
            final Enchantment enchant = Registries.getEnchantment(id);
            if (enchant != null) {
                setter.applyEnchantment(enchant, level);
            } else {
                LOGGER.warn("Unknown enchantment type: {}", id);
            }
        }
    }

    public static void writeItemStack(BinaryWriter packet, ItemStack itemStack) {
        if (itemStack == null || itemStack.isAir()) {
            packet.writeBoolean(false);
        } else {
            packet.writeBoolean(true);
            packet.writeVarInt(itemStack.getMaterial().getId());
            packet.writeByte(itemStack.getAmount());

            if (!itemStack.hasNbtTag()) {
                packet.writeByte((byte) NBTTypes.TAG_End); // No nbt
                return;
            }

            NBTCompound itemNBT = new NBTCompound();

            // Vanilla compound
            saveDataIntoNBT(itemStack, itemNBT);

            // End custom model data
            packet.writeNBT("", itemNBT);
        }
    }

    public static void saveDataIntoNBT(@NotNull ItemStack itemStack, @NotNull NBTCompound itemNBT) {
        // Unbreakable
        if (itemStack.isUnbreakable()) {
            itemNBT.setInt("Unbreakable", 1);
        }

        // Start damage
        {
            final int damage = itemStack.getDamage();
            if (damage > 0) {
                itemNBT.setInt("Damage", damage);
            }
        }
        // End damage

        // Display
        final boolean hasDisplayName = itemStack.hasDisplayName();
        final boolean hasLore = itemStack.hasLore();

        if (hasDisplayName || hasLore) {
            NBTCompound displayNBT = new NBTCompound();
            if (hasDisplayName) {
                final String name = itemStack.getDisplayName().toString();
                displayNBT.setString("Name", name);
            }

            if (hasLore) {
                final List<JsonMessage> lore = itemStack.getLore();

                final NBTList<NBTString> loreNBT = new NBTList<>(NBTTypes.TAG_String);
                for (JsonMessage line : lore) {
                    loreNBT.add(new NBTString(line.toString()));
                }
                displayNBT.set("Lore", loreNBT);
            }

            itemNBT.set("display", displayNBT);
        }
        // End display

        // Start enchantment
        {
            final Map<Enchantment, Short> enchantmentMap = itemStack.getEnchantmentMap();
            if (!enchantmentMap.isEmpty()) {
                writeEnchant(itemNBT, "Enchantments", enchantmentMap);
            }
        }
        // End enchantment

        // Start attribute
        {
            final List<ItemAttribute> itemAttributes = itemStack.getAttributes();
            if (!itemAttributes.isEmpty()) {
                NBTList<NBTCompound> attributesNBT = new NBTList<>(NBTTypes.TAG_Compound);

                for (ItemAttribute itemAttribute : itemAttributes) {
                    final UUID uuid = itemAttribute.getUuid();
                    attributesNBT.add(
                            new NBTCompound()
                                    .setIntArray("UUID", Utils.uuidToIntArray(uuid))
                                    .setDouble("Amount", itemAttribute.getValue())
                                    .setString("Slot", itemAttribute.getSlot().name().toLowerCase())
                                    .setString("AttributeName", itemAttribute.getAttribute().getKey())
                                    .setInt("Operation", itemAttribute.getOperation().getId())
                                    .setString("Name", itemAttribute.getInternalName())
                    );
                }
                itemNBT.set("AttributeModifiers", attributesNBT);
            }
        }
        // End attribute

        // Start hide flags
        {
            final int hideFlag = itemStack.getHideFlag();
            if (hideFlag != 0) {
                itemNBT.setInt("HideFlags", hideFlag);
            }
        }
        // End hide flags

        // Start custom model data
        {
            final int customModelData = itemStack.getCustomModelData();
            if (customModelData != 0) {
                itemNBT.setInt("CustomModelData", customModelData);
            }
        }
        // End custom model data

        // Start custom meta
        {
            final ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                itemMeta.write(itemNBT);
            }
        }
        // End custom meta

        // Start ownership
        {
            final Data data = itemStack.getData();
            if (data != null && !data.isEmpty()) {
                final UUID identifier = itemStack.getIdentifier();
                itemNBT.setString(ItemStack.OWNERSHIP_DATA_KEY, identifier.toString());
            }
        }
        // End ownership
    }

    /**
     * Converts an object into its {@link NBT} equivalent.
     * <p>
     * If {@code type} is not a primitive type or primitive array and {@code supportDataType} is true,
     * the data will be encoded with the appropriate {@link DataType} into a byte array.
     *
     * @param value           the value to convert
     * @param type            the type of the value, used to know which {@link DataType} to use if {@code value} is not a primitive type
     * @param supportDataType true to allow using a {@link DataType} to encode {@code value} into a byte array if not a primitive type
     * @return the converted value, null if {@code type} is not a primitive type and {@code supportDataType} is false
     */
    @Nullable
    public static NBT toNBT(@NotNull Object value, @NotNull Class type, boolean supportDataType) {
        type = PrimitiveConversion.getObjectClass(type);
        if (type.equals(Boolean.class)) {
            // No boolean type in NBT
            return new NBTByte((byte) (((boolean) value) ? 1 : 0));
        } else if (type.equals(Byte.class)) {
            return new NBTByte((byte) value);
        } else if (type.equals(Character.class)) {
            // No char type in NBT
            return new NBTShort((short) value);
        } else if (type.equals(Short.class)) {
            return new NBTShort((short) value);
        } else if (type.equals(Integer.class)) {
            return new NBTInt((int) value);
        } else if (type.equals(Long.class)) {
            return new NBTLong((long) value);
        } else if (type.equals(Float.class)) {
            return new NBTFloat((float) value);
        } else if (type.equals(Double.class)) {
            return new NBTDouble((double) value);
        } else if (type.equals(String.class)) {
            return new NBTString((String) value);
        } else if (type.equals(Byte[].class)) {
            return new NBTByteArray((byte[]) value);
        } else if (type.equals(Integer[].class)) {
            return new NBTIntArray((int[]) value);
        } else if (type.equals(Long[].class)) {
            return new NBTLongArray((long[]) value);
        } else {
            if (supportDataType) {
                // Custom NBT type, try to encode using the data manager
                DataType dataType = MinecraftServer.getDataManager().getDataType(type);
                Check.notNull(dataType, "The type '" + type + "' is not registered in DataManager and not a primitive type.");

                BinaryWriter writer = new BinaryWriter();
                dataType.encode(writer, value);

                final byte[] encodedValue = writer.toByteArray();

                return new NBTByteArray(encodedValue);
            } else {
                return null;
            }
        }
    }

    /**
     * Converts a nbt object to its raw value.
     * <p>
     * Currently support number, string, byte/int/long array.
     *
     * @param nbt the nbt tag to convert
     * @return the value representation of a tag
     * @throws UnsupportedOperationException if the tag type is not supported
     */
    @NotNull
    public static Object fromNBT(@NotNull NBT nbt) {
        if (nbt instanceof NBTNumber) {
            return ((NBTNumber) nbt).getValue();
        } else if (nbt instanceof NBTString) {
            return ((NBTString) nbt).getValue();
        } else if (nbt instanceof NBTByteArray) {
            return ((NBTByteArray) nbt).getValue();
        } else if (nbt instanceof NBTIntArray) {
            return ((NBTIntArray) nbt).getValue();
        } else if (nbt instanceof NBTLongArray) {
            return ((NBTLongArray) nbt).getValue();
        }

        throw new UnsupportedOperationException("NBT type " + nbt.getClass() + " is not handled properly.");
    }

    @FunctionalInterface
    public interface EnchantmentSetter {
        void applyEnchantment(Enchantment name, short level);
    }
}
