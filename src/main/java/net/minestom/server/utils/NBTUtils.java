package net.minestom.server.utils;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.chat.ChatParser;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.NBTConsumer;
import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.potion.PotionType;
import net.minestom.server.registry.Registries;
import org.jglrxavpok.hephaistos.nbt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

// for lack of a better name
public class NBTUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(NBTUtils.class);

    /**
     * Loads all the items from the 'items' list into the given inventory
     *
     * @param items
     * @param destination
     */
    public static void loadAllItems(NBTList<NBTCompound> items, Inventory destination) {
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

    public static void saveAllItems(NBTList<NBTCompound> list, Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItemStack(i);
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
            if (itemNBT instanceof NBTCompound) { // can also be a TAG_End if no data
                NBTCompound nbt = (NBTCompound) itemNBT;
                loadDataIntoItem(item, nbt);
            }
        } catch (IOException | NBTException e) {
            e.printStackTrace();
        }

        return item;
    }

    public static void loadDataIntoItem(ItemStack item, NBTCompound nbt) {
        if (nbt.containsKey("Damage")) item.setDamage(nbt.getInt("Damage"));
        if (nbt.containsKey("Unbreakable")) item.setUnbreakable(nbt.getInt("Unbreakable") == 1);
        if (nbt.containsKey("HideFlags")) item.setHideFlag(nbt.getInt("HideFlags"));
        if (nbt.containsKey("Potion")) item.addPotionType(Registries.getPotionType(nbt.getString("Potion")));
        if (nbt.containsKey("display")) {
            NBTCompound display = nbt.getCompound("display");
            if (display.containsKey("Name")) item.setDisplayName(ChatParser.toColoredText(display.getString("Name")));
            if (display.containsKey("Lore")) {
                NBTList<NBTString> loreList = display.getList("Lore");
                ArrayList<ColoredText> lore = new ArrayList<>();
                for (NBTString s : loreList) {
                    lore.add(ChatParser.toColoredText(s.getValue()));
                }
                item.setLore(lore);
            }
        }

        if (nbt.containsKey("Enchantments")) {
            loadEnchantments(nbt.getList("Enchantments"), item::setEnchantment);
        }
        if (nbt.containsKey("StoredEnchantments")) {
            loadEnchantments(nbt.getList("StoredEnchantments"), item::setStoredEnchantment);
        }
        if (nbt.containsKey("AttributeModifiers")) {
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

    private static void loadEnchantments(NBTList<NBTCompound> enchantments, EnchantmentSetter setter) {
        for (NBTCompound enchantment : enchantments) {
            short level = enchantment.getShort("lvl");
            String id = enchantment.getString("id");
            Enchantment enchant = Registries.getEnchantment(id);
            if (enchant != null) {
                setter.applyEnchantment(enchant, level);
            } else {
                LOGGER.warn("Unknown enchantment type: " + id);
            }
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

            // Vanilla compound
            saveDataIntoNBT(itemStack, itemNBT);

            // Custom item nbt
            NBTConsumer nbtConsumer = itemStack.getNBTConsumer();
            if (nbtConsumer != null) {
                nbtConsumer.accept(itemNBT);
            }

            // End custom model data
            packet.writeNBT("", itemNBT);
        }
    }

    public static void saveDataIntoNBT(ItemStack itemStack, NBTCompound itemNBT) {
        // Unbreakable
        if (itemStack.isUnbreakable()) {
            itemNBT.setInt("Unbreakable", 1);
        }

        // Start damage
        {
            itemNBT.setInt("Damage", itemStack.getDamage());
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
    }

    @FunctionalInterface
    private interface EnchantmentSetter {
        void applyEnchantment(Enchantment name, short level);
    }
}
