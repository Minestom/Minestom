package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.adventure.AdventureSerializer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.utils.NBTUtils;
import net.minestom.server.utils.Utils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ItemMetaBuilder {

    protected NBTCompound nbt = new NBTCompound();

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
        this.nbt.setInt("Damage", damage);
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        this.nbt.setByte("Unbreakable", (byte) (unbreakable ? 1 : 0));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder hideFlag(int hideFlag) {
        this.hideFlag = hideFlag;
        this.nbt.setInt("HideFlags", hideFlag);
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
                final String name = AdventureSerializer.serialize(displayName);
                nbtCompound.setString("Name", name);
            } else {
                nbtCompound.removeTag("Name");
            }
        });
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder lore(@NotNull List<@NotNull Component> lore) {
        this.lore = lore;
        handleCompound("display", nbtCompound -> {
            final NBTList<NBTString> loreNBT = new NBTList<>(NBTTypes.TAG_String);
            for (Component line : lore) {
                loreNBT.add(new NBTString(GsonComponentSerializer.gson().serialize(line)));
            }
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
        this.enchantmentMap = enchantments;
        handleMap(enchantmentMap, "Enchantments", nbt, () -> {
            NBTUtils.writeEnchant(nbt, "Enchantments", enchantmentMap);
            return nbt.get("Enchantments");
        });
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
        this.enchantmentMap.clear();
        enchantments(enchantmentMap);
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder attributes(@NotNull List<@NotNull ItemAttribute> attributes) {
        this.attributes = attributes;

        handleCollection(attributes, "AttributeModifiers", nbt, () -> {
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
            return attributesNBT;
        });

        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder customModelData(int customModelData) {
        this.customModelData = customModelData;
        this.nbt.setInt("CustomModelData", customModelData);
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder canPlaceOn(@NotNull Set<@NotNull Block> blocks) {
        this.canPlaceOn = blocks;
        handleCollection(canPlaceOn, "CanPlaceOn", nbt, () -> {
            NBTList<NBTString> list = new NBTList<>(NBTTypes.TAG_String);
            canPlaceOn.forEach(block -> list.add(new NBTString(block.getName())));
            nbt.set("CanPlaceOn", list);
            return list;
        });
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder canPlaceOn(@NotNull Block... blocks) {
        return canPlaceOn(Set.of(blocks));
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder canDestroy(@NotNull Set<@NotNull Block> blocks) {
        this.canDestroy = blocks;
        handleCollection(canDestroy, "CanDestroy", nbt, () -> {
            NBTList<NBTString> list = new NBTList<>(NBTTypes.TAG_String);
            canDestroy.forEach(block -> list.add(new NBTString(block.getName())));
            nbt.set("CanDestroy", list);
            return list;
        });
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder canDestroy(@NotNull Block... blocks) {
        return canDestroy(Set.of(blocks));
    }

    public <T> @NotNull ItemMetaBuilder set(@NotNull ItemTag<T> tag, @Nullable T value) {
        if (value != null) {
            tag.write(nbt, value);
        } else {
            this.nbt.removeTag(tag.getKey());
        }
        return this;
    }

    @Contract("-> new")
    public abstract @NotNull ItemMeta build();

    public abstract void read(@NotNull NBTCompound nbtCompound);

    protected abstract @NotNull Supplier<@NotNull ItemMetaBuilder> getSupplier();

    protected void handleCompound(@NotNull String key,
                                  @NotNull Consumer<@NotNull NBTCompound> consumer) {
        NBTCompound compound = null;
        boolean newNbt = false;
        if (nbt.containsKey(key)) {
            NBT dNbt = nbt.get(key);
            if (dNbt instanceof NBTCompound) {
                compound = (NBTCompound) dNbt;
            }
        } else {
            compound = new NBTCompound();
            newNbt = true;
        }

        if (compound != null) {
            consumer.accept(compound);

            if (newNbt && compound.getSize() > 0) {
                this.nbt.set(key, compound);
            } else if (!newNbt && compound.getSize() == 0) {
                this.nbt.removeTag(key);
            }

        }
    }

    protected void handleNullable(@Nullable Object value,
                                  @NotNull String key,
                                  @NotNull NBTCompound nbtCompound,
                                  @NotNull Supplier<@NotNull NBT> supplier) {
        if (value != null) {
            nbtCompound.set(key, supplier.get());
        } else {
            nbtCompound.removeTag(key);
        }
    }

    protected void handleCollection(@NotNull Collection<?> objects,
                                    @NotNull String key,
                                    @NotNull NBTCompound nbtCompound,
                                    @NotNull Supplier<@NotNull NBT> supplier) {
        if (!objects.isEmpty()) {
            nbtCompound.set(key, supplier.get());
        } else {
            nbtCompound.removeTag(key);
        }
    }

    protected void handleMap(@NotNull Map<?, ?> objects,
                             @NotNull String key,
                             @NotNull NBTCompound nbtCompound,
                             @NotNull Supplier<@NotNull NBT> supplier) {
        if (!objects.isEmpty()) {
            nbtCompound.set(key, supplier.get());
        } else {
            nbtCompound.removeTag(key);
        }
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull ItemMetaBuilder fromNBT(@NotNull ItemMetaBuilder src, @NotNull NBTCompound nbtCompound) {
        ItemMetaBuilder dest = src.getSupplier().get();
        dest.nbt = nbtCompound.deepClone();
        NBTUtils.loadDataIntoMeta(dest, dest.nbt);
        return dest;
    }

    public interface Provider<T extends ItemMetaBuilder> {
    }

}
