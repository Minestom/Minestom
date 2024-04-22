package net.minestom.server.recipe;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.DeclareRecipesPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public abstract class ShapedRecipe extends Recipe {
    private final int width;
    private final int height;
    private String group;
    private RecipeCategory.Crafting category;
    private final List<DeclareRecipesPacket.Ingredient> ingredients;
    private ItemStack result;
    private boolean showNotification;

    protected ShapedRecipe(@NotNull String recipeId,
                           int width,
                           int height,
                           @NotNull String group,
                            @NotNull RecipeCategory.Crafting category,
                           @Nullable List<DeclareRecipesPacket.Ingredient> ingredients,
                           @NotNull ItemStack result, boolean showNotification) {
        super(Type.SHAPED, recipeId);
        this.width = width;
        this.height = height;
        this.group = group;
        this.category = category;
        this.ingredients = Objects.requireNonNullElseGet(ingredients, LinkedList::new);
        this.result = result;
        this.showNotification = showNotification;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @NotNull
    public String getGroup() {
        return group;
    }

    public void setGroup(@NotNull String group) {
        this.group = group;
    }

    @NotNull
    public RecipeCategory.Crafting getCategory() {
        return category;
    }

    public void setCategory(@NotNull RecipeCategory.Crafting category) {
        this.category = category;
    }

    public void addIngredient(DeclareRecipesPacket.Ingredient ingredient) {
        if (ingredients.size() + 1 > width * height) {
            throw new IndexOutOfBoundsException("You cannot add more ingredients than width*height");
        }

        ingredients.add(ingredient);
    }

    @NotNull
    public List<DeclareRecipesPacket.Ingredient> getIngredients() {
        return ingredients;
    }

    @NotNull
    public ItemStack getResult() {
        return result;
    }

    public void setResult(@NotNull ItemStack result) {
        this.result = result;
    }

    public boolean getShowNotification() {
        return showNotification;
    }

    public void setShowNotification(boolean showNotification) {
        this.showNotification = showNotification;
    }
}
