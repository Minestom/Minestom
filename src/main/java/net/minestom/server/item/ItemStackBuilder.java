package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.meta.CompassMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class ItemStackBuilder {

    private final Material material;
    private int amount;
    protected ItemMetaBuilder metaBuilder;
    protected ItemStoreBuilder storeBuilder;

    protected ItemStackBuilder(@NotNull Material material, @NotNull ItemMetaBuilder metaBuilder, @NotNull ItemStoreBuilder storeBuilder) {
        this.material = material;
        this.amount = 1;
        this.metaBuilder = metaBuilder;
        this.storeBuilder = storeBuilder;
    }

    protected ItemStackBuilder(@NotNull Material material) {
        // TODO: meta depends on material
        this(material, new CompassMeta.Builder(), new ItemStoreBuilder());
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemStackBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemStackBuilder meta(@NotNull ItemMeta itemMeta) {
        this.metaBuilder = itemMeta.builder();
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemStackBuilder meta(@NotNull UnaryOperator<@NotNull ItemMetaBuilder> itemMetaConsumer) {
        this.metaBuilder = itemMetaConsumer.apply(metaBuilder);
        return this;
    }

    @Contract(value = "_, _ -> this")
    public <T extends ItemMetaBuilder, U extends ItemMetaBuilder.Provider<T>> @NotNull ItemStackBuilder meta(@NotNull Class<U> metaType, @NotNull Consumer<@NotNull T> itemMetaConsumer) {
        itemMetaConsumer.accept((T) metaBuilder);
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemStackBuilder displayName(@Nullable Component displayName) {
        this.metaBuilder.displayName(displayName);
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemStackBuilder lore(List<@NotNull Component> lore) {
        this.metaBuilder.lore(lore);
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemStackBuilder lore(Component... lore) {
        this.metaBuilder.lore(lore);
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemStackBuilder store(@NotNull ItemStore store) {
        this.storeBuilder = store.builder();
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemStackBuilder store(@NotNull Consumer<@NotNull ItemStoreBuilder> consumer) {
        consumer.accept(storeBuilder);
        return this;
    }

    @Contract(value = "-> new", pure = true)
    public @NotNull ItemStack build() {
        return new ItemStack(material, amount, metaBuilder.build(), storeBuilder.build());
    }

}
