package net.minestom.server.recipe;

import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
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
        declareRecipesPacket = new DeclareRecipesPacket(recipes.stream().map(this::mapPacket).toList());
    }

    @NotNull
    private DeclareRecipesPacket.DeclaredRecipe mapPacket(@NotNull Recipe recipe) {
        return switch (recipe.recipeType) {
            case SHAPELESS -> PacketDeclaration.create((ShapelessRecipe) recipe);
            case SHAPED -> PacketDeclaration.create((ShapedRecipe) recipe);
            case SMELTING -> PacketDeclaration.create((SmeltingRecipe) recipe);
            case BLASTING -> PacketDeclaration.create((BlastingRecipe) recipe);
            case SMOKING -> PacketDeclaration.create((SmokingRecipe) recipe);
            case CAMPFIRE_COOKING -> PacketDeclaration.create((CampfireCookingRecipe) recipe);
            case STONECUTTING -> PacketDeclaration.create((StonecutterRecipe) recipe);
            case SMITHING_TRANSFORM -> PacketDeclaration.create((SmithingTransformRecipe) recipe);
            case SMITHING_TRIM -> PacketDeclaration.create((SmithingTrimRecipe) recipe);
        };
    }

}
