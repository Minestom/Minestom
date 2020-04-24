package net.minestom.server.recipe;

public class Recipe {

    protected RecipeType recipeType;
    protected String recipeId;
    protected String group;

    protected Recipe(RecipeType recipeType, String recipeId) {
        this.recipeType = recipeType;
        this.recipeId = recipeId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public RecipeType getRecipeType() {
        return recipeType;
    }

    public String getRecipeId() {
        return recipeId;
    }

    protected enum RecipeType {
        SHAPELESS, SHAPED, SMELTING
    }

}
