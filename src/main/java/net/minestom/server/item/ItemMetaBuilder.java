package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class ItemMetaBuilder implements Cloneable {

    protected Component displayName;
    protected List<Component> lore;
    protected Map<Enchantment, Short> enchantmentMap = new HashMap<>();

    protected ItemMetaBuilder() {
    }

    public @NotNull ItemMetaBuilder displayName(@Nullable Component displayName) {
        this.displayName = displayName;
        return this;
    }

    public @NotNull ItemMetaBuilder lore(List<@NotNull Component> lore) {
        this.lore = lore;
        return this;
    }

    public @NotNull ItemMetaBuilder lore(Component... lore) {
        lore(Arrays.asList(lore));
        return this;
    }

    public @NotNull ItemMetaBuilder enchantments(@NotNull Map<Enchantment, Short> enchantments) {
        this.enchantmentMap.putAll(enchantments);
        return this;
    }

    public @NotNull ItemMetaBuilder enchantment(@NotNull Enchantment enchantment, short level) {
        this.enchantmentMap.put(enchantment, level);
        return this;
    }

    public @NotNull ItemMetaBuilder clearEnchantment() {
        this.enchantmentMap.clear();
        return this;
    }

    public abstract @NotNull ItemMeta build();

    protected abstract void deepClone(@NotNull ItemMetaBuilder metaBuilder);

    @Override
    protected ItemMetaBuilder clone() {
        try {
            var builder = (ItemMetaBuilder) super.clone();
            builder.displayName = displayName;
            builder.lore = new ArrayList<>(lore);
            builder.enchantmentMap = new HashMap<>(enchantmentMap);
            deepClone(builder);
            return builder;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("Weird thing happened");
        }
    }

    public interface Provider<T> {
    }

}
