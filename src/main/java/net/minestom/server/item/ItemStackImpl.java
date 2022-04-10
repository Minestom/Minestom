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

record ItemStackImpl(@NotNull Material material, int amount,
                     @NotNull ItemMeta meta) implements ItemStack {
    static final @NotNull VanillaStackingRule DEFAULT_STACKING_RULE = new VanillaStackingRule();

    @Override
    public @NotNull ItemStack with(@NotNull Consumer<ItemStack.@NotNull Builder> builderConsumer) {
        var builder = builder();
        builderConsumer.accept(builder);
        return builder.build();
    }

    @Override
    public @NotNull <T extends ItemMeta.Builder> ItemStack withMeta(@NotNull Class<T> metaType,
                                                                    @NotNull Consumer<T> metaConsumer) {
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
        return material == itemStack.material() &&
                meta.equals(itemStack.meta());
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
        public ItemStack.@NotNull Builder meta(@NotNull ItemMeta itemMeta) {
            this.metaBuilder = new ItemMetaImpl.Builder(TagHandler.fromCompound(itemMeta.toNBT()));
            return this;
        }

        @Override
        public <T extends ItemMeta.Builder> ItemStack.@NotNull Builder meta(@NotNull UnaryOperator<ItemMeta.Builder> consumer) {
            this.metaBuilder = consumer.apply(metaBuilder);
            return this;
        }

        @Override
        public <T extends ItemMeta.Builder> ItemStack.@NotNull Builder meta(@NotNull Class<T> metaType, @NotNull Consumer<@NotNull T> itemMetaConsumer) {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public @NotNull ItemStack build() {
            return new ItemStackImpl(material, amount, metaBuilder.build());
        }
    }
}
