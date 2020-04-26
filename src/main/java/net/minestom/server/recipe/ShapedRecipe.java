package net.minestom.server.recipe;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;

import java.util.ArrayList;
import java.util.List;

public abstract class ShapedRecipe extends Recipe {

    private int width, height;

    private List<DeclareRecipesPacket.Ingredient> ingredients = new ArrayList<>();

    private ItemStack result = ItemStack.getAirItem();

    public ShapedRecipe(String recipeId, String group, int width, int height) {
        super(RecipeType.SHAPED, recipeId);
        setGroup(group);
        this.width = width;
        this.height = height;
    }

    public void addIngredient(DeclareRecipesPacket.Ingredient ingredient) {
        if (ingredients.size() + 1 > width * height)
            throw new IndexOutOfBoundsException("You cannot add more ingredients than width*height");

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
