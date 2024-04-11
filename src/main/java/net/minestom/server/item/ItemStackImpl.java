package net.minestom.server.item;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.item.component.ItemComponent;
import net.minestom.server.item.component.ItemComponentPatch;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

record ItemStackImpl(Material material, int amount, ItemComponentPatch components) implements ItemStack {

    static ItemStack create(Material material, int amount, ItemComponentPatch components) {
        if (amount <= 0) return AIR;
        return new ItemStackImpl(material, amount, components);
    }

    static ItemStack create(Material material, int amount) {
        return create(material, amount, ItemComponentPatch.EMPTY);
    }

    @Override
    public <T> @Nullable T get(@NotNull ItemComponent<T> component) {
        return components.get(component);
    }

    @Override
    public boolean has(@NotNull ItemComponent<?> component) {
        return components.has(component);
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
        int newAmount = amount() - amount;
        if (newAmount <= 0) return AIR;
        return withAmount(newAmount);
    }

    @Override
    public boolean isSimilar(@NotNull ItemStack itemStack) {
        return material == itemStack.material() && components.equals(((ItemStackImpl) itemStack).components);
    }

    @Override
    public @NotNull CompoundBinaryTag toItemNBT() {
//        CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder()
//                .putString("id", material.name())
//                .putByte("Count", (byte) amount);
//        CompoundBinaryTag nbt = meta.toNBT();
//        if (nbt.size() > 0) builder.put("tag", nbt);
//        return builder.build();
        //todo
    }

    @Contract(value = "-> new", pure = true)
    private @NotNull ItemStack.Builder builder() {
        return new Builder(material, amount, new ItemMetaImpl.Builder(meta.tagHandler().copy()));
    }

    // BEGIN DEPRECATED PRE-COMPONENT METHODS

    @Override public @NotNull ItemMeta meta() {
        return new ItemMetaImpl(components);
    }

    @Override
    public <T extends ItemMetaView<?>> @NotNull T meta(@NotNull Class<T> metaClass) {
        return ItemMetaViewImpl.construct(metaClass, meta);
    }

    @Override
    public @NotNull <V extends ItemMetaView.Builder, T extends ItemMetaView<V>> ItemStack withMeta(@NotNull Class<T> metaType,
                                                                                                   @NotNull Consumer<V> consumer) {
        return builder().meta(metaType, consumer).build();
    }

    @Override
    public @NotNull ItemStack withMeta(@NotNull Consumer<ItemMeta.@NotNull Builder> consumer) {
        return builder().meta(consumer).build();
    }

    @Override
    public @NotNull ItemStack withMeta(@NotNull ItemMeta meta) {
        return new ItemStackImpl(material, amount, (ItemMetaImpl) meta);
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

        @Override
        public ItemStack.@NotNull Builder meta(@NotNull TagHandler tagHandler) {
            return meta(tagHandler.asCompound());
        }

        @Override
        public ItemStack.@NotNull Builder meta(@NotNull CompoundBinaryTag compound) {
            components.set(ItemComponent.CUSTOM_DATA, new CustomData(compound));
            return this;
        }

        @Override
        public ItemStack.@NotNull Builder meta(@NotNull ItemMeta itemMeta) {
            this.components = itemMeta.components().builder();
            return this;
        }

        @Override
        public ItemStack.@NotNull Builder meta(@NotNull Consumer<ItemMeta.Builder> consumer) {
            consumer.accept(new ItemMetaImpl.Builder(components));
            return this;
        }

        @Override
        public <V extends ItemMetaView.Builder, T extends ItemMetaView<V>> ItemStack.@NotNull Builder meta(@NotNull Class<T> metaType,
                                                                                                           @NotNull Consumer<@NotNull V> itemMetaConsumer) {
            V view = ItemMetaViewImpl.constructBuilder(metaType, components);
            itemMetaConsumer.accept(view);
            return this;
        }
    }
}
