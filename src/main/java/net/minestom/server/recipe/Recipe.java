package net.minestom.server.recipe;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class Recipe {
    protected final RecipeType type;
    protected final String id;

    protected Recipe(@NotNull RecipeType type, @NotNull String id) {
        this.type = type;
        this.id = id;
    }

    public abstract boolean shouldShow(@NotNull Player player);

    @NotNull
    public RecipeType type() {
        return type;
    }

    public @NotNull String id() {
        return id;
    }
}
