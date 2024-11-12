package net.minestom.server.recipe;

import net.minestom.server.recipe.display.RecipeDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Recipe {

    @NotNull RecipeDisplay toDisplay();

    /**
     * Returns the protocol recipe type.
     * @return
     */
    default @Nullable RecipeType recipeType() {
        return null;
    }

}
