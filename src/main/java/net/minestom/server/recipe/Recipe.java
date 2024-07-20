package net.minestom.server.recipe;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Recipe(@NotNull String id, @NotNull Data data) {
    public static final int MAX_INGREDIENTS = 128;

    sealed public interface Data {
        @NotNull RecipeType type();
    }

    public record Shaped(String group, RecipeCategory.Crafting category,
                         int width, int height, List<Ingredient> ingredients,
                         ItemStack result, boolean showNotification) implements Data {
        public static final NetworkBuffer.Type<Recipe.Shaped> SERIALIZER = RecipeSerializers.SHAPED;

        public Shaped {
            if (ingredients.size() != width * height)
                throw new IllegalArgumentException("Invalid shaped recipe, ingredients size must be equal to width * height");
            ingredients = List.copyOf(ingredients);
        }

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.SHAPED;
        }
    }

    public record Shapeless(String group, RecipeCategory.Crafting category,
                            List<Ingredient> ingredients, ItemStack result) implements Data {
        public static final NetworkBuffer.Type<Recipe.Shapeless> SERIALIZER = RecipeSerializers.SHAPELESS;

        public Shapeless {
            if (ingredients.size() > MAX_INGREDIENTS)
                throw new IllegalArgumentException("Shapeless recipe has too many ingredients");
            ingredients = List.copyOf(ingredients);
        }

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.SHAPELESS;
        }
    }

    public record Smelting(String group, RecipeCategory.Cooking category,
                           Ingredient ingredient, ItemStack result,
                           float experience, int cookingTime) implements Data {
        public static final NetworkBuffer.Type<Recipe.Smelting> SERIALIZER = RecipeSerializers.SMELTING;

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.SMELTING;
        }
    }

    public record Blasting(String group, RecipeCategory.Cooking category,
                           Ingredient ingredient, ItemStack result,
                           float experience, int cookingTime) implements Data {
        public static final NetworkBuffer.Type<Recipe.Blasting> SERIALIZER = RecipeSerializers.BLASTING;

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.BLASTING;
        }
    }

    public record Smoking(String group, RecipeCategory.Cooking category,
                          Ingredient ingredient, ItemStack result,
                          float experience, int cookingTime) implements Data {
        public static final NetworkBuffer.Type<Recipe.Smoking> SERIALIZER = RecipeSerializers.SMOKING;

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.SMOKING;
        }
    }

    public record CampfireCooking(String group, RecipeCategory.Cooking category,
                                  Ingredient ingredient, ItemStack result,
                                  float experience, int cookingTime) implements Data {
        public static final NetworkBuffer.Type<Recipe.CampfireCooking> SERIALIZER = RecipeSerializers.CAMPFIRE_COOKING;

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.CAMPFIRE_COOKING;
        }
    }

    public record Stonecutting(String group, Ingredient ingredient,
                               ItemStack result) implements Data {
        public static final NetworkBuffer.Type<Recipe.Stonecutting> SERIALIZER = RecipeSerializers.STONECUTTING;

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.STONECUTTING;
        }
    }

    public record SmithingTransform(Ingredient template, Ingredient base,
                                    Ingredient addition, ItemStack result) implements Data {
        public static final NetworkBuffer.Type<Recipe.SmithingTransform> SERIALIZER = RecipeSerializers.SMITHING_TRANSFORM;

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.SMITHING_TRANSFORM;
        }
    }

    public record SmithingTrim(Ingredient template,
                               Ingredient base, Ingredient addition) implements Data {
        public static final NetworkBuffer.Type<Recipe.SmithingTrim> SERIALIZER = RecipeSerializers.SMITHING_TRIM;

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.SMITHING_TRIM;
        }
    }

    public record Ingredient(@NotNull List<@NotNull ItemStack> items) {
        public Ingredient {
            items = List.copyOf(items);
        }

        public Ingredient(@NotNull ItemStack @NotNull ... items) {
            this(List.of(items));
        }
    }
}
