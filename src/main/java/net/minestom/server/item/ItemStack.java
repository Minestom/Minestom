package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.minestom.server.item.rule.VanillaStackingRule;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.utils.NBTUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;

/**
 * Represents an immutable item to be placed inside {@link net.minestom.server.inventory.PlayerInventory},
 * {@link net.minestom.server.inventory.Inventory} or even on the ground {@link net.minestom.server.entity.ItemEntity}.
 * <p>
 * An item stack cannot be null, {@link ItemStack#AIR} should be used instead.
 */
public final class ItemStack implements TagReadable, HoverEventSource<HoverEvent.ShowItem> {

    private static final @NotNull VanillaStackingRule DEFAULT_STACKING_RULE = new VanillaStackingRule();

    /**
     * Constant AIR item. Should be used instead of 'null'.
     */
    public static final @NotNull ItemStack AIR = ItemStack.of(Material.AIR);

    private final StackingRule stackingRule;

    private final Material material;
    private final int amount;
    private final ItemMeta meta;

    ItemStack(@NotNull Material material, int amount,
              @NotNull ItemMeta meta,
              @Nullable StackingRule stackingRule) {
        this.material = material;
        this.amount = amount;
        this.meta = meta;
        this.stackingRule = Objects.requireNonNullElse(stackingRule, DEFAULT_STACKING_RULE);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ItemStackBuilder builder(@NotNull Material material) {
        return new ItemStackBuilder(material);
    }

    @Contract(value = "_ ,_ -> new", pure = true)
    public static @NotNull ItemStack of(@NotNull Material material, int amount) {
        return builder(material).amount(amount).build();
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ItemStack of(@NotNull Material material) {
        return of(material, 1);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public static @NotNull ItemStack fromNBT(@NotNull Material material, @Nullable NBTCompound nbtCompound, int amount) {
        ItemMetaBuilder builder = ItemStackBuilder.getMetaBuilder(material);
        if (nbtCompound != null) ItemMetaBuilder.resetMeta(builder, nbtCompound);
        return new ItemStack(material, amount, builder.build(), null);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull ItemStack fromNBT(@NotNull Material material, @Nullable NBTCompound nbtCompound) {
        return fromNBT(material, nbtCompound, 1);
    }

    /**
     * Converts this item to an NBT tag containing the id (material), count (amount), and tag (meta).
     *
     * @param nbtCompound The nbt representation of the item
     */
    @ApiStatus.Experimental
    public static @NotNull ItemStack fromItemNBT(@NotNull NBTCompound nbtCompound) {
        String id = nbtCompound.getString("id");
        Check.notNull(id, "Item NBT must contain an id field.");
        Material material = Material.fromNamespaceId(id);
        Check.notNull(material, "Unknown material: {0}", id);

        Byte amount = nbtCompound.getByte("Count");
        return fromNBT(material,
                nbtCompound.getCompound("tag"),
                amount == null ? 1 : amount);
    }

    @Contract(pure = true)
    public @NotNull Material getMaterial() {
        return material;
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull ItemStack with(@NotNull Consumer<@NotNull ItemStackBuilder> builderConsumer) {
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
        if (amount < 1) return AIR;
        return new ItemStack(material, amount, meta, stackingRule);
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull ItemStack withAmount(@NotNull IntUnaryOperator intUnaryOperator) {
        return withAmount(intUnaryOperator.applyAsInt(amount));
    }

    @ApiStatus.Experimental
    @Contract(value = "_, -> new", pure = true)
    public @NotNull ItemStack consume(int amount) {
        return stackingRule.apply(this, currentAmount -> currentAmount - amount);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public <T extends ItemMetaBuilder, U extends ItemMetaBuilder.Provider<T>> @NotNull ItemStack withMeta(Class<U> metaType, Consumer<T> metaConsumer) {
        return builder().meta(metaType, metaConsumer).build();
    }

    @Contract(value = "_ -> new", pure = true)
    public <T extends ItemMetaBuilder> @NotNull ItemStack withMeta(@NotNull UnaryOperator<@NotNull T> metaOperator) {
        return builder().meta(metaOperator).build();
    }

    @ApiStatus.Experimental
    @Contract(value = "_ -> new", pure = true)
    public @NotNull ItemStack withMeta(@NotNull ItemMeta meta) {
        return new ItemStack(material, amount, meta, stackingRule);
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
    public @NotNull List<@NotNull Component> getLore() {
        return meta.getLore();
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull ItemStack withLore(@NotNull List<? extends Component> lore) {
        return builder().lore(lore).build();
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull ItemStack withLore(@NotNull UnaryOperator<@NotNull List<@NotNull Component>> loreUnaryOperator) {
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
        return material == Material.AIR;
    }

    @Contract(pure = true)
    public boolean isSimilar(@NotNull ItemStack itemStack) {
        return material == itemStack.material &&
                meta.equals(itemStack.meta);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemStack itemStack = (ItemStack) o;

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

    @Override
    public String toString() {
        return "ItemStack{" +
                "stackingRule=" + stackingRule +
                ", material=" + material +
                ", amount=" + amount +
                ", meta=" + meta +
                '}';
    }

    @Contract(value = "_, _ -> new", pure = true)
    public <T> @NotNull ItemStack withTag(@NotNull Tag<T> tag, @Nullable T value) {
        return builder().meta(metaBuilder -> metaBuilder.set(tag, value)).build();
    }

    @Override
    public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
        return meta.getTag(tag);
    }

    @Override
    public @NotNull HoverEvent<HoverEvent.ShowItem> asHoverEvent(@NotNull UnaryOperator<HoverEvent.ShowItem> op) {
        return HoverEvent.showItem(op.apply(HoverEvent.ShowItem.of(this.material,
                this.amount,
                NBTUtils.asBinaryTagHolder(this.meta.toNBT()))));
    }

    /**
     * Converts this item to an NBT tag containing the id (material), count (amount), and tag (meta)
     *
     * @return The nbt representation of the item
     */
    @ApiStatus.Experimental
    public @NotNull NBTCompound toItemNBT() {
        return NBT.Compound(Map.of(
                "id", NBT.String(getMaterial().name()),
                "Count", NBT.Byte(getAmount()),
                "tag", getMeta().toNBT()));
    }

    @Contract(value = "-> new", pure = true)
    private @NotNull ItemStackBuilder builder() {
        return new ItemStackBuilder(material, meta.builder())
                .amount(amount)
                .stackingRule(stackingRule);
    }
}
