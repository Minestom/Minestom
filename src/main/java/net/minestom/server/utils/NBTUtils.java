package net.minestom.server.utils;

import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.util.Codec;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemMetaBuilder;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

// for lack of a better name
public final class NBTUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(NBTUtils.class);

    /**
     * An Adventure codec to convert between NBT and SNBT.
     *
     * @deprecated Use {@link MinestomAdventure#NBT_CODEC}
     */
    @Deprecated(forRemoval = true)
    public static final Codec<NBT, String, NBTException, RuntimeException> SNBT_CODEC = MinestomAdventure.NBT_CODEC;

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

        return BinaryTagHolder.encode(tag, MinestomAdventure.NBT_CODEC);
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
            Material material = Material.fromNamespaceId(tag.getString("id"));
            if (material == Material.AIR) {
                material = Material.STONE;
            }
            byte count = tag.getByte("Count");
            NBTCompound nbtCompound = null;
            if (tag.containsKey("tag")) {
                nbtCompound = tag.getCompound("tag");
            }
            ItemStack itemStack = ItemStack.fromNBT(material, nbtCompound, count);
            destination.setItemStack(tag.getByte("Slot"), itemStack);
        }
    }

    public static void saveAllItems(@NotNull NBTList<NBTCompound> list, @NotNull Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            final ItemStack stack = inventory.getItemStack(i);
            NBTCompound nbt = new NBTCompound();

            NBTCompound tag = stack.getMeta().toNBT();

            nbt.set("tag", tag);
            nbt.setByte("Slot", (byte) i);
            nbt.setByte("Count", (byte) stack.getAmount());
            nbt.setString("id", stack.getMaterial().name());

            list.add(nbt);
        }
    }

    public static void writeEnchant(@NotNull NBTCompound nbt, @NotNull String listName,
                                    @NotNull Map<Enchantment, Short> enchantmentMap) {
        NBTList<NBTCompound> enchantList = new NBTList<>(NBTTypes.TAG_Compound);
        for (var entry : enchantmentMap.entrySet()) {
            final Enchantment enchantment = entry.getKey();
            final short level = entry.getValue();
            enchantList.add(new NBTCompound()
                    .setShort("lvl", level)
                    .setString("id", enchantment.name())
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

        return ItemStack.fromNBT(material, nbtCompound, count);
    }

    @SuppressWarnings("ConstantConditions")
    public static void loadDataIntoMeta(@NotNull ItemMetaBuilder metaBuilder, @NotNull NBTCompound nbt) {
        if (nbt.containsKey("Damage")) metaBuilder.damage(nbt.getInt("Damage"));
        if (nbt.containsKey("Unbreakable")) metaBuilder.unbreakable(nbt.getAsByte("Unbreakable") == 1);
        if (nbt.containsKey("HideFlags")) metaBuilder.hideFlag(nbt.getInt("HideFlags"));
        if (nbt.containsKey("display")) {
            final NBTCompound display = nbt.getCompound("display");
            if (display.containsKey("Name")) {
                final String rawName = StringUtils.unescapeJavaString(display.getString("Name"));
                final Component displayName = GsonComponentSerializer.gson().deserialize(rawName);
                metaBuilder.displayName(displayName);
            }
            if (display.containsKey("Lore")) {
                NBTList<NBTString> loreList = display.getList("Lore");
                List<Component> lore = new ArrayList<>();
                for (NBTString s : loreList) {
                    final String rawLore = StringUtils.unescapeJavaString(s.getValue());
                    lore.add(GsonComponentSerializer.gson().deserialize(rawLore));
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

        // CanPlaceOn
        {
            if (nbt.containsKey("CanPlaceOn")) {
                NBTList<NBTString> canPlaceOn = nbt.getList("CanPlaceOn");
                Set<Block> blocks = new HashSet<>();
                for (NBTString blockNamespace : canPlaceOn) {
                    Block block = Block.fromNamespaceId(blockNamespace.getValue());
                    blocks.add(block);
                }
                metaBuilder.canPlaceOn(blocks);
            }
        }
        // CanDestroy
        {
            if (nbt.containsKey("CanDestroy")) {
                NBTList<NBTString> canDestroy = nbt.getList("CanDestroy");
                Set<Block> blocks = new HashSet<>();
                for (NBTString blockNamespace : canDestroy) {
                    Block block = Block.fromNamespaceId(blockNamespace.getValue());
                    blocks.add(block);
                }
                metaBuilder.canDestroy(blocks);
            }
        }
    }

    public static void loadEnchantments(NBTList<NBTCompound> enchantments, EnchantmentSetter setter) {
        for (NBTCompound enchantment : enchantments) {
            final short level = enchantment.getAsShort("lvl");
            final String id = enchantment.getString("id");
            final Enchantment enchant = Enchantment.fromNamespaceId(id);
            if (enchant != null) {
                setter.applyEnchantment(enchant, level);
            } else {
                LOGGER.warn("Unknown enchantment type: {}", id);
            }
        }
    }

    @FunctionalInterface
    public interface EnchantmentSetter {
        void applyEnchantment(Enchantment name, short level);
    }
}
