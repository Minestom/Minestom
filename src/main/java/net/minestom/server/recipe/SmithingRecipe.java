package net.minestom.server.recipe;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;

public abstract class SmithingRecipe extends Recipe {

    private DeclareRecipesPacket.Ingredient baseIngredient;
    private DeclareRecipesPacket.Ingredient additionIngredient;

    private ItemStack result = ItemStack.getAirItem();

    protected SmithingRecipe(String recipeId, String group) {
        super(RecipeType.SMITHING, recipeId);
        setGroup(group);
    }

    public DeclareRecipesPacket.Ingredient getBaseIngredient() {
        return baseIngredient;
    }

    public void setBaseIngredient(DeclareRecipesPacket.Ingredient baseIngredient) {
        this.baseIngredient = baseIngredient;
    }

    public DeclareRecipesPacket.Ingredient getAdditionIngredient() {
        return additionIngredient;
    }

    public void setAdditionIngredient(DeclareRecipesPacket.Ingredient additionIngredient) {
        this.additionIngredient = additionIngredient;
    }

    public ItemStack getResult() {
        return result;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }
}
