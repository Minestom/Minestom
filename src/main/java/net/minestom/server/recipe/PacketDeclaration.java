package net.minestom.server.recipe;

import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import org.jetbrains.annotations.NotNull;

interface PacketDeclaration {

    @NotNull
    static DeclareRecipesPacket.DeclaredShapelessCraftingRecipe create(@NotNull ShapelessRecipe shapelessRecipe) {
        return new DeclareRecipesPacket.DeclaredShapelessCraftingRecipe(shapelessRecipe.getRecipeId(),
                shapelessRecipe.getGroup(),
                shapelessRecipe.getCategory(),
                shapelessRecipe.getIngredients(),
                shapelessRecipe.getResult());
    }

    @NotNull
    static DeclareRecipesPacket.DeclaredShapedCraftingRecipe create(@NotNull ShapedRecipe shapedRecipe) {
        return new DeclareRecipesPacket.DeclaredShapedCraftingRecipe(shapedRecipe.getRecipeId(),
                shapedRecipe.getGroup(),
                shapedRecipe.getCategory(),
                shapedRecipe.getWidth(),
                shapedRecipe.getHeight(),
                shapedRecipe.getIngredients(),
                shapedRecipe.getResult(),
                shapedRecipe.getShowNotification());
    }

    @NotNull
    static DeclareRecipesPacket.DeclaredSmeltingRecipe create(@NotNull SmeltingRecipe smeltingRecipe) {
        return  new DeclareRecipesPacket.DeclaredSmeltingRecipe(
                smeltingRecipe.getRecipeId(),
                smeltingRecipe.getGroup(),
                smeltingRecipe.getCategory(),
                smeltingRecipe.getIngredient(),
                smeltingRecipe.getResult(),
                smeltingRecipe.getExperience(),
                smeltingRecipe.getCookingTime());
    }

    @NotNull
    static DeclareRecipesPacket.DeclaredBlastingRecipe create(@NotNull BlastingRecipe blastingRecipe) {
        return new DeclareRecipesPacket.DeclaredBlastingRecipe(
                blastingRecipe.getRecipeId(),
                blastingRecipe.getGroup(),
                blastingRecipe.getCategory(),
                blastingRecipe.getIngredient(),
                blastingRecipe.getResult(),
                blastingRecipe.getExperience(),
                blastingRecipe.getCookingTime());
    }

    @NotNull
    static DeclareRecipesPacket.DeclaredSmokingRecipe create(@NotNull SmokingRecipe smokingRecipe) {
        return new DeclareRecipesPacket.DeclaredSmokingRecipe(
                smokingRecipe.getRecipeId(),
                smokingRecipe.getGroup(),
                smokingRecipe.getCategory(),
                smokingRecipe.getIngredient(),
                smokingRecipe.getResult(),
                smokingRecipe.getExperience(),
                smokingRecipe.getCookingTime());
    }

    @NotNull
    static DeclareRecipesPacket.DeclaredCampfireCookingRecipe create(@NotNull CampfireCookingRecipe campfireCookingRecipe) {
        return new DeclareRecipesPacket.DeclaredCampfireCookingRecipe(
                campfireCookingRecipe.getRecipeId(),
                campfireCookingRecipe.getGroup(),
                campfireCookingRecipe.getCategory(),
                campfireCookingRecipe.getIngredient(),
                campfireCookingRecipe.getResult(),
                campfireCookingRecipe.getExperience(),
                campfireCookingRecipe.getCookingTime());
    }

    @NotNull
    static DeclareRecipesPacket.DeclaredStonecutterRecipe create(@NotNull StonecutterRecipe stonecuttingRecipe) {
        return new DeclareRecipesPacket.DeclaredStonecutterRecipe(
                stonecuttingRecipe.getRecipeId(),
                stonecuttingRecipe.getGroup(),
                stonecuttingRecipe.getIngredient(),
                stonecuttingRecipe.getResult());
    }

    @NotNull
    static DeclareRecipesPacket.DeclaredSmithingTransformRecipe create(@NotNull SmithingTransformRecipe smithingTransformRecipe) {
        return  new DeclareRecipesPacket.DeclaredSmithingTransformRecipe(
                smithingTransformRecipe.getRecipeId(),
                smithingTransformRecipe.getTemplate(),
                smithingTransformRecipe.getBaseIngredient(),
                smithingTransformRecipe.getAdditionIngredient(),
                smithingTransformRecipe.getResult());
    }

    @NotNull
    static DeclareRecipesPacket.DeclaredSmithingTrimRecipe create(@NotNull SmithingTrimRecipe smithingTrimRecipe) {
        return  new DeclareRecipesPacket.DeclaredSmithingTrimRecipe(
                smithingTrimRecipe.getRecipeId(),
                smithingTrimRecipe.getTemplate(),
                smithingTrimRecipe.getBaseIngredient(),
                smithingTrimRecipe.getAdditionIngredient());
    }
}
