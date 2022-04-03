package net.minestom.server.recipe

import net.minestom.server.entity.Player
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

abstract class Recipe protected constructor(val recipeType: Type, val recipeId: String) {

    abstract fun shouldShow(player: Player): Boolean
    enum class Type {
        SHAPELESS, SHAPED, SMELTING, BLASTING, SMOKING, CAMPFIRE_COOKING, STONECUTTING, SMITHING
    }
}