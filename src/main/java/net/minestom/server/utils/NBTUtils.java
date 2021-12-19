package net.minestom.server.utils;

import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.util.Codec;
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
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static void saveAllItems(@NotNull List<NBTCompound> list, @NotNull Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            final ItemStack stack = inventory.getItemStack(i);

            NBTCompound tag = stack.getMeta().toNBT();

            final int slotIndex = i;
            list.add(NBT.Compound(nbt -> {
                nbt.set("tag", tag);
                nbt.setByte("Slot", (byte) slotIndex);
                nbt.setByte("Count", (byte) stack.getAmount());
                nbt.setString("id", stack.getMaterial().name());
            }));
        }
    }

    public static void writeEnchant(@NotNull MutableNBTCompound nbt, @NotNull String listName,
                                    @NotNull Map<Enchantment, Short> enchantmentMap) {
        nbt.set(listName, NBT.List(
                NBTType.TAG_Compound,
                enchantmentMap.entrySet().stream()
                        .map(entry -> NBT.Compound(Map.of(
                                "lvl", NBT.Short(entry.getValue()),
                                "id", NBT.String(entry.getKey().name()))))
                        .toList()
        ));
    }

    public static @NotNull ItemStack readItemStack(@NotNull BinaryReader reader) {
        final boolean present = reader.readBoolean();
        if (!present) return ItemStack.AIR;

        final int id = reader.readVarInt();
        if (id == -1) {
            // Drop mode
            return ItemStack.AIR;
        }

        final Material material = Material.fromId((short) id);
        final byte count = reader.readByte();
        NBTCompound nbtCompound = reader.readTag() instanceof NBTCompound compound ?
                compound : null;
        return ItemStack.fromNBT(material, nbtCompound, count);
    }

    public static void loadDataIntoMeta(@NotNull ItemMetaBuilder metaBuilder, @NotNull NBTCompound nbt) {
        if (nbt.get("Damage") instanceof NBTInt damage) metaBuilder.damage(damage.getValue());
        if (nbt.get("Unbreakable") instanceof NBTByte unbreakable) metaBuilder.unbreakable(unbreakable.asBoolean());
        if (nbt.get("HideFlags") instanceof NBTInt hideFlags) metaBuilder.hideFlag(hideFlags.getValue());
        if (nbt.get("display") instanceof NBTCompound display) {
            if (display.get("Name") instanceof NBTString rawName) {
                final Component displayName = GsonComponentSerializer.gson().deserialize(rawName.getValue());
                metaBuilder.displayName(displayName);
            }
            if (display.get("Lore") instanceof NBTList<?> loreList &&
                    loreList.getSubtagType() == NBTType.TAG_String) {
                List<Component> lore = new ArrayList<>();
                for (NBTString s : loreList.<NBTString>asListOf()) {
                    final String rawLore = s.getValue();
                    lore.add(GsonComponentSerializer.gson().deserialize(rawLore));
                }
                metaBuilder.lore(lore);
            }
        }

        // Enchantments
        if (nbt.get("Enchantments") instanceof NBTList<?> nbtEnchants &&
                nbtEnchants.getSubtagType() == NBTType.TAG_Compound) {
            loadEnchantments(nbtEnchants.asListOf(), metaBuilder::enchantment);
        }

        // Attributes
        if (nbt.get("AttributeModifiers") instanceof NBTList<?> nbtAttributes &&
                nbtAttributes.getSubtagType() == NBTType.TAG_Compound) {
            List<ItemAttribute> attributes = new ArrayList<>();
            for (NBTCompound attributeNBT : nbtAttributes.<NBTCompound>asListOf()) {
                final UUID uuid;
                {
                    final int[] uuidArray = attributeNBT.getIntArray("UUID").copyArray();
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
        if (nbt.get("CustomModelData") instanceof NBTInt customModelData) {
            metaBuilder.customModelData(customModelData.getValue());
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
