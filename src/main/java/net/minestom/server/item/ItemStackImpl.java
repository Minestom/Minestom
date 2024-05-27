package net.minestom.server.item;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

record ItemStackImpl(Material material, int amount, DataComponentMap components) implements ItemStack {

    static final NetworkBuffer.Type<ItemStack> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ItemStack value) {
            if (value.isAir()) {
                buffer.write(NetworkBuffer.VAR_INT, 0);
                return;
            }

            buffer.write(NetworkBuffer.VAR_INT, value.amount());
            buffer.write(NetworkBuffer.VAR_INT, value.material().id());
            buffer.write(DataComponentMap.PATCH_NETWORK_TYPE, ((ItemStackImpl) value).components);
        }

        @Override
        public ItemStack read(@NotNull NetworkBuffer buffer) {
            int amount = buffer.read(NetworkBuffer.VAR_INT);
            if (amount <= 0) return ItemStack.AIR;
            Material material = Material.fromId(buffer.read(NetworkBuffer.VAR_INT));
            DataComponentMap components = buffer.read(DataComponentMap.PATCH_NETWORK_TYPE);
            return create(material, amount, components);
        }
    };
    static final NetworkBuffer.Type<ItemStack> STRICT_NETWORK_TYPE = NETWORK_TYPE.map(itemStack -> {
        Check.argCondition(itemStack.amount() == 0 || itemStack.isAir(), "ItemStack cannot be empty");
        return itemStack;
    }, itemStack -> {
        Check.argCondition(itemStack.amount() == 0 || itemStack.isAir(), "ItemStack cannot be empty");
        return itemStack;
    });
    static final BinaryTagSerializer<ItemStack> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(ItemStackImpl::fromCompound, ItemStackImpl::toCompound);

    static ItemStack create(Material material, int amount, DataComponentMap components) {
        if (amount <= 0) return AIR;
        return new ItemStackImpl(material, amount, components);
    }

    static ItemStack create(Material material, int amount) {
        return create(material, amount, DataComponentMap.EMPTY);
    }

    public ItemStackImpl {
        Check.notNull(material, "Material cannot be null");

        // It is relevant to create the minimal diff of the prototype so that #isSimilar returns consistent
        // results for ItemStacks which would resolve to the same thing. For example, consider two items
        // (name indicating prototype, brackets showing the components given during construction):
        // 1: apple[max_stack_size=64, custom_name=Hello]
        // 2: apple[custom_name=Hello]
        // After resolution the first set of components would turn into the second one because apple already has a
        // max stack size of 64. If we did not do this, #isSimilar would return false for these two items because of
        // their different patches.
        // It is worth noting that the client would handle both cases perfectly fine.
        components = DataComponentMap.diff(material.prototype(), components);
    }

    @Override
    public <T> @Nullable T get(@NotNull DataComponent<T> component) {
        return components.get(material.prototype(), component);
    }

    @Override
    public boolean has(@NotNull DataComponent<?> component) {
        return components.has(material.prototype(), component);
    }

    @Override
    public @NotNull ItemStack with(@NotNull Consumer<ItemStack.@NotNull Builder> consumer) {
        ItemStack.Builder builder = builder();
        consumer.accept(builder);
        return builder.build();
    }

    @Override
    public @NotNull ItemStack withMaterial(@NotNull Material material) {
        return new ItemStackImpl(material, amount, components);
    }

    @Override
    public @NotNull ItemStack withAmount(int amount) {
        if (amount <= 0) return ItemStack.AIR;
        return create(material, amount, components);
    }

    @Override
    public @NotNull <T> ItemStack with(@NotNull DataComponent<T> component, @NotNull T value) {
        return new ItemStackImpl(material, amount, components.set(component, value));
    }

    @Override
    public @NotNull ItemStack without(@NotNull DataComponent<?> component) {
        // We can be slightly smart here. If the component is not present, this will always be a noop.
        // No need to make a new patch with the removal only for it to be removed again when doing a diff.
        if (get(component) == null) return this;
        return new ItemStackImpl(material, amount, components.remove(component));
    }

    @Override
    public @NotNull ItemStack consume(int amount) {
        return withAmount(amount() - amount);
    }

    @Override
    public boolean isSimilar(@NotNull ItemStack itemStack) {
        return material == itemStack.material() && components.equals(((ItemStackImpl) itemStack).components);
    }

    @Override
    public @NotNull CompoundBinaryTag toItemNBT() {
        return (CompoundBinaryTag) NBT_TYPE.write(this);
    }

    @Contract(value = "-> new", pure = true)
    private @NotNull ItemStack.Builder builder() {
        return new Builder(material, amount, components.toBuilder());
    }

    private static @NotNull ItemStack fromCompound(@NotNull CompoundBinaryTag tag) {
        String id = tag.getString("id");
        Material material = Material.fromNamespaceId(id);
        Check.notNull(material, "Unknown material: {0}", id);
        int count = tag.getInt("count", 1);
        DataComponentMap patch = DataComponentMap.PATCH_NBT_TYPE.read(tag.getCompound("components"));
        return new ItemStackImpl(material, count, patch);
    }

    private static @NotNull CompoundBinaryTag toCompound(@NotNull ItemStack itemStack) {
        CompoundBinaryTag.Builder tag = CompoundBinaryTag.builder();
        tag.putString("id", itemStack.material().name());
        tag.putInt("count", itemStack.amount());

        CompoundBinaryTag components = (CompoundBinaryTag) DataComponentMap.PATCH_NBT_TYPE.write(((ItemStackImpl) itemStack).components);
        if (components.size() > 0) tag.put("components", components);

        return tag.build();
    }

    static final class Builder implements ItemStack.Builder {
        final Material material;
        int amount;
        DataComponentMap.Builder components;

        Builder(Material material, int amount, DataComponentMap.Builder components) {
            this.material = material;
            this.amount = amount;
            this.components = components;
        }

        Builder(Material material, int amount) {
            this.material = material;
            this.amount = amount;
            this.components = DataComponentMap.builder();
        }

        @Override
        public ItemStack.@NotNull Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        @Override
        public <T> ItemStack.@NotNull Builder set(@NotNull DataComponent<T> component, T value) {
            components.set(component, value);
            return this;
        }

        @Override
        public ItemStack.@NotNull Builder remove(@NotNull DataComponent<?> component) {
            components.remove(component);
            return this;
        }

        @Override
        public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
            components.set(ItemComponent.CUSTOM_DATA, components.get(ItemComponent.CUSTOM_DATA, CustomData.EMPTY).withTag(tag, value));
        }

        @Override
        public @NotNull ItemStack build() {
            return ItemStackImpl.create(material, amount, components.build());
        }

    }
}
