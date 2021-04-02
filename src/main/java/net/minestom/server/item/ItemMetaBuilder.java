package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.utils.NBTUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.*;

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

    protected ItemMetaBuilder() {
    }

    public @NotNull ItemMetaBuilder damage(int damage) {
        this.damage = damage;
        return this;
    }

    public @NotNull ItemMetaBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public @NotNull ItemMetaBuilder hideFlag(int hideFlag) {
        this.hideFlag = hideFlag;
        return this;
    }

    public @NotNull ItemMetaBuilder displayName(@Nullable Component displayName) {
        this.displayName = displayName;
        return this;
    }

    public @NotNull ItemMetaBuilder lore(List<@NotNull Component> lore) {
        this.lore = lore;
        return this;
    }

    public @NotNull ItemMetaBuilder lore(Component... lore) {
        lore(Arrays.asList(lore));
        return this;
    }

    public @NotNull ItemMetaBuilder enchantments(@NotNull Map<Enchantment, Short> enchantments) {
        this.enchantmentMap.putAll(enchantments);
        return this;
    }

    public @NotNull ItemMetaBuilder enchantment(@NotNull Enchantment enchantment, short level) {
        this.enchantmentMap.put(enchantment, level);
        return this;
    }

    public @NotNull ItemMetaBuilder clearEnchantment() {
        this.enchantmentMap.clear();
        return this;
    }

    public @NotNull ItemMetaBuilder attributes(List<ItemAttribute> attributes) {
        this.attributes = attributes;
        return this;
    }

    public @NotNull ItemMetaBuilder customModelData(int customModelData) {
        this.customModelData = customModelData;
        return this;
    }

    public abstract @NotNull ItemMeta build();

    public abstract void read(@NotNull NBTCompound nbtCompound);

    public abstract void write(@NotNull NBTCompound nbtCompound);

    protected abstract void deepClone(@NotNull ItemMetaBuilder metaBuilder);

    public static @NotNull ItemMetaBuilder fromNBT(@NotNull ItemMetaBuilder metaBuilder, @NotNull NBTCompound nbtCompound) {
        NBTUtils.loadDataIntoMeta(metaBuilder, nbtCompound);
        metaBuilder.originalNBT = nbtCompound;
        return metaBuilder;
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
