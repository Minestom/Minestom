package net.minestom.server.item;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.MinestomDataComponentValue;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.item.component.CustomModelData;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;

/**
 * Represents an immutable item to be placed inside {@link net.minestom.server.inventory.PlayerInventory},
 * {@link net.minestom.server.inventory.Inventory} or even on the ground {@link net.minestom.server.entity.ItemEntity}.
 * <p>
 * An item stack cannot be null, {@link ItemStack#AIR} should be used instead.
 */
public sealed interface ItemStack extends TagReadable, DataComponent.Holder, HoverEventSource<HoverEvent.ShowItem>
        permits ItemStackImpl {

    @NotNull NetworkBuffer.Type<ItemStack> NETWORK_TYPE = ItemStackImpl.networkType(DataComponent.PATCH_NETWORK_TYPE);
    @NotNull NetworkBuffer.Type<ItemStack> UNTRUSTED_NETWORK_TYPE = ItemStackImpl.networkType(DataComponent.UNTRUSTED_PATCH_NETWORK_TYPE);
    @NotNull NetworkBuffer.Type<ItemStack> STRICT_NETWORK_TYPE = NETWORK_TYPE.transform(itemStack -> {
        Check.argCondition(itemStack.amount() == 0 || itemStack.isAir(), "ItemStack cannot be empty");
        return itemStack;
    }, itemStack -> {
        Check.argCondition(itemStack.amount() == 0 || itemStack.isAir(), "ItemStack cannot be empty");
        return itemStack;
    });
    @NotNull Codec<ItemStack> CODEC = new StructCodec<>() {
        // These exist because Mojang optionally decodes count (ie missing will default to 1),
        // but when encoding they always include the 1. We want to preserve this behavior and
        // since its currently a one off we can just do it here in a gross way.
        private static final StructCodec<ItemStack> DECODER = StructCodec.struct(
                "id", Material.CODEC, ItemStack::material,
                "count", Codec.INT.optional(1), ItemStack::amount,
                "components", DataComponent.PATCH_CODEC.optional(DataComponentMap.EMPTY), ItemStack::componentPatch,
                ItemStack::of);
        private static final StructCodec<ItemStack> ENCODER = StructCodec.struct(
                "id", Material.CODEC, ItemStack::material,
                "count", Codec.INT, ItemStack::amount,
                "components", DataComponent.PATCH_CODEC.optional(DataComponentMap.EMPTY), ItemStack::componentPatch,
                ItemStack::of);

        @Override
        public @NotNull <D> Result<ItemStack> decodeFromMap(@NotNull Transcoder<D> coder, Transcoder.@NotNull MapLike<D> map) {
            return DECODER.decodeFromMap(coder, map);
        }

        @Override
        public @NotNull <D> Result<D> encodeToMap(@NotNull Transcoder<D> coder, @NotNull ItemStack value, Transcoder.@NotNull MapBuilder<D> map) {
            return ENCODER.encodeToMap(coder, value, map);
        }
    };

    /**
     * Constant AIR item. Should be used instead of 'null'.
     */
    @NotNull ItemStack AIR = ItemStack.of(Material.AIR);

    @Contract(value = "_ -> new", pure = true)
    static @NotNull Builder builder(@NotNull Material material) {
        return new ItemStackImpl.Builder(material, 1);
    }

    @Contract(value = "_ -> new", pure = true)
    static @NotNull ItemStack of(@NotNull Material material) {
        return of(material, 1);
    }

    @Contract(value = "_ ,_ -> new", pure = true)
    static @NotNull ItemStack of(@NotNull Material material, int amount) {
        return ItemStackImpl.create(material, amount);
    }

    @Contract(value = "_ ,_ -> new", pure = true)
    static @NotNull ItemStack of(@NotNull Material material, @NotNull DataComponentMap components) {
        return ItemStackImpl.create(material, 1, components);
    }

    @Contract(value = "_ ,_, _ -> new", pure = true)
    static @NotNull ItemStack of(@NotNull Material material, int amount, @NotNull DataComponentMap components) {
        return ItemStackImpl.create(material, amount, components);
    }

    /**
     * Converts this item to an NBT tag containing the id (material), count (amount), and components.
     *
     * @param nbtCompound The nbt representation of the item
     */
    static @NotNull ItemStack fromItemNBT(@NotNull CompoundBinaryTag nbtCompound) {
        final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());
        return CODEC.decode(coder, nbtCompound).orElseThrow("Invalid NBT for ItemStack");
    }

    @Contract(pure = true)
    @NotNull Material material();

    @Contract(pure = true)
    int amount();

    @Contract(pure = true)
    @NotNull DataComponentMap componentPatch();

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

    /**
     * <p>Returns a new ItemStack with the given component set to the given value.</p>
     *
     * <p>Note: this should not be used to remove components, see {@link #without(DataComponent)}.</p>
     */
    @Contract(value = "_, _ -> new", pure = true)
    <T> @NotNull ItemStack with(@NotNull DataComponent<T> component, @NotNull T value);

    /**
     * Returns a new ItemStack with the given {@link Unit} component applied.
     *
     * @param component The unit component to apply
     * @return A new ItemStack with the given component applied
     */
    @Contract(value = "_ -> new", pure = true)
    default @NotNull ItemStack with(@NotNull DataComponent<Unit> component) {
        return with(component, Unit.INSTANCE);
    }

    /**
     * Applies a transformation to the value of a component, only if present.
     *
     * @param component The component type to modify
     * @param operator  The transformation function
     * @param <T>       The component type
     * @return A new ItemStack if the component was transformed, otherwise this.
     */
    default <T> @NotNull ItemStack with(@NotNull DataComponent<T> component, @NotNull UnaryOperator<T> operator) {
        T value = get(component);
        if (value == null) return this;
        return with(component, operator.apply(value));
    }

    /**
     * <p>Removes the given component from this item. This will explicitly remove the component from the item, as opposed
     * to reverting back to the default.</p>
     *
     * <p>For example, if {@link DataComponents#FOOD} is applied to an apple, and then this method is called,
     * the resulting itemstack will not be a food item at all, as opposed to returning to the default apple
     * food type. Likewise, if this method is called on a default apple, it will no longer be a food item.</p>
     *
     * @param component The component to remove
     * @return A new ItemStack without the given component
     */
    @Contract(value = "_, -> new", pure = true)
    @NotNull ItemStack without(@NotNull DataComponent<?> component);

    @Contract(value = "_, -> new", pure = true)
    default @NotNull ItemStack withCustomName(@NotNull Component customName) {
        return with(DataComponents.CUSTOM_NAME, customName);
    }

    @Contract(value = "_, -> new", pure = true)
    default @NotNull ItemStack withLore(@NotNull Component... lore) {
        return with(DataComponents.LORE, List.of(lore));
    }

    @Contract(value = "_, -> new", pure = true)
    default @NotNull ItemStack withLore(@NotNull List<Component> lore) {
        return with(DataComponents.LORE, lore);
    }

    @Contract(value = "_, -> new", pure = true)
    default @NotNull ItemStack withItemModel(@NotNull String model) {
        return with(DataComponents.ITEM_MODEL, model);
    }

    @Contract(value = "_, _, _, _ -> new", pure = true)
    default @NotNull ItemStack withCustomModelData(@NotNull List<Float> floats, @NotNull List<Boolean> flags, @NotNull List<String> strings, @NotNull List<RGBLike> colors) {
        return with(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(floats, flags, strings, colors));
    }

    @Contract(value = "_ -> new", pure = true)
    default @NotNull ItemStack withGlowing(boolean glowing) {
        return with(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, glowing);
    }

    @Contract(value = "-> new", pure = true)
    default @NotNull ItemStack withoutExtraTooltip() {
        return builder().hideExtraTooltip().build();
    }

    @Contract(pure = true)
    default int maxStackSize() {
        return get(DataComponents.MAX_STACK_SIZE, 64);
    }

    @Contract(value = "_ -> new", pure = true)
    default @NotNull ItemStack withMaxStackSize(int maxStackSize) {
        return with(DataComponents.MAX_STACK_SIZE, maxStackSize);
    }

    @Contract(value = "_, _ -> new", pure = true)
    default <T> @NotNull ItemStack withTag(@NotNull Tag<T> tag, @Nullable T value) {
        return with(DataComponents.CUSTOM_DATA, get(DataComponents.CUSTOM_DATA, CustomData.EMPTY).withTag(tag, value));
    }

    @Override
    @Contract(pure = true)
    default <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return get(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getTag(tag);
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
     * Converts this itemstack back into a builder (starting from the current state).
     *
     * @return this itemstack, as a builder.
     */
    @NotNull ItemStack.Builder builder();

    /**
     * Converts this item to an NBT tag containing the id (material), count (amount), and components (diff)
     *
     * @return The nbt representation of the item
     */
    @NotNull CompoundBinaryTag toItemNBT();

    @Override
    default @NotNull HoverEvent<HoverEvent.ShowItem> asHoverEvent(@NotNull UnaryOperator<HoverEvent.ShowItem> op) {
        if (componentPatch().isEmpty())
            return HoverEvent.showItem(op.apply(HoverEvent.ShowItem.showItem(material(), amount())));

        final Map<Key, DataComponentValue> dataComponents = new HashMap<>();
        for (final Map.Entry<DataComponent<?>, Object> entry : componentPatch().entrySet())
            dataComponents.put(entry.getKey().key(), MinestomDataComponentValue.dataComponentValue(entry.getValue()));
        return HoverEvent.showItem(op.apply(HoverEvent.ShowItem.showItem(material(), amount(), dataComponents)));
    }

    // These functions are mirrors of ComponentHolder, but we can't actually implement that interface
    // because it conflicts with DataComponent.Holder.

    static @NotNull Collection<Component> textComponents(@NotNull ItemStack itemStack) {
        final var components = new ArrayList<>(itemStack.get(DataComponents.LORE, List.of()));
        final var displayName = itemStack.get(DataComponents.CUSTOM_NAME);
        if (displayName != null) components.add(displayName);
        final var itemName = itemStack.get(DataComponents.ITEM_NAME);
        if (itemName != null) components.add(itemName);
        return List.copyOf(components);
    }

    static @NotNull ItemStack copyWithOperator(@NotNull ItemStack itemStack, @NotNull UnaryOperator<Component> operator) {
        return itemStack
                .with(DataComponents.CUSTOM_NAME, operator)
                .with(DataComponents.ITEM_NAME, operator)
                .with(DataComponents.LORE, (UnaryOperator<List<Component>>) lines -> {
                    final var translatedComponents = new ArrayList<Component>();
                    lines.forEach(component -> translatedComponents.add(operator.apply(component)));
                    return translatedComponents;
                });
    }

    sealed interface Hash permits ItemStackHashImpl.Air, ItemStackHashImpl.Item {
        @NotNull Hash AIR = new ItemStackHashImpl.Air();

        static @NotNull Hash of(@NotNull ItemStack itemStack) {
            return ItemStackHashImpl.of(new RegistryTranscoder<>(Transcoder.CRC32_HASH, MinecraftServer.process()), itemStack);
        }

        @NotNull NetworkBuffer.Type<Hash> NETWORK_TYPE = ItemStackHashImpl.NETWORK_TYPE;
    }

    sealed interface Builder permits ItemStackImpl.Builder {

        @Contract(value = "_ -> this")
        @NotNull Builder material(@NotNull Material material);

        @Contract(value = "_ -> this")
        @NotNull Builder amount(int amount);

        @Contract(value = "_, _ -> this")
        <T> @NotNull Builder set(@NotNull DataComponent<T> component, T value);

        @Contract(value = "_ -> this")
        default @NotNull Builder set(@NotNull DataComponent<Unit> component) {
            return set(component, Unit.INSTANCE);
        }

        @Contract(value = "_ -> this")
        @NotNull Builder remove(@NotNull DataComponent<?> component);

        default @NotNull Builder customName(@NotNull Component customName) {
            return set(DataComponents.CUSTOM_NAME, customName);
        }

        default @NotNull Builder lore(@NotNull Component... lore) {
            return set(DataComponents.LORE, List.of(lore));
        }

        default @NotNull Builder lore(@NotNull List<Component> lore) {
            return set(DataComponents.LORE, lore);
        }

        default @NotNull Builder itemModel(@NotNull String model) {
            return set(DataComponents.ITEM_MODEL, model);
        }

        default @NotNull Builder customModelData(@NotNull List<Float> floats, @NotNull List<Boolean> flags, @NotNull List<String> strings, @NotNull List<RGBLike> colors) {
            return set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(floats, flags, strings, colors));
        }

        default @NotNull Builder glowing() {
            return set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

        default @NotNull Builder glowing(boolean glowing) {
            return set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, glowing);
        }

        default @NotNull Builder maxStackSize(int maxStackSize) {
            return set(DataComponents.MAX_STACK_SIZE, maxStackSize);
        }

        /**
         * <p>Hides all components which append tooltip lines using {@link DataComponents#TOOLTIP_DISPLAY}.
         * The result should be an item with only name and lore.</p>
         */
        @NotNull Builder hideExtraTooltip();

        @Contract(value = "_, _ -> this")
        <T> @NotNull Builder set(@NotNull Tag<T> tag, @Nullable T value);

        default <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
            set(tag, value);
        }

        @Contract(value = "-> new", pure = true)
        @NotNull ItemStack build();
    }
}
