package net.minestom.server.item;

import net.kyori.adventure.text.Component;
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

    @NotNull
    public static ItemBuilder builder(@NotNull Material material) {
        return new ItemBuilder(material);
    }

    @NotNull
    public ItemBuilder builder() {
        return new ItemBuilder(material)
                .amount(amount)
                .displayName(displayName)
                .lore(lore);
    }

    @NotNull
    public Item with(@NotNull Consumer<ItemBuilder> builderConsumer) {
        var builder = builder();
        builderConsumer.accept(builder);
        return builder.build();
    }

    public int getAmount() {
        return amount;
    }

    @NotNull
    public Item withAmount(int amount) {
        return builder().amount(amount).build();
    }

    @NotNull
    public Item withAmount(@NotNull IntUnaryOperator intUnaryOperator) {
        return withAmount(intUnaryOperator.applyAsInt(amount));
    }

    @Nullable
    public Component getDisplayName() {
        return displayName;
    }

    @NotNull
    public Item withDisplayName(@Nullable Component displayName) {
        return builder().displayName(displayName).build();
    }

    @NotNull
    public Item withDisplayName(@NotNull UnaryOperator<@Nullable Component> componentUnaryOperator) {
        return withDisplayName(componentUnaryOperator.apply(displayName));
    }

    @Nullable
    public List<Component> getLore() {
        return lore;
    }

    @NotNull
    public Item withLore(@Nullable List<@NotNull Component> lore) {
        return builder().lore(lore).build();
    }

    @NotNull
    public Item withLore(@NotNull UnaryOperator<@Nullable List<Component>> loreUnaryOperator) {
        return withLore(loreUnaryOperator.apply(lore));
    }
}
