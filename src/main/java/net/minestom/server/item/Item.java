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
    private final Component displayName;
    private final List<Component> lore;

    protected Item(@NotNull Material material, int amount,
                   @Nullable Component displayName, @Nullable List<Component> lore) {
        this.material = material;
        this.amount = amount;
        this.displayName = displayName;
        this.lore = lore;
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ItemBuilder builder(@NotNull Material material) {
        return new ItemBuilder(material);
    }

    @Contract(value = "-> new", pure = true)
    public @NotNull ItemBuilder builder() {
        return new ItemBuilder(material)
                .amount(amount)
                .displayName(displayName)
                .lore(lore);
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

    @Contract(pure = true)
    public @Nullable Component getDisplayName() {
        return displayName;
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull Item withDisplayName(@Nullable Component displayName) {
        return builder().displayName(displayName).build();
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull Item withDisplayName(@NotNull UnaryOperator<@Nullable Component> componentUnaryOperator) {
        return withDisplayName(componentUnaryOperator.apply(displayName));
    }

    @Contract(pure = true)
    public @Nullable List<@NotNull Component> getLore() {
        return lore;
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull Item withLore(@Nullable List<@NotNull Component> lore) {
        return builder().lore(lore).build();
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull Item withLore(@NotNull UnaryOperator<@Nullable List<@NotNull Component>> loreUnaryOperator) {
        return withLore(loreUnaryOperator.apply(lore));
    }
}
