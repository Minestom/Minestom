package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class ItemMetaBuilder implements Cloneable {

    protected Component displayName;
    protected List<Component> lore;

    public void displayName(@Nullable Component displayName) {
        this.displayName = displayName;
    }

    public void lore(List<@NotNull Component> lore) {
        this.lore = Collections.unmodifiableList(lore);
    }

    public void lore(Component... lore) {
        lore(Arrays.asList(lore));
    }

    public abstract @NotNull ItemMeta build();

    protected abstract void deepClone(@NotNull ItemMetaBuilder metaBuilder);

    @Override
    protected ItemMetaBuilder clone() {
        try {
            var builder = (ItemMetaBuilder) super.clone();
            deepClone(builder);
            return builder;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException("Weird thing happened");
        }
    }

}
