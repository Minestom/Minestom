package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ItemMeta implements Cloneable {

    private final ItemMetaBuilder builder;
    private final Component displayName;
    private final List<Component> lore;

    protected ItemMeta(@NotNull ItemMetaBuilder metaBuilder) {
        this.builder = metaBuilder.clone();
        this.displayName = metaBuilder.displayName;
        this.lore = Collections.unmodifiableList(metaBuilder.lore);
    }

    @Contract(value = "_, -> new", pure = true)
    public @NotNull ItemMeta with(@NotNull Consumer<@NotNull ItemMetaBuilder> builderConsumer) {
        var builder = builder();
        builderConsumer.accept(builder);
        return builder.build();
    }

    @Contract(pure = true)
    public @Nullable Component getDisplayName() {
        return displayName;
    }

    @Contract(pure = true)
    public @Nullable List<@NotNull Component> getLore() {
        return lore;
    }

    protected @NotNull ItemMetaBuilder builder() {
        return builder.clone();
    }
}
