package net.minestom.server.recipe;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class Recipe {
    protected final Type recipeType;
    protected final String recipeId;

    protected Recipe(@NotNull Type recipeType, @NotNull String recipeId) {
        this.recipeType = recipeType;
        this.recipeId = recipeId;
    }

    public abstract boolean shouldShow(@NotNull Player player);

    @NotNull
    public Type getRecipeType() {
        return recipeType;
    }

    @NotNull
    public String getRecipeId() {
        return recipeId;
    }

    public enum Type {
        SHAPELESS,
        SHAPED,
        SMELTING,
        BLASTING,
        SMOKING,
        CAMPFIRE_COOKING,
        STONECUTTING,
        SMITHING
    }

}
