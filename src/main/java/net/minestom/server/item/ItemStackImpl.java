package net.minestom.server.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

record ItemStackImpl(Material material, int amount, ItemComponentPatch components) implements ItemStack {

    static final NetworkBuffer.Type<ItemStack> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ItemStack value) {
            if (value.isAir()) {
                buffer.write(NetworkBuffer.VAR_INT, 0);
                return;
            }

            buffer.write(NetworkBuffer.VAR_INT, value.amount());
            buffer.write(NetworkBuffer.VAR_INT, value.material().id());
            buffer.write(ItemComponentPatch.NETWORK_TYPE, ((ItemStackImpl) value).components);
        }

        @Override
        public ItemStack read(@NotNull NetworkBuffer buffer) {
            int amount = buffer.read(NetworkBuffer.VAR_INT);
            if (amount <= 0) return ItemStack.AIR;
            Material material = Material.fromId(buffer.read(NetworkBuffer.VAR_INT));
            ItemComponentPatch components = buffer.read(ItemComponentPatch.NETWORK_TYPE);
            return new ItemStackImpl(material, amount, components);
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

    static ItemStack create(Material material, int amount, ItemComponentPatch components) {
        if (amount <= 0) return AIR;
        return new ItemStackImpl(material, amount, components);
    }

    static ItemStack create(Material material, int amount) {
        return create(material, amount, ItemComponentPatch.EMPTY);
    }

    public ItemStackImpl {
        Check.notNull(material, "Material cannot be null");
    }

    @Override
    public <T> @Nullable T get(@NotNull ItemComponent<T> component) {
        return components.get(material.prototype(), component);
    }

    @Override
    public boolean has(@NotNull ItemComponent<?> component) {
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
    public @NotNull <T> ItemStack with(@NotNull ItemComponent<T> component, T value) {
        return new ItemStackImpl(material, amount, components.with(component, value));
    }

    @Override
    public @NotNull ItemStack without(@NotNull ItemComponent<?> component) {
        return new ItemStackImpl(material, amount, components.without(component));
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
        return new Builder(material, amount, components.builder());
    }

    private static @NotNull ItemStack fromCompound(@NotNull CompoundBinaryTag tag) {
        String id = tag.getString("id");
        Material material = Material.fromNamespaceId(id);
        Check.notNull(material, "Unknown material: {0}", id);
        int count = tag.getInt("count", 1);
        ItemComponentPatch patch = ItemComponentPatch.NBT_TYPE.read(tag.getCompound("components"));
        return new ItemStackImpl(material, count, patch);
    }

    private static @NotNull CompoundBinaryTag toCompound(@NotNull ItemStack itemStack) {
        CompoundBinaryTag.Builder tag = CompoundBinaryTag.builder();
        tag.putString("id", itemStack.material().name());
        tag.putInt("count", itemStack.amount());

        CompoundBinaryTag components = (CompoundBinaryTag) ItemComponentPatch.NBT_TYPE.write(((ItemStackImpl) itemStack).components);
        if (components.size() > 0) tag.put("components", components);

        return tag.build();
    }

    static final class Builder implements ItemStack.Builder {
        final Material material;
        int amount;
        ItemComponentPatch.Builder components;

        Builder(Material material, int amount, ItemComponentPatch.Builder components) {
            this.material = material;
            this.amount = amount;
            this.components = components;
        }

        Builder(Material material, int amount) {
            this.material = material;
            this.amount = amount;
            this.components = new ItemComponentPatch.Builder(new Int2ObjectArrayMap<>());
        }

        @Override
        public ItemStack.@NotNull Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        @Override
        public <T> ItemStack.@NotNull Builder set(@NotNull ItemComponent<T> component, T value) {
            components.set(component, value);
            return this;
        }

        @Override
        public ItemStack.@NotNull Builder remove(@NotNull ItemComponent<?> component) {
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
