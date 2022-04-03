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
import java.util.*
import java.util.function.Supplier

abstract class ShapelessRecipe protected constructor(
    recipeId: String,
    var group: String,
    ingredients: MutableList<Ingredient>?,
    result: ItemStack
) : Recipe(Type.SHAPELESS, recipeId) {
    private val ingredients: MutableList<Ingredient>
    var result: ItemStack

    init {
        this.ingredients =
            Objects.requireNonNullElseGet(ingredients, Supplier<MutableList<Ingredient>> { LinkedList() })
        this.result = result
    }

    fun addIngredient(ingredient: Ingredient) {
        ingredients.add(ingredient)
    }

    fun getIngredients(): List<Ingredient> {
        return ingredients
    }
}