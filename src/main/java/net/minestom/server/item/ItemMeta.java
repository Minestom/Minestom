package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.utils.NBTUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ItemMeta {

    private final ItemMetaBuilder builder;

    private final int damage;
    private final boolean unbreakable;
    private final int hideFlag;
    private final Component displayName;
    private final List<Component> lore;

    private final Map<Enchantment, Short> enchantmentMap;
    private final List<ItemAttribute> attributes;

    private final int customModelData;

    private final @Nullable NBTCompound originalNbt;
    private SoftReference<NBTCompound> cache;

    protected ItemMeta(@NotNull ItemMetaBuilder metaBuilder) {
        this.builder = metaBuilder.clone();
        this.damage = 0;
        this.unbreakable = false;
        this.hideFlag = 0;
        this.displayName = metaBuilder.displayName;
        this.lore = Collections.unmodifiableList(metaBuilder.lore);
        this.enchantmentMap = Collections.unmodifiableMap(metaBuilder.enchantmentMap);
        this.attributes = new ArrayList<>();
        this.customModelData = 0;

        // Can be null
        this.originalNbt = metaBuilder.originalNBT;
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull ItemMeta with(@NotNull Consumer<@NotNull ItemMetaBuilder> builderConsumer) {
        var builder = builder();
        builderConsumer.accept(builder);
        return builder.build();
    }

    @Contract(pure = true)
    public int getDamage() {
        return damage;
    }

    @Contract(pure = true)
    public boolean isUnbreakable() {
        return unbreakable;
    }

    @Contract(pure = true)
    public int getHideFlag() {
        return hideFlag;
    }

    @Contract(pure = true)
    public @Nullable Component getDisplayName() {
        return displayName;
    }

    @Contract(pure = true)
    public @NotNull List<@NotNull Component> getLore() {
        return lore;
    }

    @Contract(pure = true)
    public @NotNull Map<Enchantment, Short> getEnchantmentMap() {
        return enchantmentMap;
    }

    @Contract(pure = true)
    public @NotNull List<ItemAttribute> getAttributes() {
        return attributes;
    }

    @Contract(pure = true)
    public int getCustomModelData() {
        return customModelData;
    }

    public @NotNull NBTCompound toNBT() {
        if (originalNbt != null) {
            // Return the nbt this meta has been created with
            return originalNbt;
        }

        var nbt = cache != null ? cache.get() : null;
        if (nbt == null) {
            nbt = NBTUtils.metaToNBT(this);
            this.builder.write(nbt);
            this.cache = new SoftReference<>(nbt);
        }

        return nbt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemMeta itemMeta = (ItemMeta) o;
        return toNBT().equals(itemMeta.toNBT());
    }

    @Override
    public int hashCode() {
        return toNBT().hashCode();
    }

    @Contract(value = "-> new", pure = true)
    protected @NotNull ItemMetaBuilder builder() {
        return builder.clone();
    }
}
