package net.minestom.server.recipe;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;

import java.util.ArrayList;
import java.util.List;

public abstract class ShapelessRecipe extends Recipe {

    private List<DeclareRecipesPacket.Ingredient> ingredients = new ArrayList<>();

    private ItemStack result = ItemStack.getAirItem();

    public ShapelessRecipe(String recipeId, String group) {
        super(RecipeType.SHAPELESS, recipeId);
        setGroup(group);
    }

    public void addIngredient(DeclareRecipesPacket.Ingredient ingredient) {
        ingredients.add(ingredient);
    }

    public List<DeclareRecipesPacket.Ingredient> getIngredients() {
        return ingredients;
    }

    public ItemStack getResult() {
        return result;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }
}
