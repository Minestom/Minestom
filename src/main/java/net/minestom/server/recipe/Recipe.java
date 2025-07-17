package net.minestom.server.recipe;

import net.minestom.server.item.Material;
import net.minestom.server.recipe.display.RecipeDisplay;
import net.minestom.server.recipe.display.SlotDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface Recipe {

    /**
     * Creates recipe displays for use in the recipe book.
     *
     * <p>Displays should be consistent across calls and not specific to a player, they may be cached in {@link RecipeManager}.</p>
     *
     * <p>Note that stonecutter recipes are always sent to the client and not present in the recipe book.
     * Stonecutter ingredients must be {@link SlotDisplay.Item} or {@link SlotDisplay.Tag} to be shown
     * on the client.</p>
     *
     * @return a list of recipe displays, or none if the recipe should not be displayed in the recipe book
     */
    default @NotNull List<RecipeDisplay> createRecipeDisplays() {
        return List.of();
    }

    /**
     * Returns the item properties associated with this recipe. These are sent to the client to indicate
     * client side special slot prediction. For example, if a recipe includes {@link Material#STONE} in
     * {@link RecipeProperty#FURNACE_INPUT}, the client will predict that item being placed into a furnace
     * input (note that final placement is still decided by the server).
     *
     * <p>Item properties should be consistent across calls and not specific to a player, they may be cached in {@link RecipeManager}.</p>
     *
     * @return A map of item properties associated with this recipe.
     */
    default @NotNull Map<RecipeProperty, List<Material>> itemProperties() {
        return Map.of();
    }

    default @Nullable String recipeBookGroup() {
        return null;
    }

    default @Nullable RecipeBookCategory recipeBookCategory() {
        return null;
    }

    default @Nullable List<Ingredient> craftingRequirements() {
        return null;
    }

}
