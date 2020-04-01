package fr.themode.minestom.recipe;

import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.server.play.DeclareRecipesPacket;

public class SmeltingRecipe extends Recipe {

    private DeclareRecipesPacket.Ingredient ingredient;

    private ItemStack result = ItemStack.AIR_ITEM;

    private float experience;

    private int cookingTime;

    public SmeltingRecipe(String recipeId, String group) {
        super(RecipeType.SMELTING, recipeId);
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

    public float getExperience() {
        return experience;
    }

    public void setExperience(float experience) {
        this.experience = experience;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
    }
}
