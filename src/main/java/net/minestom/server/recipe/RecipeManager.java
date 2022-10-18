package net.minestom.server.recipe;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import net.minestom.server.network.packet.server.play.UnlockRecipesPacket;
import net.minestom.server.recipe.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class RecipeManager {
    private DeclareRecipesPacket declareRecipesPacket = new DeclareRecipesPacket(List.of());
    private final Set<Recipe> recipes = new CopyOnWriteArraySet<>();

    public void addRecipes(@NotNull Recipe... recipe) {
        if (recipes.addAll(List.of(recipe))) {
            refreshRecipesPacket();
        }
    }

    public void addRecipe(@NotNull Recipe recipe) {
        if (this.recipes.add(recipe)) {
            refreshRecipesPacket();
        }
    }

    public void removeRecipe(@NotNull Recipe recipe) {
        if (this.recipes.remove(recipe)) {
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
            switch (recipe.getRecipeType()) {
                case SHAPELESS -> {
                    ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
                    recipesCache.add(
                            new DeclareRecipesPacket.DeclaredShapelessCraftingRecipe(
                                    shapelessRecipe.getRecipeId(),
                                    shapelessRecipe.getGroup(),
                                    shapelessRecipe.getIngredients(),
                                    shapelessRecipe.getResult()));
                }
                case SHAPED -> {
                    ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
                    recipesCache.add(
                            new DeclareRecipesPacket.DeclaredShapedCraftingRecipe(
                                    shapedRecipe.getRecipeId(),
                                    shapedRecipe.getWidth(),
                                    shapedRecipe.getHeight(),
                                    shapedRecipe.getGroup(),
                                    shapedRecipe.getIngredients(),
                                    shapedRecipe.getResult()));
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
                                    smeltingRecipe.getCookingTime()));
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
                                    blastingRecipe.getCookingTime()));
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
                                    smokingRecipe.getCookingTime()));
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
                                    campfireCookingRecipe.getCookingTime()));
                }
                case STONECUTTING -> {
                    StonecutterRecipe stonecuttingRecipe = (StonecutterRecipe) recipe;
                    recipesCache.add(
                            new DeclareRecipesPacket.DeclaredStonecutterRecipe(
                                    stonecuttingRecipe.getRecipeId(),
                                    stonecuttingRecipe.getGroup(),
                                    stonecuttingRecipe.getIngredient(),
                                    stonecuttingRecipe.getResult()));
                }
                case SMITHING -> {
                    SmithingRecipe smithingRecipe = (SmithingRecipe) recipe;
                    recipesCache.add(
                            new DeclareRecipesPacket.DeclaredSmithingRecipe(
                                    smithingRecipe.getRecipeId(),
                                    smithingRecipe.getBaseIngredient(),
                                    smithingRecipe.getAdditionIngredient(),
                                    smithingRecipe.getResult()));
                }
            }
        }

        declareRecipesPacket = new DeclareRecipesPacket(recipesCache);
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(this::refreshPlayerRecipes);
    }

    public void refreshPlayerRecipes(Player player) {
        List<String> recipesIdentifier = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (!recipe.shouldShow(player))
                continue;
            recipesIdentifier.add(recipe.getRecipeId());
        }
        if (!recipesIdentifier.isEmpty()) {
            UnlockRecipesPacket unlockRecipesPacket = new UnlockRecipesPacket(0,
                    false, false,
                    false, false,
                    false, false,
                    false, false,
                    recipesIdentifier, recipesIdentifier);
            player.sendPacket(unlockRecipesPacket);
        }
    }

}
