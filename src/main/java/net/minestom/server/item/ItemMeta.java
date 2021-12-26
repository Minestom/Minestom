package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ItemMeta implements TagReadable, Writeable {

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

    private final ItemMetaBuilder metaBuilder;
    private final NBTCompound nbt;

    private String cachedSNBT;
    private ByteBuffer cachedBuffer;

    protected ItemMeta(@NotNull ItemMetaBuilder metaBuilder) {
        this.damage = metaBuilder.damage;
        this.unbreakable = metaBuilder.unbreakable;
        this.hideFlag = metaBuilder.hideFlag;
        this.displayName = metaBuilder.displayName;
        this.lore = List.copyOf(metaBuilder.lore);
        this.enchantmentMap = Map.copyOf(metaBuilder.enchantmentMap);
        this.attributes = List.copyOf(metaBuilder.attributes);
        this.customModelData = metaBuilder.customModelData;
        this.canDestroy = Set.copyOf(metaBuilder.canDestroy);
        this.canPlaceOn = Set.copyOf(metaBuilder.canPlaceOn);

        this.metaBuilder = metaBuilder;
        this.nbt = metaBuilder.nbt.toCompound();
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
    public @NotNull List<@NotNull ItemAttribute> getAttributes() {
        return attributes;
    }

    @Contract(pure = true)
    public int getCustomModelData() {
        return customModelData;
    }

    @Contract(pure = true)
    public @NotNull Set<@NotNull Block> getCanDestroy() {
        return canDestroy;
    }

    @Contract(pure = true)
    public @NotNull Set<@NotNull Block> getCanPlaceOn() {
        return canPlaceOn;
    }

    @Override
    public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
        return tag.read(nbt);
    }

    public @NotNull NBTCompound toNBT() {
        return nbt;
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "nbt=" + nbt +
                '}';
    }

    @Contract(value = "-> new", pure = true)
    protected @NotNull ItemMetaBuilder builder() {
        ItemMetaBuilder result = metaBuilder.createEmpty();
        ItemMetaBuilder.resetMeta(result, nbt);
        return result;
    }

    @Override
    public synchronized void write(@NotNull BinaryWriter writer) {
        if (nbt.isEmpty()) {
            writer.writeByte((byte) 0);
            return;
        }
        if (cachedBuffer == null) {
            BinaryWriter w = new BinaryWriter();
            w.writeNBT("", nbt);
            this.cachedBuffer = w.getBuffer();
        }
        writer.write(cachedBuffer.flip());
    }
}
