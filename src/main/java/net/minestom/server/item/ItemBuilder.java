package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.meta.CompassMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ItemBuilder {

    private final Material material;
    private int amount;
    protected ItemMetaBuilder metaBuilder;

    protected ItemBuilder(@NotNull Material material, @NotNull ItemMetaBuilder metaBuilder) {
        this.material = material;
        this.amount = 0;
        this.metaBuilder = metaBuilder;
    }

    protected ItemBuilder(@NotNull Material material) {
        // TODO: meta depends on material
        this(material, new CompassMeta.Builder());
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemBuilder meta(@NotNull ItemMeta itemMeta) {
        this.metaBuilder = itemMeta.builder();
        return this;
    }

    @Contract(value = "_, _ -> this")
    public <T extends ItemMetaBuilder, U extends ItemMetaBuilder.Provider<T>> @NotNull ItemBuilder meta(Class<U> metaType, Consumer<T> itemMetaConsumer) {
        itemMetaConsumer.accept((T) metaBuilder);
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemBuilder displayName(@Nullable Component displayName) {
        this.metaBuilder.displayName(displayName);
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemBuilder lore(List<@NotNull Component> lore) {
        this.metaBuilder.lore(lore);
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemBuilder lore(Component... lore) {
        this.metaBuilder.lore(lore);
        return this;
    }

    @Contract(value = "-> new", pure = true)
    public @NotNull Item build() {
        return new Item(material, amount, metaBuilder.build());
    }

}
