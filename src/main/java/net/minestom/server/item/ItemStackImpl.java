package net.minestom.server.item;

import net.minestom.server.item.rule.VanillaStackingRule;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTByte;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTString;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

record ItemStackImpl(Material material, int amount, ItemMeta meta) implements ItemStack {
    static final @NotNull VanillaStackingRule DEFAULT_STACKING_RULE = new VanillaStackingRule();

    static ItemStack create(Material material, int amount, ItemMeta meta) {
        if (amount <= 0) return AIR;
        return new ItemStackImpl(material, amount, meta);
    }

    static ItemStack create(Material material, int amount) {
        return create(material, amount, ItemMetaImpl.EMPTY);
    }

    @Override
    public <T extends ItemMetaView<?>> @NotNull T meta(@NotNull Class<T> metaClass) {
        return ItemMetaViewImpl.construct(metaClass, meta);
    }

    @Override
    public @NotNull ItemStack with(@NotNull Consumer<ItemStack.@NotNull Builder> builderConsumer) {
        ItemStack.Builder builder = builder();
        builderConsumer.accept(builder);
        return builder.build();
    }

    @Override
    public @NotNull <V extends ItemMetaView.Builder, T extends ItemMetaView<V>> ItemStack withMeta(@NotNull Class<T> metaType,
                                                                                                   @NotNull Consumer<V> metaConsumer) {
        return builder().meta(metaType, metaConsumer).build();
    }

    @Override
    public @NotNull ItemStack withMeta(@NotNull UnaryOperator<ItemMeta.@NotNull Builder> metaOperator) {
        return builder().meta(metaOperator).build();
    }

    @Override
    public @NotNull ItemStack consume(int amount) {
        return DEFAULT_STACKING_RULE.apply(this, currentAmount -> currentAmount - amount);
    }

    @Override
    public boolean isSimilar(@NotNull ItemStack itemStack) {
        return material == itemStack.material() && meta.equals(itemStack.meta());
    }

    @Override
    public @NotNull NBTCompound toItemNBT() {
        final NBTString material = NBT.String(material().name());
        final NBTByte amount = NBT.Byte(amount());
        final NBTCompound nbt = meta().toNBT();
        if (nbt.isEmpty()) return NBT.Compound(Map.of("id", material, "Count", amount));
        return NBT.Compound(Map.of("id", material, "Count", amount, "tag", nbt));
    }

    @Contract(value = "-> new", pure = true)
    private @NotNull ItemStack.Builder builder() {
        return new Builder(material, amount, new ItemMetaImpl.Builder(TagHandler.fromCompound(meta.toNBT())));
    }

    static final class Builder implements ItemStack.Builder {
        final Material material;
        int amount;
        ItemMeta.Builder metaBuilder;

        Builder(Material material, int amount, ItemMeta.Builder metaBuilder) {
            this.material = material;
            this.amount = amount;
            this.metaBuilder = metaBuilder;
        }

        Builder(Material material, int amount) {
            this(material, amount, new ItemMetaImpl.Builder(TagHandler.newHandler()));
        }

        @Override
        public ItemStack.@NotNull Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        @Override
        public ItemStack.@NotNull Builder meta(@NotNull TagHandler tagHandler) {
            return metaBuilder(new ItemMetaImpl.Builder(tagHandler.copy()));
        }

        @Override
        public ItemStack.@NotNull Builder meta(@NotNull NBTCompound compound) {
            return metaBuilder(new ItemMetaImpl.Builder(TagHandler.fromCompound(compound)));
        }

        @Override
        public ItemStack.@NotNull Builder meta(@NotNull ItemMeta itemMeta) {
            final TagHandler tagHandler = ((ItemMetaImpl) itemMeta).tagHandler();
            return metaBuilder(new ItemMetaImpl.Builder(tagHandler.copy()));
        }

        @Override
        public ItemStack.@NotNull Builder meta(@NotNull UnaryOperator<ItemMeta.Builder> consumer) {
            this.metaBuilder = consumer.apply(metaBuilder);
            return this;
        }

        @Override
        public <V extends ItemMetaView.Builder, T extends ItemMetaView<V>> ItemStack.@NotNull Builder meta(@NotNull Class<T> metaType,
                                                                                                           @NotNull Consumer<@NotNull V> itemMetaConsumer) {
            V view = ItemMetaViewImpl.constructBuilder(metaType, metaBuilder.tagHandler());
            itemMetaConsumer.accept(view);
            return this;
        }

        @Override
        public @NotNull ItemStack build() {
            return ItemStackImpl.create(material, amount, metaBuilder.build());
        }

        private ItemStack.@NotNull Builder metaBuilder(@NotNull ItemMeta.Builder builder) {
            this.metaBuilder = builder;
            return this;
        }
    }
}
