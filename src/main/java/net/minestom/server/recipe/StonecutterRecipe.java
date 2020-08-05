package net.minestom.server.recipe;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;

public abstract class StonecutterRecipe extends Recipe {

    private DeclareRecipesPacket.Ingredient ingredient;

    private ItemStack result = ItemStack.getAirItem();

    protected StonecutterRecipe(String recipeId, String group) {
        super(RecipeType.STONECUTTER, recipeId);
        setGroup(group);
    }

    public DeclareRecipesPacket.Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(DeclareRecipesPacket.Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public ItemStack getResult() {
        return result;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }
}
