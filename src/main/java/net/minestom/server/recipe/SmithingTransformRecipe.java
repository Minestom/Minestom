package net.minestom.server.recipe;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import org.jetbrains.annotations.NotNull;

public abstract class SmithingTransformRecipe extends Recipe {
    private DeclareRecipesPacket.Ingredient template;
    private DeclareRecipesPacket.Ingredient baseIngredient;
    private DeclareRecipesPacket.Ingredient additionIngredient;
    private ItemStack result;

    protected SmithingTransformRecipe(
            @NotNull String recipeId,
            @NotNull DeclareRecipesPacket.Ingredient template,
            @NotNull DeclareRecipesPacket.Ingredient baseIngredient,
            @NotNull DeclareRecipesPacket.Ingredient additionIngredient,
            @NotNull ItemStack result
    ) {
        super(Type.SMITHING_TRANSFORM, recipeId);
        this.template = template;
        this.baseIngredient = baseIngredient;
        this.additionIngredient = additionIngredient;
        this.result = result;
    }

    @NotNull
    public DeclareRecipesPacket.Ingredient getTemplate() {
        return template;
    }

    public void setTemplate(@NotNull DeclareRecipesPacket.Ingredient template) {
        this.template = template;
    }

    @NotNull
    public DeclareRecipesPacket.Ingredient getBaseIngredient() {
        return baseIngredient;
    }

    public void setBaseIngredient(@NotNull DeclareRecipesPacket.Ingredient baseIngredient) {
        this.baseIngredient = baseIngredient;
    }

    @NotNull
    public DeclareRecipesPacket.Ingredient getAdditionIngredient() {
        return additionIngredient;
    }

    public void setAdditionIngredient(@NotNull DeclareRecipesPacket.Ingredient additionIngredient) {
        this.additionIngredient = additionIngredient;
    }

    @NotNull
    public ItemStack getResult() {
        return result;
    }

    public void setResult(@NotNull ItemStack result) {
        this.result = result;
    }
}
