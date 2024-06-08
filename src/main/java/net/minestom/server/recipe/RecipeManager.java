package net.minestom.server.recipe;

import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class RecipeManager {
    private final CachedPacket declareRecipesPacket = new CachedPacket(this::createDeclareRecipesPacket);
    private final Set<Recipe> recipes = new CopyOnWriteArraySet<>();

    public void addRecipes(@NotNull Recipe... recipe) {
        if (recipes.addAll(List.of(recipe))) {
            declareRecipesPacket.invalidate();
        }
    }

    public void addRecipe(@NotNull Recipe recipe) {
        if (this.recipes.add(recipe)) {
            declareRecipesPacket.invalidate();
        }
    }

    public void removeRecipe(@NotNull Recipe recipe) {
        if (this.recipes.remove(recipe)) {
            declareRecipesPacket.invalidate();
        }
    }

    @NotNull
    public Set<Recipe> getRecipes() {
        return recipes;
    }

    @NotNull
    public SendablePacket getDeclareRecipesPacket() {
        return declareRecipesPacket;
    }

    private @NotNull DeclareRecipesPacket createDeclareRecipesPacket() {
        var entries = new ArrayList<DeclareRecipesPacket.DeclaredRecipe>();
        for (var recipe : recipes) {
            entries.add(switch (recipe.type) {
                case SHAPELESS -> RecipeConversion.shapeless((ShapelessRecipe) recipe);
                case SHAPED -> RecipeConversion.shaped((ShapedRecipe) recipe);
                case SMELTING -> RecipeConversion.smelting((SmeltingRecipe) recipe);
                case BLASTING -> RecipeConversion.blasting((BlastingRecipe) recipe);
                case SMOKING -> RecipeConversion.smoking((SmokingRecipe) recipe);
                case CAMPFIRE_COOKING -> RecipeConversion.campfire((CampfireCookingRecipe) recipe);
                case STONECUTTING -> RecipeConversion.stonecutter((StonecutterRecipe) recipe);
                case SMITHING_TRANSFORM -> RecipeConversion.smithingTransform((SmithingTransformRecipe) recipe);
                case SMITHING_TRIM -> RecipeConversion.smithingTrim((SmithingTrimRecipe) recipe);
                default -> throw new IllegalStateException("Unhandled recipe type : " + recipe.type);
            });
        }
        return new DeclareRecipesPacket(entries);
    }

}
