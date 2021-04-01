package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;

public class Item {

    private final Material material;
    private final int amount;
    private final ItemMeta meta;

    protected Item(@NotNull Material material, int amount, ItemMeta meta) {
        this.material = material;
        this.amount = amount;
        this.meta = meta;
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ItemBuilder builder(@NotNull Material material) {
        return new ItemBuilder(material);
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull Item with(@NotNull Consumer<@NotNull ItemBuilder> builderConsumer) {
        var builder = builder();
        builderConsumer.accept(builder);
        return builder.build();
    }

    @Contract(pure = true)
    public int getAmount() {
        return amount;
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull Item withAmount(int amount) {
        return builder().amount(amount).build();
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull Item withAmount(@NotNull IntUnaryOperator intUnaryOperator) {
        return withAmount(intUnaryOperator.applyAsInt(amount));
    }

    @Contract(value = "_, _ -> new", pure = true)
    public <T extends ItemMetaBuilder> @NotNull Item withMeta(Class<T> metaType, Consumer<T> metaConsumer) {
        return builder().meta(metaType, metaConsumer).build();
    }

    @Contract(pure = true)
    public @Nullable Component getDisplayName() {
        return meta.getDisplayName();
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull Item withDisplayName(@Nullable Component displayName) {
        return builder().displayName(displayName).build();
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull Item withDisplayName(@NotNull UnaryOperator<@Nullable Component> componentUnaryOperator) {
        return withDisplayName(componentUnaryOperator.apply(getDisplayName()));
    }

    @Contract(pure = true)
    public @Nullable List<@NotNull Component> getLore() {
        return meta.getLore();
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull Item withLore(@Nullable List<@NotNull Component> lore) {
        return builder().lore(lore).build();
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull Item withLore(@NotNull UnaryOperator<@Nullable List<@NotNull Component>> loreUnaryOperator) {
        return withLore(loreUnaryOperator.apply(getLore()));
    }

    @Contract(value = "-> new", pure = true)
    protected @NotNull ItemBuilder builder() {
        return new ItemBuilder(material, meta.builder())
                .amount(amount);
    }
}
