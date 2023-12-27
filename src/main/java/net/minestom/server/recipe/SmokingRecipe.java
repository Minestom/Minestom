package net.minestom.server.recipe;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import org.jetbrains.annotations.NotNull;

public abstract class SmokingRecipe extends Recipe {
    private String group;
    private RecipeCategory.Cooking category;
    private DeclareRecipesPacket.Ingredient ingredient;
    private ItemStack result;
    private float experience;
    private int cookingTime;

    protected SmokingRecipe(
            @NotNull String recipeId,
            @NotNull String group,
            @NotNull RecipeCategory.Cooking category,
            @NotNull ItemStack result,
            float experience,
            int cookingTime
    ) {
        super(Type.SMOKING, recipeId);
        this.group = group;
        this.category = category;
        this.result = result;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    @NotNull
    public String getGroup() {
        return group;
    }

    public void setGroup(@NotNull String group) {
        this.group = group;
    }

    @NotNull
    public RecipeCategory.Cooking getCategory() {
        return category;
    }

    public void setCategory(@NotNull RecipeCategory.Cooking category) {
        this.category = category;
    }

    @NotNull
    public DeclareRecipesPacket.Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(@NotNull DeclareRecipesPacket.Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    @NotNull
    public ItemStack getResult() {
        return result;
    }

    public void setResult(@NotNull ItemStack result) {
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
