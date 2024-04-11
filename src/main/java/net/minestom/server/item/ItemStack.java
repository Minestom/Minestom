package net.minestom.server.item;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.item.component.ItemComponent;
import net.minestom.server.item.component.ItemComponentMap;
import net.minestom.server.inventory.ContainerInventory;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.TagWritable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;

/**
 * Represents an immutable item to be placed inside {@link net.minestom.server.inventory.PlayerInventory},
 * {@link net.minestom.server.inventory.Inventory} or even on the ground {@link net.minestom.server.entity.ItemEntity}.
 * <p>
 * An item stack cannot be null, {@link ItemStack#AIR} should be used instead.
 */
public sealed interface ItemStack extends TagReadable, ItemComponentMap, HoverEventSource<HoverEvent.ShowItem>
        permits ItemStackImpl {

    /**
     * Constant AIR item. Should be used instead of 'null'.
     */
    @NotNull ItemStack AIR = ItemStack.of(Material.AIR);

    @Contract(value = "_ -> new", pure = true)
    static @NotNull Builder builder(@NotNull Material material) {
        return new ItemStackImpl.Builder(material, 1);
    }

    @Contract(value = "_ ,_ -> new", pure = true)
    static @NotNull ItemStack of(@NotNull Material material, int amount) {
        return ItemStackImpl.create(material, amount);
    }

    @Contract(value = "_ -> new", pure = true)
    static @NotNull ItemStack of(@NotNull Material material) {
        return of(material, 1);
    }

    /**
     * Converts this item to an NBT tag containing the id (material), count (amount), and tag (meta).
     *
     * @param nbtCompound The nbt representation of the item
     */
    static @NotNull ItemStack fromItemNBT(@NotNull CompoundBinaryTag nbtCompound) {
//        String id = nbtCompound.getString("id");
//        Check.notNull(id, "Item NBT must contain an id field.");
//        Material material = Material.fromNamespaceId(id);
//        Check.notNull(material, "Unknown material: {0}", id);
//
//        Byte amount = nbtCompound.getByte("Count");
//        if (amount == null) amount = 1;
//        final CompoundBinaryTag tag = nbtCompound.getCompound("tag");
//        return tag != null ? fromNBT(material, tag, amount) : of(material, amount);
    }

    @Contract(pure = true)
    @NotNull Material material();

    @Contract(pure = true)
    int amount();

    @Contract(value = "_, -> new", pure = true)
    @NotNull ItemStack with(@NotNull Consumer<@NotNull Builder> consumer);

    @Contract(value = "_, -> new", pure = true)
    @NotNull ItemStack withMaterial(@NotNull Material material);

    @Contract(value = "_, -> new", pure = true)
    @NotNull ItemStack withAmount(int amount);

    @Contract(value = "_, -> new", pure = true)
    default @NotNull ItemStack withAmount(@NotNull IntUnaryOperator intUnaryOperator) {
        return withAmount(intUnaryOperator.applyAsInt(amount()));
    }

    @Contract(value = "_, _ -> new", pure = true)
    <T> @NotNull ItemStack with(@NotNull ItemComponent<T> component, T value);

    @Contract(value = "_, -> new", pure = true)
    @NotNull ItemStack without(@NotNull ItemComponent<?> component);

    @Contract(value = "_, _ -> new", pure = true)
    default <T> @NotNull ItemStack withTag(@NotNull Tag<T> tag, @Nullable T value) {
        return withMeta(builder -> builder.set(tag, value));
    }

    @Override
    @Contract(pure = true)
    default <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return getOrDefault(ItemComponent.CUSTOM_DATA, CustomData.EMPTY).getTag(tag);
    }

    @Contract(value = "_, -> new", pure = true)
    @NotNull ItemStack consume(int amount);

    @Contract(pure = true)
    default boolean isAir() {
        return material() == Material.AIR;
    }

    @Contract(pure = true)
    boolean isSimilar(@NotNull ItemStack itemStack);

    /**
     * Converts this item to an NBT tag containing the id (material), count (amount), and components (diff)
     *
     * @return The nbt representation of the item
     */
    @NotNull CompoundBinaryTag toItemNBT();

    @Override
    default @NotNull HoverEvent<HoverEvent.ShowItem> asHoverEvent(@NotNull UnaryOperator<HoverEvent.ShowItem> op) {
        //todo
//        try {
//            final BinaryTagHolder tagHolder = BinaryTagHolder.encode(meta().toNBT(), MinestomAdventure.NBT_CODEC);
//            return HoverEvent.showItem(op.apply(HoverEvent.ShowItem.showItem(material(), amount(), tagHolder)));
//        } catch (IOException e) {
//            //todo(matt): revisit,
//            throw new RuntimeException(e);
//        }
    }

    sealed interface Builder extends TagWritable
            permits ItemStackImpl.Builder {

        @Contract(value = "_ -> this")
        @NotNull Builder amount(int amount);

        @Contract(value = "_, _ -> this")
        <T> @NotNull Builder set(@NotNull ItemComponent<T> component, T value);

        @Contract(value = "_ -> this")
        @NotNull Builder remove(@NotNull ItemComponent<?> component);

        @Contract(value = "_, _ -> this")
        default <T> @NotNull Builder set(@NotNull Tag<T> tag, @Nullable T value) {
            setTag(tag, value);
            return this;
        }

        @Contract(value = "-> new", pure = true)
        @NotNull ItemStack build();

    }
}
