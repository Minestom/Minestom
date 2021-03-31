package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;

public class Item {

    private final Material material;
    private final int amount;
    private final Component displayName;
    private final List<Component> lore;

    protected Item(Material material, int amount, Component displayName, List<Component> lore) {
        this.material = material;
        this.amount = amount;
        this.displayName = displayName;
        this.lore = lore;
    }

    @NotNull
    public static ItemBuilder builder(@NotNull Material material) {
        return new ItemBuilder(material);
    }

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

    public Component getDisplayName() {
        return displayName;
    }

    public List<Component> getLore() {
        return lore;
    }
}
