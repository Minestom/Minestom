package net.minestom.server.recipe;

import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import org.jetbrains.annotations.NotNull;

final class RecipeConversion {

    static @NotNull DeclareRecipesPacket.DeclaredShapelessCraftingRecipe shapeless(@NotNull ShapelessRecipe shapelessRecipe) {
        return new DeclareRecipesPacket.DeclaredShapelessCraftingRecipe(shapelessRecipe.id(),
                shapelessRecipe.getGroup(),
                shapelessRecipe.getCategory(),
                shapelessRecipe.getIngredients(),
                shapelessRecipe.getResult());
    }

    static @NotNull DeclareRecipesPacket.DeclaredShapedCraftingRecipe shaped(@NotNull ShapedRecipe shapedRecipe) {
        return new DeclareRecipesPacket.DeclaredShapedCraftingRecipe(shapedRecipe.id(),
                shapedRecipe.getGroup(),
                shapedRecipe.getCategory(),
                shapedRecipe.getWidth(),
                shapedRecipe.getHeight(),
                shapedRecipe.getIngredients(),
                shapedRecipe.getResult(),
                shapedRecipe.getShowNotification());
    }

    static @NotNull DeclareRecipesPacket.DeclaredSmeltingRecipe smelting(@NotNull SmeltingRecipe smeltingRecipe) {
        return  new DeclareRecipesPacket.DeclaredSmeltingRecipe(
                smeltingRecipe.id(),
                smeltingRecipe.getGroup(),
                smeltingRecipe.getCategory(),
                smeltingRecipe.getIngredient(),
                smeltingRecipe.getResult(),
                smeltingRecipe.getExperience(),
                smeltingRecipe.getCookingTime());
    }

    static @NotNull DeclareRecipesPacket.DeclaredBlastingRecipe blasting(@NotNull BlastingRecipe blastingRecipe) {
        return new DeclareRecipesPacket.DeclaredBlastingRecipe(
                blastingRecipe.id(),
                blastingRecipe.getGroup(),
                blastingRecipe.getCategory(),
                blastingRecipe.getIngredient(),
                blastingRecipe.getResult(),
                blastingRecipe.getExperience(),
                blastingRecipe.getCookingTime());
    }

    static @NotNull DeclareRecipesPacket.DeclaredSmokingRecipe smoking(@NotNull SmokingRecipe smokingRecipe) {
        return new DeclareRecipesPacket.DeclaredSmokingRecipe(
                smokingRecipe.id(),
                smokingRecipe.getGroup(),
                smokingRecipe.getCategory(),
                smokingRecipe.getIngredient(),
                smokingRecipe.getResult(),
                smokingRecipe.getExperience(),
                smokingRecipe.getCookingTime());
    }

    static @NotNull DeclareRecipesPacket.DeclaredCampfireCookingRecipe campfire(@NotNull CampfireCookingRecipe campfireCookingRecipe) {
        return new DeclareRecipesPacket.DeclaredCampfireCookingRecipe(
                campfireCookingRecipe.id(),
                campfireCookingRecipe.getGroup(),
                campfireCookingRecipe.getCategory(),
                campfireCookingRecipe.getIngredient(),
                campfireCookingRecipe.getResult(),
                campfireCookingRecipe.getExperience(),
                campfireCookingRecipe.getCookingTime());
    }

    static @NotNull DeclareRecipesPacket.DeclaredStonecutterRecipe stonecutter(@NotNull StonecutterRecipe stonecuttingRecipe) {
        return new DeclareRecipesPacket.DeclaredStonecutterRecipe(
                stonecuttingRecipe.id(),
                stonecuttingRecipe.getGroup(),
                stonecuttingRecipe.getIngredient(),
                stonecuttingRecipe.getResult());
    }

    static @NotNull DeclareRecipesPacket.DeclaredSmithingTransformRecipe smithingTransform(@NotNull SmithingTransformRecipe smithingTransformRecipe) {
        return  new DeclareRecipesPacket.DeclaredSmithingTransformRecipe(
                smithingTransformRecipe.id(),
                smithingTransformRecipe.getTemplate(),
                smithingTransformRecipe.getBaseIngredient(),
                smithingTransformRecipe.getAdditionIngredient(),
                smithingTransformRecipe.getResult());
    }

    static @NotNull DeclareRecipesPacket.DeclaredSmithingTrimRecipe smithingTrim(@NotNull SmithingTrimRecipe smithingTrimRecipe) {
        return  new DeclareRecipesPacket.DeclaredSmithingTrimRecipe(
                smithingTrimRecipe.id(),
                smithingTrimRecipe.getTemplate(),
                smithingTrimRecipe.getBaseIngredient(),
                smithingTrimRecipe.getAdditionIngredient());
    }

    private RecipeConversion() {

    }
}
