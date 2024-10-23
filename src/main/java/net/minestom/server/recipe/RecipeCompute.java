package net.minestom.server.recipe;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Experimental
public final class RecipeCompute {

    public static @Nullable CraftResult searchCraft(List<Recipe> recipes, int width, int height, ItemStack[] items) {
        for (Recipe recipe : recipes) {
            final CraftResult result = switch (recipe.data()) {
                case Recipe.Shaped shaped -> searchShaped(shaped, width, height, items);
                case Recipe.Shapeless shapeless -> searchShapeless(shapeless, items);
                default -> null;
            };
            if (result != null) return result;
        }
        return null;
    }

    private static @Nullable CraftResult searchShaped(Recipe.Shaped recipe, int width, int height, ItemStack[] items) {
        // Verify if the inventory is too small for the recipe
        if (width < recipe.width() || height < recipe.height()) return null;
        // Verify if the inventory has items outside the recipe bound
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                final ItemStack item = items[x + y * width];
                if (item.isAir()) continue;
                if (x < minX) minX = x;
                if (y < minY) minY = y;
                if (x > maxX) maxX = x;
                if (y > maxY) maxY = y;
            }
        }
        if (maxX - minX + 1 > recipe.width() || maxY - minY + 1 > recipe.height()) return null;
        // Search for the recipe
        for (int x = 0; x <= width - recipe.width(); x++) {
            for (int y = 0; y <= height - recipe.height(); y++) {
                boolean found = true;
                for (int recipeX = 0; recipeX < recipe.width(); recipeX++) {
                    for (int recipeY = 0; recipeY < recipe.height(); recipeY++) {
                        boolean validIngredient = false;
                        final int ingredientIndex = recipeX + recipeY * recipe.width();
                        final Recipe.Ingredient ingredient = recipe.ingredients().get(ingredientIndex);
                        final ItemStack item = items[(x + recipeX) + (y + recipeY) * width];
                        if (ingredient.items().isEmpty() && item.isAir()) {
                            validIngredient = true;
                        } else {
                            for (Material ingredientItem : ingredient.items()) {
                                if (ingredientItem.equals(item.material())) {
                                    validIngredient = true;
                                    break;
                                }
                            }
                        }
                        if (!validIngredient) {
                            found = false;
                            break;
                        }
                    }
                    if (!found) break;
                }
                if (found) return new CraftResult(recipe.result(), possibleConsume(items));
            }
        }
        return null;
    }

    private static @Nullable CraftResult searchShapeless(Recipe.Shapeless recipe, ItemStack[] items) {
        // Count material occurrences
        Map<Material, Integer> materials = new HashMap<>();
        for (ItemStack item : items) {
            if (item.isAir()) continue;
            materials.put(item.material(), materials.getOrDefault(item.material(), 0) + 1);
        }
        if (materials.isEmpty()) return null;

        // Check if the recipe is valid
        for (Recipe.Ingredient ingredient : recipe.ingredients()) {
            boolean success = false;
            for (Material material : ingredient.items()) {
                final int occurrences = materials.getOrDefault(material, 0);
                if (occurrences == 0) continue;
                final int reduced = occurrences - 1;
                if (reduced > 0) {
                    materials.put(material, reduced);
                } else {
                    materials.remove(material);
                }
                success = true;
                break;
            }
            if (!success) return null;
        }
        if (!materials.isEmpty()) return null;

        return new CraftResult(recipe.result(), possibleConsume(items));
    }

    private static int possibleConsume(ItemStack[] items) {
        int smallestSize = Integer.MAX_VALUE;
        for (ItemStack item : items) {
            if (item.isAir()) continue;
            if (item.amount() < smallestSize) smallestSize = item.amount();
        }
        return smallestSize;
    }

    public record CraftResult(ItemStack item, int consumeAbility) {
    }
}
