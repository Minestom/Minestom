package net.minestom.demo.recipe;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.recipe.Ingredient;
import net.minestom.server.recipe.Recipe;
import net.minestom.server.recipe.RecipeBookCategory;
import net.minestom.server.recipe.display.RecipeDisplay;
import net.minestom.server.recipe.display.SlotDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ShapelessRecipe(
        @NotNull RecipeBookCategory recipeBookCategory,
        @NotNull List<Material> ingredients,
        @NotNull ItemStack result
) implements Recipe {

    @Override
    public @NotNull List<RecipeDisplay> createRecipeDisplays() {
        return List.of(new RecipeDisplay.CraftingShapeless(
                ingredients.stream().map(item -> (SlotDisplay) new SlotDisplay.Item(item)).toList(),
                new SlotDisplay.ItemStack(result),
                new SlotDisplay.Item(Material.CRAFTING_TABLE)
        ));
    }

    @Override
    public @NotNull List<Ingredient> craftingRequirements() {
        return List.of(new Ingredient(ingredients));
    }

}
