package net.minestom.server.item;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.item.component.ItemComponentPatch;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.Taggable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.function.Consumer;

@Deprecated
public sealed interface ItemMeta extends TagReadable permits ItemMetaImpl {

    @NotNull ItemComponentPatch components();

    @Override
    <T> @UnknownNullability T getTag(@NotNull Tag<T> tag);

    @Contract(value = "_, -> new", pure = true)
    @NotNull ItemMeta with(@NotNull Consumer<@NotNull Builder> builderConsumer);

    @NotNull CompoundBinaryTag toNBT();

    @NotNull String toSNBT();

    @Contract(pure = true)
    int getDamage();

    @Contract(pure = true)
    boolean isUnbreakable();

    @Contract(pure = true)
    int getHideFlag();

    @Contract(pure = true)
    @Nullable Component getDisplayName();

    @Contract(pure = true)
    @NotNull List<@NotNull Component> getLore();

    @Contract(pure = true)
    @NotNull Map<Enchantment, Short> getEnchantmentMap();

    @Contract(pure = true)
    @NotNull List<@NotNull ItemAttribute> getAttributes();

    @Contract(pure = true)
    int getCustomModelData();

    @Contract(pure = true)
    @NotNull Set<@NotNull String> getCanDestroy();

    @Contract(pure = true)
    boolean canDestroy(@NotNull Block block);

    @Contract(pure = true)
    @NotNull Set<@NotNull String> getCanPlaceOn();

    @Contract(pure = true)
    boolean canPlaceOn(@NotNull Block block);

    @Deprecated
    sealed interface Builder extends Taggable
            permits ItemMetaImpl.Builder, ItemMetaView.Builder {
        @NotNull ItemMeta build();

        @NotNull ItemComponentPatch.Builder components();

        default <T> @NotNull Builder set(@NotNull Tag<T> tag, @Nullable T value) {
            setTag(tag, value);
            return this;
        }

        @Contract("_ -> this")
        default @NotNull Builder damage(int damage) {
            return set(ItemTags.DAMAGE, damage);
        }

        @Contract("_ -> this")
        default @NotNull Builder unbreakable(boolean unbreakable) {
            return set(ItemTags.UNBREAKABLE, unbreakable);
        }

        @Contract("_ -> this")
        default @NotNull Builder hideFlag(int hideFlag) {
            return set(ItemTags.HIDE_FLAGS, hideFlag);
        }

        @Contract("_ -> this")
        default @NotNull Builder hideFlag(@NotNull ItemHideFlag... hideFlags) {
            int result = 0;
            for (ItemHideFlag hideFlag : hideFlags) result |= hideFlag.getBitFieldPart();
            return hideFlag(result);
        }

        @Contract("_ -> this")
        default @NotNull Builder displayName(@Nullable Component displayName) {
            return set(ItemTags.NAME, displayName);
        }

        @Contract("_ -> this")
        default @NotNull Builder lore(@NotNull List<? extends Component> lore) {
            return set(ItemTags.LORE, lore.isEmpty() ? null : List.class.cast(lore));
        }

        @Contract("_ -> this")
        default @NotNull Builder lore(Component... lore) {
            return lore(Arrays.asList(lore));
        }

        @Contract("_ -> this")
        default @NotNull Builder enchantments(@NotNull Map<Enchantment, Short> enchantments) {
            return set(ItemTags.ENCHANTMENTS, Map.copyOf(enchantments));
        }

        @Contract("_, _ -> this")
        default @NotNull Builder enchantment(@NotNull Enchantment enchantment, short level) {
            var enchantments = new HashMap<>(getTag(ItemTags.ENCHANTMENTS));
            enchantments.put(enchantment, level);
            return enchantments(enchantments);
        }

        @Contract("-> this")
        default @NotNull Builder clearEnchantment() {
            return enchantments(Map.of());
        }

        @Contract("_ -> this")
        default @NotNull Builder attributes(@NotNull List<@NotNull ItemAttribute> attributes) {
            return set(ItemTags.ATTRIBUTES, attributes.isEmpty() ? null : attributes);
        }

        @Contract("_ -> this")
        default @NotNull Builder customModelData(int customModelData) {
            return set(ItemTags.CUSTOM_MODEL_DATA, customModelData);
        }

        @Contract("_ -> this")
        default @NotNull Builder canPlaceOn(@NotNull Set<@NotNull Block> blocks) {
            return set(ItemTags.CAN_PLACE_ON, blocks.stream().map(StaticProtocolObject::name).toList());
        }

        @Contract("_ -> this")
        default @NotNull Builder canPlaceOn(@NotNull Block... blocks) {
            return canPlaceOn(Set.of(blocks));
        }

        @Contract("_ -> this")
        default @NotNull Builder canDestroy(@NotNull Set<@NotNull Block> blocks) {
            return set(ItemTags.CAN_DESTROY, blocks.stream().map(StaticProtocolObject::name).toList());
        }

        @Contract("_ -> this")
        default @NotNull Builder canDestroy(@NotNull Block... blocks) {
            return canDestroy(Set.of(blocks));
        }
    }
}
