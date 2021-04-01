package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.metadata.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ItemBuilder {

    private final Material material;
    private int amount;
    private Component displayName;
    private List<Component> lore;

    protected ItemBuilder(@NotNull Material material) {
        this.material = material;
        this.amount = 0;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    @Contract(value = "_, _ -> this")
    public <T extends ItemMeta> @NotNull ItemBuilder meta(Class<T> metaType, Consumer<T> metaConsumer) {
        // TODO
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemBuilder displayName(@Nullable Component displayName) {
        this.displayName = displayName;
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemBuilder lore(List<@NotNull Component> lore) {
        this.lore = Collections.unmodifiableList(lore);
        return this;
    }

    @Contract(value = "_ -> this")
    public @NotNull ItemBuilder lore(Component... lore) {
        return lore(Arrays.asList(lore));
    }

    @Contract(value = "-> new", pure = true)
    public @NotNull Item build() {
        return new Item(material, amount, displayName, lore);
    }

}
