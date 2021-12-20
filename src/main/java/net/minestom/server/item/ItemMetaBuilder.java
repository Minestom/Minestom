package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.attribute.AttributeSlot;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagWritable;
import net.minestom.server.utils.NBTUtils;
import net.minestom.server.utils.Utils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.*;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ItemMetaBuilder implements TagWritable {
    MutableNBTCompound nbt = new MutableNBTCompound();

    protected int damage;
    protected boolean unbreakable;
    protected int hideFlag;
    protected Component displayName;
    protected List<Component> lore = new ArrayList<>();
    protected Map<Enchantment, Short> enchantmentMap = new HashMap<>();
    protected List<ItemAttribute> attributes = new ArrayList<>();
    protected int customModelData;
    protected Set<Block> canDestroy = new HashSet<>();
    protected Set<Block> canPlaceOn = new HashSet<>();

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder damage(int damage) {
        this.damage = damage;
        mutateNbt(compound -> compound.setInt("Damage", damage));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        mutateNbt(compound -> compound.set("Unbreakable", NBT.Boolean(unbreakable)));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder hideFlag(int hideFlag) {
        this.hideFlag = hideFlag;
        mutateNbt(compound -> compound.setInt("HideFlags", hideFlag));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder hideFlag(@NotNull ItemHideFlag... hideFlags) {
        int result = 0;
        for (ItemHideFlag hideFlag : hideFlags) {
            result |= hideFlag.getBitFieldPart();
        }
        return hideFlag(result);
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder displayName(@Nullable Component displayName) {
        this.displayName = displayName;
        handleCompound("display", nbtCompound -> {
            if (displayName != null) {
                final String name = GsonComponentSerializer.gson().serialize(displayName);
                nbtCompound.setString("Name", name);
            } else {
                nbtCompound.remove("Name");
            }
        });
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder lore(@NotNull List<@NotNull Component> lore) {
        this.lore = new ArrayList<>(lore);
        handleCompound("display", nbtCompound -> {
            final NBTList<NBTString> loreNBT = NBT.List(NBTType.TAG_String,
                    lore.stream()
                            .map(line -> new NBTString(GsonComponentSerializer.gson().serialize(line)))
                            .toList()
            );
            nbtCompound.set("Lore", loreNBT);
        });
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder lore(Component... lore) {
        lore(Arrays.asList(lore));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder enchantments(@NotNull Map<Enchantment, Short> enchantments) {
        this.enchantmentMap = new HashMap<>(enchantments);
        handleMap(enchantmentMap, "Enchantments",
                (nbt) -> NBTUtils.writeEnchant(nbt, "Enchantments", enchantmentMap));
        return this;
    }

    @Contract("_, _ -> this")
    public @NotNull ItemMetaBuilder enchantment(@NotNull Enchantment enchantment, short level) {
        this.enchantmentMap.put(enchantment, level);
        enchantments(enchantmentMap);
        return this;
    }

    @Contract("-> this")
    public @NotNull ItemMetaBuilder clearEnchantment() {
        this.enchantmentMap = new HashMap<>();
        enchantments(enchantmentMap);
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder attributes(@NotNull List<@NotNull ItemAttribute> attributes) {
        this.attributes = new ArrayList<>(attributes);

        handleCollection(attributes, "AttributeModifiers", () -> NBT.List(
                NBTType.TAG_Compound,
                attributes.stream()
                        .map(itemAttribute -> NBT.Compound(Map.of(
                                "UUID", NBT.IntArray(Utils.uuidToIntArray(itemAttribute.getUuid())),
                                "Amount", NBT.Double(itemAttribute.getValue()),
                                "Slot", NBT.String(itemAttribute.getSlot().name().toLowerCase()),
                                "AttributeName", NBT.String(itemAttribute.getAttribute().getKey()),
                                "Operation", NBT.Int(itemAttribute.getOperation().getId()),
                                "Name", NBT.String(itemAttribute.getInternalName()))))
                        .toList()
        ));

        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder customModelData(int customModelData) {
        this.customModelData = customModelData;
        mutateNbt(compound -> compound.setInt("CustomModelData", customModelData));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder canPlaceOn(@NotNull Set<@NotNull Block> blocks) {
        this.canPlaceOn = new HashSet<>(blocks);
        handleCollection(canPlaceOn, "CanPlaceOn", () -> NBT.List(
                NBTType.TAG_String,
                canPlaceOn.stream()
                        .map(block -> new NBTString(block.name()))
                        .toList()
        ));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder canPlaceOn(@NotNull Block... blocks) {
        return canPlaceOn(Set.of(blocks));
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder canDestroy(@NotNull Set<@NotNull Block> blocks) {
        this.canDestroy = new HashSet<>(blocks);
        handleCollection(canDestroy, "CanDestroy", () -> NBT.List(
                NBTType.TAG_String,
                canDestroy.stream()
                        .map(block -> new NBTString(block.name()))
                        .toList()
        ));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder canDestroy(@NotNull Block... blocks) {
        return canDestroy(Set.of(blocks));
    }

    @Override
    public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        mutateNbt(compound -> tag.write(compound, value));
    }

    public <T> @NotNull ItemMetaBuilder set(@NotNull Tag<T> tag, @Nullable T value) {
        setTag(tag, value);
        return this;
    }

    @Contract("-> new")
    public abstract @NotNull ItemMeta build();

    public abstract void read(@NotNull NBTCompound nbtCompound);

    protected abstract @NotNull Supplier<@NotNull ItemMetaBuilder> getSupplier();

    protected void mutateNbt(Consumer<MutableNBTCompound> consumer) {
        consumer.accept(nbt);
    }

    protected @NotNull ItemMeta generate() {
        return build();
    }

    protected void handleCompound(@NotNull String key,
                                  @NotNull Consumer<@NotNull MutableNBTCompound> consumer) {
        mutateNbt(nbt -> {
            MutableNBTCompound compoundToModify = nbt.get(key) instanceof NBTCompound compound ?
                    compound.toMutableCompound() : new MutableNBTCompound();

            consumer.accept(compoundToModify);
            if (compoundToModify.isEmpty()) {
                nbt.remove(key);
            } else {
                nbt.set(key, compoundToModify.toCompound());
            }
        });
    }

    protected void handleNullable(@Nullable Object value,
                                  @NotNull String key,
                                  @NotNull Supplier<@NotNull NBT> supplier) {
        mutateNbt(compound -> {
            if (value != null) {
                compound.set(key, supplier.get());
            } else {
                compound.remove(key);
            }
        });
    }

    protected void handleCollection(@NotNull Collection<?> objects,
                                    @NotNull String key,
                                    @NotNull Supplier<@NotNull NBT> supplier) {
        mutateNbt(compound -> {
            if (!objects.isEmpty()) {
                compound.set(key, supplier.get());
            } else {
                compound.remove(key);
            }
        });
    }

    protected void handleMap(@NotNull Map<?, ?> objects,
                             @NotNull String key,
                             @NotNull Consumer<MutableNBTCompound> consumer) {
        mutateNbt(compound -> {
            if (!objects.isEmpty()) {
                consumer.accept(compound);
            } else {
                compound.remove(key);
            }
        });
    }

    @ApiStatus.Internal
    public static void resetMeta(@NotNull ItemMetaBuilder src, @NotNull NBTCompound nbtCompound) {
        src.nbt = nbtCompound.toMutableCompound();
        appendMeta(src, nbtCompound);
    }

    private static void appendMeta(@NotNull ItemMetaBuilder metaBuilder,
                                   @NotNull NBTCompound nbt) {
        if (nbt.get("Damage") instanceof NBTInt damage) metaBuilder.damage = damage.getValue();
        if (nbt.get("Unbreakable") instanceof NBTByte unbreakable) metaBuilder.unbreakable = unbreakable.asBoolean();
        if (nbt.get("HideFlags") instanceof NBTInt hideFlags) metaBuilder.hideFlag = hideFlags.getValue();
        if (nbt.get("display") instanceof NBTCompound display) {
            if (display.get("Name") instanceof NBTString rawName) {
                metaBuilder.displayName = GsonComponentSerializer.gson().deserialize(rawName.getValue());
            }
            if (display.get("Lore") instanceof NBTList<?> loreList &&
                    loreList.getSubtagType() == NBTType.TAG_String) {
                for (NBTString rawLore : loreList.<NBTString>asListOf()) {
                    metaBuilder.lore.add(GsonComponentSerializer.gson().deserialize(rawLore.getValue()));
                }
            }
        }
        // Enchantments
        if (nbt.get("Enchantments") instanceof NBTList<?> nbtEnchants &&
                nbtEnchants.getSubtagType() == NBTType.TAG_Compound) {
            NBTUtils.loadEnchantments(nbtEnchants.asListOf(),
                    (enchantment, level) -> metaBuilder.enchantmentMap.put(enchantment, level));
        }
        // Attributes
        if (nbt.get("AttributeModifiers") instanceof NBTList<?> nbtAttributes &&
                nbtAttributes.getSubtagType() == NBTType.TAG_Compound) {
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
                metaBuilder.attributes.add(itemAttribute);
            }
        }
        // Custom model data
        if (nbt.get("CustomModelData") instanceof NBTInt customModelData) {
            metaBuilder.customModelData = customModelData.getValue();
        }
        // Meta specific fields
        metaBuilder.read(nbt);
        // CanPlaceOn
        if (nbt.get("CanPlaceOn") instanceof NBTList<?> canPlaceOn &&
                canPlaceOn.getSubtagType() == NBTType.TAG_String) {
            for (NBTString blockNamespace : canPlaceOn.<NBTString>asListOf()) {
                Block block = Block.fromNamespaceId(blockNamespace.getValue());
                metaBuilder.canPlaceOn.add(block);
            }
        }
        // CanDestroy
        if (nbt.get("CanDestroy") instanceof NBTList<?> canDestroy &&
                canDestroy.getSubtagType() == NBTType.TAG_String) {
            for (NBTString blockNamespace : canDestroy.<NBTString>asListOf()) {
                Block block = Block.fromNamespaceId(blockNamespace.getValue());
                metaBuilder.canDestroy.add(block);
            }
        }
    }

    public interface Provider<T extends ItemMetaBuilder> {
    }
}
