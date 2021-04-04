package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.utils.NBTUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.*;
import java.util.function.Supplier;

public abstract class ItemMetaBuilder implements Cloneable {

    protected int damage;
    protected boolean unbreakable;
    protected int hideFlag;
    protected Component displayName;
    protected List<Component> lore = new ArrayList<>();
    protected Map<Enchantment, Short> enchantmentMap = new HashMap<>();
    protected List<ItemAttribute> attributes = new ArrayList<>();
    protected int customModelData;

    protected NBTCompound originalNBT;

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder damage(int damage) {
        this.damage = damage;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder hideFlag(int hideFlag) {
        this.hideFlag = hideFlag;
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
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder lore(@NotNull List<@NotNull Component> lore) {
        this.lore = lore;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder lore(Component... lore) {
        lore(Arrays.asList(lore));
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder enchantments(@NotNull Map<Enchantment, Short> enchantments) {
        this.enchantmentMap.putAll(enchantments);
        return this;
    }

    @Contract("_, _ -> this")
    public @NotNull ItemMetaBuilder enchantment(@NotNull Enchantment enchantment, short level) {
        this.enchantmentMap.put(enchantment, level);
        return this;
    }

    @Contract("-> this")
    public @NotNull ItemMetaBuilder clearEnchantment() {
        this.enchantmentMap.clear();
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder attributes(@NotNull List<@NotNull ItemAttribute> attributes) {
        this.attributes = attributes;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ItemMetaBuilder customModelData(int customModelData) {
        this.customModelData = customModelData;
        return this;
    }

    public <T> @NotNull ItemMetaBuilder set(@NotNull ItemTag<T> tag, @Nullable T value) {
        if (originalNBT != null) {
            // Item is from nbt
            if (value != null) {
                tag.write(originalNBT, value);
            } else {
                this.originalNBT.removeTag(tag.getKey());
            }
            return this;
        } else {
            // Create item meta based on nbt
            var currentNbt = build().toNBT();
            return fromNBT(this, currentNbt).set(tag, value);
        }
    }

    @Contract("-> new")
    public abstract @NotNull ItemMeta build();

    public abstract void read(@NotNull NBTCompound nbtCompound);

    public abstract void write(@NotNull NBTCompound nbtCompound);

    protected abstract void deepClone(@NotNull ItemMetaBuilder metaBuilder);

    protected abstract @NotNull Supplier<@NotNull ItemMetaBuilder> getSupplier();

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull ItemMetaBuilder fromNBT(@NotNull ItemMetaBuilder src, @NotNull NBTCompound nbtCompound) {
        ItemMetaBuilder dest = src.getSupplier().get();
        NBTUtils.loadDataIntoMeta(dest, nbtCompound);
        dest.originalNBT = nbtCompound;
        return dest;
    }

    @Override
    protected ItemMetaBuilder clone() {
        try {
            var builder = (ItemMetaBuilder) super.clone();
            builder.displayName = displayName;
            builder.lore = new ArrayList<>(lore);
            builder.enchantmentMap = new HashMap<>(enchantmentMap);
            deepClone(builder);
            return builder;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("Weird thing happened");
        }
    }

    public interface Provider<T> {
    }

}
