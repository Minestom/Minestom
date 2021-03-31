package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ItemBuilder {

    private final Material material;
    private int amount;
    private Component displayName;
    private List<Component> lore;

    protected ItemBuilder(@NotNull Material material) {
        this.material = material;
        this.amount = 0;
    }

    @NotNull
    public ItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    @NotNull
    public ItemBuilder displayName(Component displayName) {
        this.displayName = displayName;
        return this;
    }

    @NotNull
    public ItemBuilder lore(List<Component> lore) {
        this.lore = Collections.unmodifiableList(lore);
        return this;
    }

    @NotNull
    public ItemBuilder lore(Component... lore) {
        return lore(Arrays.asList(lore));
    }

    @NotNull
    public Item build() {
        return new Item(material, amount, displayName, lore);
    }

}
