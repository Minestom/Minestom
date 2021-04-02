package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.rule.VanillaStackingRule;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;

public class ItemStack {

    public static final ItemStack AIR = ItemStack.of(Material.AIR);

    private final UUID uuid = UUID.randomUUID();
    private final StackingRule stackingRule = new VanillaStackingRule(64);

    private final Material material;
    private final int amount;
    private final ItemMeta meta;

    protected ItemStack(@NotNull Material material, int amount, ItemMeta meta) {
        this.material = material;
        this.amount = amount;
        this.meta = meta;
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ItemBuilder builder(@NotNull Material material) {
        return new ItemBuilder(material);
    }

    @Contract(value = "_ ,_ -> new", pure = true)
    public static @NotNull ItemStack of(@NotNull Material material, int amount) {
        return builder(material).amount(amount).build();
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ItemStack of(@NotNull Material material) {
        return of(material, 1);
    }

    @Contract(pure = true)
    public @NotNull UUID getUuid() {
        return uuid;
    }

    @Contract(pure = true)
    public @NotNull Material getMaterial() {
        return material;
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull ItemStack with(@NotNull Consumer<@NotNull ItemBuilder> builderConsumer) {
        var builder = builder();
        builderConsumer.accept(builder);
        return builder.build();
    }

    @Contract(pure = true)
    public int getAmount() {
        return amount;
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull ItemStack withAmount(int amount) {
        return builder().amount(amount).build();
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull ItemStack withAmount(@NotNull IntUnaryOperator intUnaryOperator) {
        return withAmount(intUnaryOperator.applyAsInt(amount));
    }

    @Contract(value = "_, _ -> new", pure = true)
    public <T extends ItemMetaBuilder, U extends ItemMetaBuilder.Provider<T>> @NotNull ItemStack withMeta(Class<U> metaType, Consumer<T> metaConsumer) {
        return builder().meta(metaType, metaConsumer).build();
    }

    @Contract(pure = true)
    public @Nullable Component getDisplayName() {
        return meta.getDisplayName();
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull ItemStack withDisplayName(@Nullable Component displayName) {
        return builder().displayName(displayName).build();
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull ItemStack withDisplayName(@NotNull UnaryOperator<@Nullable Component> componentUnaryOperator) {
        return withDisplayName(componentUnaryOperator.apply(getDisplayName()));
    }

    @Contract(pure = true)
    public @Nullable List<@NotNull Component> getLore() {
        return meta.getLore();
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull ItemStack withLore(@Nullable List<@NotNull Component> lore) {
        return builder().lore(lore).build();
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull ItemStack withLore(@NotNull UnaryOperator<@Nullable List<@NotNull Component>> loreUnaryOperator) {
        return withLore(loreUnaryOperator.apply(getLore()));
    }

    @Contract(pure = true)
    public @NotNull StackingRule getStackingRule() {
        return stackingRule;
    }

    @Contract(pure = true)
    public @NotNull ItemMeta getMeta() {
        return meta;
    }

    @Contract(pure = true)
    public boolean isAir() {
        return equals(AIR);
    }

    @Contract(pure = true)
    public boolean isSimilar(@NotNull ItemStack itemStack) {
        return material.equals(itemStack.material) &&
                meta.equals(itemStack.meta);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemStack itemStack = (ItemStack) o;
        if (uuid.equals(itemStack.uuid)) return true;

        if (amount != itemStack.amount) return false;
        if (!stackingRule.equals(itemStack.stackingRule)) return false;
        if (material != itemStack.material) return false;
        return meta.equals(itemStack.meta);
    }

    @Override
    public int hashCode() {
        int result = stackingRule.hashCode();
        result = 31 * result + material.hashCode();
        result = 31 * result + amount;
        result = 31 * result + meta.hashCode();
        return result;
    }

    @Contract(value = "-> new", pure = true)
    protected @NotNull ItemBuilder builder() {
        return new ItemBuilder(material, meta.builder())
                .amount(amount);
    }
}
