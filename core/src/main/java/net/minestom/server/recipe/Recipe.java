package net.minestom.server.recipe;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class Recipe {
    protected final RecipeType recipeType;
    protected final String recipeId;

    protected Recipe(@NotNull RecipeType recipeType, @NotNull String recipeId) {
        this.recipeType = recipeType;
        this.recipeId = recipeId;
    }

    public abstract boolean shouldShow(@NotNull Player player);

    @NotNull
    public RecipeType getRecipeType() {
        return recipeType;
    }

    @NotNull
    public String getRecipeId() {
        return recipeId;
    }

    protected enum RecipeType {
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
