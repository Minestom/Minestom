package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.TagWritable;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface ItemMeta extends TagReadable, Writeable {
    @Override
    <T> @UnknownNullability T getTag(@NotNull Tag<T> tag);

    @Contract(value = "_, -> new", pure = true)
    @NotNull ItemMeta with(@NotNull Consumer<@NotNull Builder> builderConsumer);

    @NotNull NBTCompound toNBT();

    @NotNull String toSNBT();

    @Contract(pure = true)
    default int getDamage() {
        return getTag(ItemTags.DAMAGE);
    }

    @Contract(pure = true)
    default boolean isUnbreakable() {
        return getTag(ItemTags.UNBREAKABLE) == 1;
    }

    @Contract(pure = true)
    default int getHideFlag() {
        return getTag(ItemTags.HIDE_FLAGS);
    }

    @Contract(pure = true)
    default @Nullable Component getDisplayName() {
        return getTag(ItemTags.NAME);
    }

    @Contract(pure = true)
    default @NotNull List<@NotNull Component> getLore() {
        return getTag(ItemTags.LORE);
    }

    @Contract(pure = true)
    default @NotNull Map<Enchantment, Short> getEnchantmentMap() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Contract(pure = true)
    default @NotNull List<@NotNull ItemAttribute> getAttributes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Contract(pure = true)
    default int getCustomModelData() {
        return getTag(ItemTags.CUSTOM_MODEL_DATA);
    }

    @Contract(pure = true)
    default @NotNull Set<@NotNull Block> getCanDestroy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Contract(pure = true)
    default @NotNull Set<@NotNull Block> getCanPlaceOn() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    interface Builder extends TagWritable {
        @Override
        <T> void setTag(@NotNull Tag<T> tag, @Nullable T value);

        @NotNull ItemMeta build();

        default <T> @NotNull Builder set(@NotNull Tag<T> tag, @Nullable T value) {
            setTag(tag, value);
            return this;
        }

        @Contract("_ -> this")
        default @NotNull Builder damage(int damage) {
            setTag(ItemTags.DAMAGE, damage);
            return this;
        }

        @Contract("_ -> this")
        default @NotNull Builder unbreakable(boolean unbreakable) {
            setTag(ItemTags.UNBREAKABLE, (byte) (unbreakable ? 1 : 0));
            return this;
        }

        @Contract("_ -> this")
        default @NotNull Builder hideFlag(int hideFlag) {
            setTag(ItemTags.HIDE_FLAGS, hideFlag);
            return this;
        }

        @Contract("_ -> this")
        default @NotNull Builder hideFlag(@NotNull ItemHideFlag... hideFlags) {
            int result = 0;
            for (ItemHideFlag hideFlag : hideFlags) result |= hideFlag.getBitFieldPart();
            return hideFlag(result);
        }

        @Contract("_ -> this")
        default @NotNull Builder displayName(@Nullable Component displayName) {
            setTag(ItemTags.NAME, displayName);
            return this;
        }

        @Contract("_ -> this")
        default @NotNull Builder lore(@NotNull List<? extends Component> lore) {
            setTag(ItemTags.LORE, List.class.cast(lore));
            return this;
        }

        @Contract("_ -> this")
        default @NotNull Builder lore(Component... lore) {
            return lore(Arrays.asList(lore));
        }

        @Contract("_ -> this")
        default @NotNull Builder enchantments(@NotNull Map<Enchantment, Short> enchantments) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Contract("_, _ -> this")
        default @NotNull Builder enchantment(@NotNull Enchantment enchantment, short level) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Contract("-> this")
        default @NotNull Builder clearEnchantment() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Contract("_ -> this")
        default @NotNull Builder attributes(@NotNull List<@NotNull ItemAttribute> attributes) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Contract("_ -> this")
        default @NotNull Builder customModelData(int customModelData) {
            setTag(ItemTags.CUSTOM_MODEL_DATA, customModelData);
            return this;
        }

        @Contract("_ -> this")
        default @NotNull Builder canPlaceOn(@NotNull Set<@NotNull Block> blocks) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Contract("_ -> this")
        default @NotNull Builder canPlaceOn(@NotNull Block... blocks) {
            return canPlaceOn(Set.of(blocks));
        }

        @Contract("_ -> this")
        default @NotNull Builder canDestroy(@NotNull Set<@NotNull Block> blocks) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Contract("_ -> this")
        default @NotNull Builder canDestroy(@NotNull Block... blocks) {
            return canDestroy(Set.of(blocks));
        }
    }
}
