package net.minestom.server.item;

import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.*;
import java.util.function.Consumer;

public class ItemMeta implements Writeable {

    private final int damage;
    private final boolean unbreakable;
    private final int hideFlag;
    private final Component displayName;
    private final List<Component> lore;

    private final Map<Enchantment, Short> enchantmentMap;
    private final List<ItemAttribute> attributes;

    private final int customModelData;

    private final Set<Block> canDestroy;
    private final Set<Block> canPlaceOn;

    private final NBTCompound nbt;
    private final ItemMetaBuilder emptyBuilder;

    private String cachedSNBT;
    private ByteBuf cachedBuffer;

    protected ItemMeta(@NotNull ItemMetaBuilder metaBuilder) {
        this.damage = metaBuilder.damage;
        this.unbreakable = metaBuilder.unbreakable;
        this.hideFlag = metaBuilder.hideFlag;
        this.displayName = metaBuilder.displayName;
        this.lore = new ArrayList<>(metaBuilder.lore);
        this.enchantmentMap = new HashMap<>(metaBuilder.enchantmentMap);
        this.attributes = new ArrayList<>(metaBuilder.attributes);
        this.customModelData = metaBuilder.customModelData;
        this.canDestroy = new HashSet<>(metaBuilder.canDestroy);
        this.canPlaceOn = new HashSet<>(metaBuilder.canPlaceOn);

        this.nbt = metaBuilder.nbt;
        this.emptyBuilder = metaBuilder.getSupplier().get();
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
        return Collections.unmodifiableList(lore);
    }

    @Contract(pure = true)
    public @NotNull Map<Enchantment, Short> getEnchantmentMap() {
        return Collections.unmodifiableMap(enchantmentMap);
    }

    @Contract(pure = true)
    public @NotNull List<@NotNull ItemAttribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    @Contract(pure = true)
    public int getCustomModelData() {
        return customModelData;
    }

    @Contract(pure = true)
    public @NotNull Set<@NotNull Block> getCanDestroy() {
        return Collections.unmodifiableSet(canDestroy);
    }

    @Contract(pure = true)
    public @NotNull Set<@NotNull Block> getCanPlaceOn() {
        return Collections.unmodifiableSet(canPlaceOn);
    }

    @Contract(pure = true)
    public <T> T getOrDefault(@NotNull ItemTag<T> tag, @Nullable T defaultValue) {
        var key = tag.getKey();
        if (nbt.containsKey(key)) {
            return tag.read(toNBT());
        } else {
            return defaultValue;
        }
    }

    public <T> @Nullable T get(@NotNull ItemTag<T> tag) {
        return tag.read(toNBT());
    }

    public @NotNull NBTCompound toNBT() {
        return nbt.deepClone();
    }

    public @NotNull String toSNBT() {
        if (cachedSNBT == null) {
            this.cachedSNBT = nbt.toSNBT();
        }
        return cachedSNBT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemMeta itemMeta = (ItemMeta) o;
        return nbt.equals(itemMeta.nbt);
    }

    @Override
    public int hashCode() {
        return nbt.hashCode();
    }

    @Contract(value = "-> new", pure = true)
    protected @NotNull ItemMetaBuilder builder() {
        return ItemMetaBuilder.fromNBT(emptyBuilder, nbt);
    }

    @Override
    public synchronized void write(@NotNull BinaryWriter writer) {
        if (cachedBuffer == null) {
            BinaryWriter w = new BinaryWriter();
            w.writeNBT("", nbt);
            this.cachedBuffer = w.getBuffer();
        }
        writer.write(cachedBuffer);
        this.cachedBuffer.resetReaderIndex();
    }
}
