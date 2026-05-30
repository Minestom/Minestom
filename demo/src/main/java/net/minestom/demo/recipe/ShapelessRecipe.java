package net.minestom.demo.recipe;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.recipe.Recipe;
import net.minestom.server.recipe.RecipeBookCategory;
import net.minestom.server.recipe.display.RecipeDisplay;
import net.minestom.server.recipe.display.SlotDisplay;
import net.minestom.server.registry.RegistryTag;

import java.util.List;

public record ShapelessRecipe(
        RecipeBookCategory recipeBookCategory,
        List<Material> ingredients,
        ItemStack result
) implements Recipe {

    @Override
    public List<RecipeDisplay> createRecipeDisplays() {
        return List.of(new RecipeDisplay.CraftingShapeless(
                ingredients.stream().map(item -> (SlotDisplay) new SlotDisplay.Item(item)).toList(),
                new SlotDisplay.ItemStack(result),
                new SlotDisplay.Item(Material.CRAFTING_TABLE)
        ));
    }

    @Override
    public List<RegistryTag<Material>> craftingRequirements() {
        return List.of(RegistryTag.direct(ingredients));
    }

}
