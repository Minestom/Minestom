package net.minestom.server.utils;

import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.util.Codec;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.AdventureSerializer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.*;
import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// for lack of a better name
public final class NBTUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(NBTUtils.class);

    /**
     * An Adventure codec to convert between NBT and SNBT.
     */
    public static final Codec<NBT, String, NBTException, RuntimeException> SNBT_CODEC
            = Codec.of(encoded -> new SNBTParser(new StringReader(encoded)).parse(), NBT::toSNBT);

    private NBTUtils() {

    }

    /**
     * Turns an {@link NBTCompound} into an Adventure {@link BinaryTagHolder}.
     *
     * @param tag the tag, if any
     * @return the binary tag holder, or {@code null} if the tag was null
     */
    @Contract("null -> null; !null -> !null")
    public static BinaryTagHolder asBinaryTagHolder(@Nullable NBTCompound tag) {
        if (tag == null) {
            return null;
        }

        return BinaryTagHolder.encode(tag, SNBT_CODEC);
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
            Material material = Registries.getMaterial(tag.getString("id"));
            if (material == Material.AIR) {
                material = Material.STONE;
            }
            byte count = tag.getByte("Count");
            NBTCompound nbtCompound = null;
            if (tag.containsKey("tag")) {
                nbtCompound = tag.getCompound("tag");
            }
            ItemStack itemStack = loadItem(material, count, nbtCompound);
            destination.setItemStack(tag.getByte("Slot"), itemStack);
        }
    }

    public static void saveAllItems(@NotNull NBTList<NBTCompound> list, @NotNull Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            final ItemStack stack = inventory.getItemStack(i);
            NBTCompound nbt = new NBTCompound();

            NBTCompound tag = metaToNBT(stack.getMeta());

            nbt.set("tag", tag);
            nbt.setByte("Slot", (byte) i);
            nbt.setByte("Count", (byte) stack.getAmount());
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

    @NotNull
    public static ItemStack readItemStack(@NotNull BinaryReader reader) {
        final boolean present = reader.readBoolean();

        if (!present) {
            return ItemStack.AIR;
        }

        final int id = reader.readVarInt();
        if (id == -1) {
            // Drop mode
            return ItemStack.AIR;
        }

        final Material material = Material.fromId((short) id);
        final byte count = reader.readByte();
        NBTCompound nbtCompound = null;

        try {
            final NBT itemNBT = reader.readTag();
            if (itemNBT instanceof NBTCompound) { // can also be a TAG_End if no data
                nbtCompound = (NBTCompound) itemNBT;
            }
        } catch (IOException | NBTException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }

        return loadItem(material, count, nbtCompound);
    }

    public static @NotNull ItemStack loadItem(@NotNull Material material, int count, @Nullable NBTCompound nbtCompound) {
        return ItemStack.builder(material)
                .amount(count)
                .meta(metaBuilder -> {
                    if (nbtCompound != null) {
                        return ItemMetaBuilder.fromNBT(metaBuilder, nbtCompound);
                    } else {
                        return metaBuilder;
                    }
                })
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    public static void loadDataIntoMeta(@NotNull ItemMetaBuilder metaBuilder, @NotNull NBTCompound nbt) {
        if (nbt.containsKey("Damage")) metaBuilder.damage(nbt.getInt("Damage"));
        if (nbt.containsKey("Unbreakable")) metaBuilder.unbreakable(nbt.getAsByte("Unbreakable") == 1);
        if (nbt.containsKey("HideFlags")) metaBuilder.hideFlag(nbt.getInt("HideFlags"));
        if (nbt.containsKey("display")) {
            final NBTCompound display = nbt.getCompound("display");
            if (display.containsKey("Name")) {
                final String rawName = display.getString("Name");
                final Component displayName = GsonComponentSerializer.gson().deserialize(rawName);
                metaBuilder.displayName(displayName);
            }
            if (display.containsKey("Lore")) {
                NBTList<NBTString> loreList = display.getList("Lore");
                List<Component> lore = new ArrayList<>();
                for (NBTString s : loreList) {
                    lore.add(GsonComponentSerializer.gson().deserialize(s.getValue()));
                }
                metaBuilder.lore(lore);
            }
        }

        // Enchantments
        if (nbt.containsKey("Enchantments")) {
            loadEnchantments(nbt.getList("Enchantments"), metaBuilder::enchantment);
        }

        // Attributes
        if (nbt.containsKey("AttributeModifiers")) {
            List<ItemAttribute> attributes = new ArrayList<>();
            NBTList<NBTCompound> nbtAttributes = nbt.getList("AttributeModifiers");
            for (NBTCompound attributeNBT : nbtAttributes) {
                final UUID uuid;
                {
                    final int[] uuidArray = attributeNBT.getIntArray("UUID");
                    uuid = Utils.intArrayToUuid(uuidArray);
                }

                final double value = attributeNBT.getAsDouble("Amount");
                final String slot = attributeNBT.containsKey("Slot") ? attributeNBT.getString("Slot") : "MAINHAND";
                final String attributeName = attributeNBT.getString("AttributeName");
                final int operation = attributeNBT.getAsInt("Operation");
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

                // Find slot, default to the main hand if the nbt tag is invalid
                AttributeSlot attributeSlot;
                try {
                    attributeSlot = AttributeSlot.valueOf(slot.toUpperCase());
                } catch (IllegalArgumentException e) {
                    attributeSlot = AttributeSlot.MAINHAND;
                }

                // Add attribute
                final ItemAttribute itemAttribute =
                        new ItemAttribute(uuid, name, attribute, attributeOperation, value, attributeSlot);
                attributes.add(itemAttribute);
            }
            metaBuilder.attributes(attributes);
        }

        // Custom model data
        {
            if (nbt.containsKey("CustomModelData")) {
                metaBuilder.customModelData(nbt.getInt("CustomModelData"));
            }
        }

        // Meta specific fields
        metaBuilder.read(nbt);

        // Ownership
        {
            // FIXME: custom data
            /*if (nbt.containsKey(ItemStack.OWNERSHIP_DATA_KEY)) {
                final String identifierString = nbt.getString(ItemStack.OWNERSHIP_DATA_KEY);
                final UUID identifier = UUID.fromString(identifierString);
                final Data data = ItemStack.DATA_OWNERSHIP.getOwnObject(identifier);
                if (data != null) {
                    item.setData(data);
                }
            }*/
        }

        //CanPlaceOn
        // FIXME: PlaceOn/CanDestroy
        /*{
            if (nbt.containsKey("CanPlaceOn")) {
                NBTList<NBTString> canPlaceOn = nbt.getList("CanPlaceOn");
                canPlaceOn.forEach(x -> item.getCanPlaceOn().add(x.getValue()));
            }
        }
        //CanDestroy
        {
            if (nbt.containsKey("CanDestroy")) {
                NBTList<NBTString> canPlaceOn = nbt.getList("CanDestroy");
                canPlaceOn.forEach(x -> item.getCanDestroy().add(x.getValue()));
            }
        }*/
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

    public static void writeItemStack(@NotNull BinaryWriter packet, @NotNull ItemStack itemStack) {
        if (itemStack.isAir()) {
            packet.writeBoolean(false);
        } else {
            packet.writeBoolean(true);
            packet.writeVarInt(itemStack.getMaterial().getId());
            packet.writeByte((byte) itemStack.getAmount());

            packet.writeNBT("", itemStack.getMeta().toNBT());
        }
    }

    public static @NotNull NBTCompound metaToNBT(@NotNull ItemMeta itemMeta) {
        final NBTCompound itemNBT = new NBTCompound();

        // Unbreakable
        if (itemMeta.isUnbreakable()) {
            itemNBT.setInt("Unbreakable", 1);
        }

        // Damage
        {
            final int damage = itemMeta.getDamage();
            if (damage > 0) {
                itemNBT.setInt("Damage", damage);
            }
        }

        // Start display
        {
            final var displayName = itemMeta.getDisplayName();
            final var lore = itemMeta.getLore();
            final boolean hasDisplayName = displayName != null;
            final boolean hasLore = !lore.isEmpty();
            if (hasDisplayName || hasLore) {
                NBTCompound displayNBT = new NBTCompound();
                if (hasDisplayName) {
                    final String name = AdventureSerializer.serialize(displayName);
                    displayNBT.setString("Name", name);
                }

                if (hasLore) {
                    final NBTList<NBTString> loreNBT = new NBTList<>(NBTTypes.TAG_String);
                    for (Component line : lore) {
                        loreNBT.add(new NBTString(GsonComponentSerializer.gson().serialize(line)));
                    }
                    displayNBT.set("Lore", loreNBT);
                }

                itemNBT.set("display", displayNBT);
            }
        }
        // End display

        // Start enchantment
        {
            final var enchantmentMap = itemMeta.getEnchantmentMap();
            if (!enchantmentMap.isEmpty()) {
                NBTUtils.writeEnchant(itemNBT, "Enchantments", enchantmentMap);
            }
        }
        // End enchantment

        // Start attribute
        {
            final var attributes = itemMeta.getAttributes();
            if (!attributes.isEmpty()) {
                NBTList<NBTCompound> attributesNBT = new NBTList<>(NBTTypes.TAG_Compound);

                for (ItemAttribute itemAttribute : attributes) {
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

        // Start hide flag
        {
            final int hideFlag = itemMeta.getHideFlag();
            if (hideFlag != 0) {
                itemNBT.setInt("HideFlags", hideFlag);
            }
        }
        // End hide flag

        // Start custom model data
        {
            final int customModelData = itemMeta.getCustomModelData();
            if (customModelData != 0) {
                itemNBT.setInt("CustomModelData", customModelData);
            }
        }
        // End custom model data

        return itemNBT;
    }

    @FunctionalInterface
    public interface EnchantmentSetter {
        void applyEnchantment(Enchantment name, short level);
    }
}
