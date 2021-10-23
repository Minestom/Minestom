package net.minestom.server.recipe;

import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class RecipeManager {
    private final DeclareRecipesPacket declareRecipesPacket = new DeclareRecipesPacket();
    private final Set<Recipe> recipes = new CopyOnWriteArraySet<>();

    public void addRecipe(@NotNull Recipe recipe) {
        if (this.recipes.add(recipe)) {
            // TODO add to all players

            refreshRecipesPacket();
        }
    }

    public void removeRecipe(@NotNull Recipe recipe) {
        if (this.recipes.remove(recipe)) {
            // TODO remove to all players

            refreshRecipesPacket();
        }
    }

    @NotNull
    public Set<Recipe> getRecipes() {
        return recipes;
    }

    @NotNull
    public DeclareRecipesPacket getDeclareRecipesPacket() {
        return declareRecipesPacket;
    }

    private void refreshRecipesPacket() {
        List<DeclareRecipesPacket.DeclaredRecipe> recipesCache = new ArrayList<>();
        for (Recipe recipe : recipes) {
            switch (recipe.recipeType) {
                case SHAPELESS -> {
                    ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
                    recipesCache.add(
                            new DeclareRecipesPacket.DeclaredShapelessCraftingRecipe(
                                    shapelessRecipe.getRecipeId(),
                                    shapelessRecipe.getGroup(),
                                    shapelessRecipe.getIngredients().toArray(new DeclareRecipesPacket.Ingredient[0]),
                                    shapelessRecipe.getResult()
                            )
                    );
                }
                case SHAPED -> {
                    ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
                    recipesCache.add(
                            new DeclareRecipesPacket.DeclaredShapedCraftingRecipe(
                                    shapedRecipe.getRecipeId(),
                                    shapedRecipe.getWidth(),
                                    shapedRecipe.getHeight(),
                                    shapedRecipe.getGroup(),
                                    shapedRecipe.getIngredients().toArray(new DeclareRecipesPacket.Ingredient[0]),
                                    shapedRecipe.getResult()
                            )
                    );
                }
                case SMELTING -> {
                    SmeltingRecipe smeltingRecipe = (SmeltingRecipe) recipe;
                    recipesCache.add(
                            new DeclareRecipesPacket.DeclaredSmeltingRecipe(
                                    smeltingRecipe.getRecipeId(),
                                    smeltingRecipe.getGroup(),
                                    smeltingRecipe.getIngredient(),
                                    smeltingRecipe.getResult(),
                                    smeltingRecipe.getExperience(),
                                    smeltingRecipe.getCookingTime()
                            )
                    );
                }
                case BLASTING -> {
                    BlastingRecipe blastingRecipe = (BlastingRecipe) recipe;
                    recipesCache.add(
                            new DeclareRecipesPacket.DeclaredBlastingRecipe(
                                    blastingRecipe.getRecipeId(),
                                    blastingRecipe.getGroup(),
                                    blastingRecipe.getIngredient(),
                                    blastingRecipe.getResult(),
                                    blastingRecipe.getExperience(),
                                    blastingRecipe.getCookingTime()
                            )
                    );
                }
                case SMOKING -> {
                    SmokingRecipe smokingRecipe = (SmokingRecipe) recipe;
                    recipesCache.add(
                            new DeclareRecipesPacket.DeclaredSmokingRecipe(
                                    smokingRecipe.getRecipeId(),
                                    smokingRecipe.getGroup(),
                                    smokingRecipe.getIngredient(),
                                    smokingRecipe.getResult(),
                                    smokingRecipe.getExperience(),
                                    smokingRecipe.getCookingTime()
                            )
                    );
                }
                case CAMPFIRE_COOKING -> {
                    CampfireCookingRecipe campfireCookingRecipe = (CampfireCookingRecipe) recipe;
                    recipesCache.add(
                            new DeclareRecipesPacket.DeclaredCampfireCookingRecipe(
                                    campfireCookingRecipe.getRecipeId(),
                                    campfireCookingRecipe.getGroup(),
                                    campfireCookingRecipe.getIngredient(),
                                    campfireCookingRecipe.getResult(),
                                    campfireCookingRecipe.getExperience(),
                                    campfireCookingRecipe.getCookingTime()
                            )
                    );
                }
                case STONECUTTING -> {
                    StonecutterRecipe stonecuttingRecipe = (StonecutterRecipe) recipe;
                    recipesCache.add(
                            new DeclareRecipesPacket.DeclaredStonecutterRecipe(
                                    stonecuttingRecipe.getRecipeId(),
                                    stonecuttingRecipe.getGroup(),
                                    stonecuttingRecipe.getIngredient(),
                                    stonecuttingRecipe.getResult()
                            )
                    );
                }
                case SMITHING -> {
                    SmithingRecipe smithingRecipe = (SmithingRecipe) recipe;
                    recipesCache.add(
                            new DeclareRecipesPacket.DeclaredSmithingRecipe(
                                    smithingRecipe.getRecipeId(),
                                    smithingRecipe.getBaseIngredient(),
                                    smithingRecipe.getAdditionIngredient(),
                                    smithingRecipe.getResult()
                            )
                    );
                }
            }
        }

        declareRecipesPacket.recipes = recipesCache.toArray(new DeclareRecipesPacket.DeclaredRecipe[0]);
    }

}
