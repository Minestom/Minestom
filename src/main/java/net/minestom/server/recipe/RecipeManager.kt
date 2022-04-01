package net.minestom.server.recipe

import net.minestom.server.network.packet.server.play.DeclareRecipesPacket.Ingredient
import net.minestom.server.item.ItemStack
import net.minestom.server.recipe.Recipe
import java.lang.IndexOutOfBoundsException
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket.DeclaredRecipe
import java.util.concurrent.CopyOnWriteArraySet
import net.minestom.server.recipe.ShapelessRecipe
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket.DeclaredShapelessCraftingRecipe
import net.minestom.server.recipe.ShapedRecipe
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket.DeclaredShapedCraftingRecipe
import net.minestom.server.recipe.SmeltingRecipe
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket.DeclaredSmeltingRecipe
import net.minestom.server.recipe.BlastingRecipe
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket.DeclaredBlastingRecipe
import net.minestom.server.recipe.SmokingRecipe
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket.DeclaredSmokingRecipe
import net.minestom.server.recipe.CampfireCookingRecipe
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket.DeclaredCampfireCookingRecipe
import net.minestom.server.recipe.StonecutterRecipe
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket.DeclaredStonecutterRecipe
import net.minestom.server.recipe.SmithingRecipe
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket.DeclaredSmithingRecipe
import java.util.ArrayList
import java.util.List

class RecipeManager {
    var declareRecipesPacket = DeclareRecipesPacket(List.of())
        private set
    private val recipes: MutableSet<Recipe> = CopyOnWriteArraySet()
    fun addRecipes(vararg recipe: Recipe?) {
        if (recipes.addAll(List.of(*recipe))) {
            refreshRecipesPacket()
        }
    }

    fun addRecipe(recipe: Recipe) {
        if (recipes.add(recipe)) {
            refreshRecipesPacket()
        }
    }

    fun removeRecipe(recipe: Recipe) {
        if (recipes.remove(recipe)) {
            refreshRecipesPacket()
        }
    }

    fun getRecipes(): Set<Recipe> {
        return recipes
    }

    private fun refreshRecipesPacket() {
        val recipesCache: MutableList<DeclaredRecipe> = ArrayList()
        for (recipe in recipes) {
            when (recipe.recipeType) {
                Recipe.Type.SHAPELESS -> {
                    val shapelessRecipe = recipe as ShapelessRecipe
                    recipesCache.add(
                        DeclaredShapelessCraftingRecipe(
                            shapelessRecipe.getRecipeId(),
                            shapelessRecipe.group,
                            shapelessRecipe.ingredients,
                            shapelessRecipe.result
                        )
                    )
                }
                Recipe.Type.SHAPED -> {
                    val shapedRecipe = recipe as ShapedRecipe
                    recipesCache.add(
                        DeclaredShapedCraftingRecipe(
                            shapedRecipe.getRecipeId(),
                            shapedRecipe.width,
                            shapedRecipe.height,
                            shapedRecipe.group,
                            shapedRecipe.ingredients,
                            shapedRecipe.result
                        )
                    )
                }
                Recipe.Type.SMELTING -> {
                    val smeltingRecipe = recipe as SmeltingRecipe
                    recipesCache.add(
                        DeclaredSmeltingRecipe(
                            smeltingRecipe.getRecipeId(),
                            smeltingRecipe.group,
                            smeltingRecipe.ingredient,
                            smeltingRecipe.result,
                            smeltingRecipe.experience,
                            smeltingRecipe.cookingTime
                        )
                    )
                }
                Recipe.Type.BLASTING -> {
                    val blastingRecipe = recipe as BlastingRecipe
                    recipesCache.add(
                        DeclaredBlastingRecipe(
                            blastingRecipe.getRecipeId(),
                            blastingRecipe.group,
                            blastingRecipe.ingredient,
                            blastingRecipe.result,
                            blastingRecipe.experience,
                            blastingRecipe.cookingTime
                        )
                    )
                }
                Recipe.Type.SMOKING -> {
                    val smokingRecipe = recipe as SmokingRecipe
                    recipesCache.add(
                        DeclaredSmokingRecipe(
                            smokingRecipe.getRecipeId(),
                            smokingRecipe.group,
                            smokingRecipe.ingredient,
                            smokingRecipe.result,
                            smokingRecipe.experience,
                            smokingRecipe.cookingTime
                        )
                    )
                }
                Recipe.Type.CAMPFIRE_COOKING -> {
                    val campfireCookingRecipe = recipe as CampfireCookingRecipe
                    recipesCache.add(
                        DeclaredCampfireCookingRecipe(
                            campfireCookingRecipe.getRecipeId(),
                            campfireCookingRecipe.group,
                            campfireCookingRecipe.ingredient,
                            campfireCookingRecipe.result,
                            campfireCookingRecipe.experience,
                            campfireCookingRecipe.cookingTime
                        )
                    )
                }
                Recipe.Type.STONECUTTING -> {
                    val stonecuttingRecipe = recipe as StonecutterRecipe
                    recipesCache.add(
                        DeclaredStonecutterRecipe(
                            stonecuttingRecipe.getRecipeId(),
                            stonecuttingRecipe.group,
                            stonecuttingRecipe.ingredient,
                            stonecuttingRecipe.result
                        )
                    )
                }
                Recipe.Type.SMITHING -> {
                    val smithingRecipe = recipe as SmithingRecipe
                    recipesCache.add(
                        DeclaredSmithingRecipe(
                            smithingRecipe.getRecipeId(),
                            smithingRecipe.baseIngredient,
                            smithingRecipe.additionIngredient,
                            smithingRecipe.result
                        )
                    )
                }
            }
        }
        declareRecipesPacket = DeclareRecipesPacket(recipesCache)
        // TODO; refresh and update players recipes
    }
}