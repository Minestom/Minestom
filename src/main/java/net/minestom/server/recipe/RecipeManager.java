package net.minestom.server.recipe;

import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class RecipeManager {

    private Set<Recipe> recipes = new CopyOnWriteArraySet<>();

    private DeclareRecipesPacket declareRecipesPacket = new DeclareRecipesPacket();

    public void addRecipe(Recipe recipe) {
        if (this.recipes.add(recipe)) {
            // TODO add to all players

            refreshRecipesPacket();
        }
    }

    public void removeRecipe(Recipe recipe) {
        if (this.recipes.remove(recipe)) {
            // TODO remove to all players

            refreshRecipesPacket();
        }
    }

    public Set<Recipe> getRecipes() {
        return recipes;
    }

    public DeclareRecipesPacket getDeclareRecipesPacket() {
        return declareRecipesPacket;
    }

    private void refreshRecipesPacket() {
        List<DeclareRecipesPacket.Recipe> recipesCache = new ArrayList<>();
        for (Recipe recipe : recipes) {
            DeclareRecipesPacket.Recipe packetRecipe = new DeclareRecipesPacket.Recipe();

            switch (recipe.recipeType) {
                case SHAPELESS: {
                    packetRecipe.recipeType = "crafting_shapeless";
                    packetRecipe.group = recipe.getGroup();

                    ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;

                    List<DeclareRecipesPacket.Ingredient> ingredients = shapelessRecipe.getIngredients();
                    packetRecipe.ingredients = ingredients.toArray(new DeclareRecipesPacket.Ingredient[0]);
                    packetRecipe.result = shapelessRecipe.getResult();
                    break;
                }
                case SHAPED: {
                    packetRecipe.recipeType = "crafting_shaped";
                    packetRecipe.group = recipe.getGroup();

                    ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;

                    List<DeclareRecipesPacket.Ingredient> ingredients2 = shapedRecipe.getIngredients();
                    packetRecipe.ingredients = ingredients2.toArray(new DeclareRecipesPacket.Ingredient[0]);
                    packetRecipe.result = shapedRecipe.getResult();
                    break;
                }
                case SMELTING: {
                    packetRecipe.recipeType = "smelting";
                    packetRecipe.group = recipe.getGroup();

                    SmeltingRecipe smeltingRecipe = (SmeltingRecipe) recipe;

                    packetRecipe.ingredient = smeltingRecipe.getIngredient();
                    packetRecipe.result = smeltingRecipe.getResult();
                    packetRecipe.experience = smeltingRecipe.getExperience();
                    packetRecipe.cookingTime = smeltingRecipe.getCookingTime();
                    break;
                }
                case BLASTING: {
                    packetRecipe.recipeType = "blasting";
                    packetRecipe.group = recipe.getGroup();

                    BlastingRecipe blastingRecipe = (BlastingRecipe) recipe;

                    packetRecipe.ingredient = blastingRecipe.getIngredient();
                    packetRecipe.result = blastingRecipe.getResult();
                    packetRecipe.experience = blastingRecipe.getExperience();
                    packetRecipe.cookingTime = blastingRecipe.getCookingTime();
                    break;
                }
                case SMOKING: {
                    packetRecipe.recipeType = "smoking";
                    packetRecipe.group = recipe.getGroup();

                    SmokingRecipe smokingRecipe = (SmokingRecipe) recipe;

                    packetRecipe.ingredient = smokingRecipe.getIngredient();
                    packetRecipe.result = smokingRecipe.getResult();
                    packetRecipe.experience = smokingRecipe.getExperience();
                    packetRecipe.cookingTime = smokingRecipe.getCookingTime();
                    break;
                }
                case CAMPFIRE_COOKING: {
                    packetRecipe.recipeType = "campfire_cooking";
                    packetRecipe.group = recipe.getGroup();

                    CampfireCookingRecipe campfireCookingRecipe = (CampfireCookingRecipe) recipe;

                    packetRecipe.ingredient = campfireCookingRecipe.getIngredient();
                    packetRecipe.result = campfireCookingRecipe.getResult();
                    packetRecipe.experience = campfireCookingRecipe.getExperience();
                    packetRecipe.cookingTime = campfireCookingRecipe.getCookingTime();
                    break;
                }
                case STONECUTTING: {
                    packetRecipe.recipeType = "stonecutting";
                    packetRecipe.group = recipe.getGroup();

                    StonecutterRecipe stonecuttingRecipe = (StonecutterRecipe) recipe;

                    packetRecipe.ingredient = stonecuttingRecipe.getIngredient();
                    packetRecipe.result = stonecuttingRecipe.getResult();
                    break;
                }
                case SMITHING: {
                    packetRecipe.recipeType = "smithing";

                    SmithingRecipe smithingRecipe = (SmithingRecipe) recipe;

                    packetRecipe.ingredient = smithingRecipe.getBaseIngredient();
                    packetRecipe.additionIngredient = smithingRecipe.getAdditionIngredient();
                    packetRecipe.result = smithingRecipe.getResult();
                    break;
                }
            }

            packetRecipe.recipeId = recipe.recipeId;

            recipesCache.add(packetRecipe);
        }

        declareRecipesPacket.recipes = recipesCache.toArray(new DeclareRecipesPacket.Recipe[0]);
    }

}
