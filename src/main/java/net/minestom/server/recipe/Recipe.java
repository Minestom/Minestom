package net.minestom.server.recipe;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Recipe(@NotNull String id, @NotNull Data data) {
    public static final int MAX_INGREDIENTS = 128;

    sealed public interface Data {
    }

    public record Shaped(String group, RecipeCategory.Crafting category,
                         int width, int height, List<Ingredient> ingredients,
                         ItemStack result, boolean showNotification) implements Data {
        public static final NetworkBuffer.Type<Shaped> SERIALIZER = RecipeSerializers.SHAPED;

        public Shaped {
            if (ingredients.size() != width * height)
                throw new IllegalArgumentException("Invalid shaped recipe, ingredients size must be equal to width * height");
            ingredients = List.copyOf(ingredients);
        }
    }

    public record Shapeless(String group, RecipeCategory.Crafting category,
                            List<Ingredient> ingredients, ItemStack result) implements Data {
        public static final NetworkBuffer.Type<Shapeless> SERIALIZER = RecipeSerializers.SHAPELESS;

        public Shapeless {
            if (ingredients.size() > MAX_INGREDIENTS)
                throw new IllegalArgumentException("Shapeless recipe has too many ingredients");
            ingredients = List.copyOf(ingredients);
        }
    }

    public record Smelting(String group, RecipeCategory.Cooking category,
                           Ingredient ingredient, ItemStack result,
                           float experience, int cookingTime) implements Data {
        public static final NetworkBuffer.Type<Smelting> SERIALIZER = RecipeSerializers.SMELTING;
    }

    public record Blasting(String group, RecipeCategory.Cooking category,
                           Ingredient ingredient, ItemStack result,
                           float experience, int cookingTime) implements Data {
        public static final NetworkBuffer.Type<Blasting> SERIALIZER = RecipeSerializers.BLASTING;
    }

    public record Smoking(String group, RecipeCategory.Cooking category,
                          Ingredient ingredient, ItemStack result,
                          float experience, int cookingTime) implements Data {
        public static final NetworkBuffer.Type<Smoking> SERIALIZER = RecipeSerializers.SMOKING;
    }

    public record CampfireCooking(String group, RecipeCategory.Cooking category,
                                  Ingredient ingredient, ItemStack result,
                                  float experience, int cookingTime) implements Data {
        public static final NetworkBuffer.Type<CampfireCooking> SERIALIZER = RecipeSerializers.CAMPFIRE_COOKING;
    }

    public record Stonecutting(String group, Ingredient ingredient,
                               ItemStack result) implements Data {
        public static final NetworkBuffer.Type<Stonecutting> SERIALIZER = RecipeSerializers.STONECUTTING;
    }

    public record SmithingTransform(Ingredient template, Ingredient base,
                                    Ingredient addition, ItemStack result) implements Data {
        public static final NetworkBuffer.Type<SmithingTransform> SERIALIZER = RecipeSerializers.SMITHING_TRANSFORM;
    }

    public record SmithingTrim(Ingredient template,
                               Ingredient base, Ingredient addition) implements Data {
        public static final NetworkBuffer.Type<SmithingTrim> SERIALIZER = RecipeSerializers.SMITHING_TRIM;
    }

    public record Ingredient(@NotNull List<@NotNull ItemStack> items) {
        public Ingredient {
            items = List.copyOf(items);
        }

        public Ingredient(@NotNull ItemStack @NotNull ... items) {
            this(List.of(items));
        }
    }

    public record SpecialArmorDye(RecipeCategory.Crafting category) implements Data {
        public static final NetworkBuffer.Type<SpecialArmorDye> SERIALIZER = RecipeSerializers.ARMOR_DYE;
    }

    public record SpecialBookCloning(RecipeCategory.Crafting category) implements Data {
        public static final NetworkBuffer.Type<SpecialBookCloning> SERIALIZER = RecipeSerializers.BOOK_CLONING;
    }

    public record SpecialMapCloning(RecipeCategory.Crafting category) implements Data {
        public static final NetworkBuffer.Type<SpecialMapCloning> SERIALIZER = RecipeSerializers.MAP_CLONING;
    }

    public record SpecialMapExtending(RecipeCategory.Crafting category) implements Data {
        public static final NetworkBuffer.Type<SpecialMapExtending> SERIALIZER = RecipeSerializers.MAP_EXTENDING;
    }

    public record SpecialFireworkRocket(RecipeCategory.Crafting category) implements Data {
        public static final NetworkBuffer.Type<SpecialFireworkRocket> SERIALIZER = RecipeSerializers.FIREWORK_ROCKET;
    }

    public record SpecialFireworkStar(RecipeCategory.Crafting category) implements Data {
        public static final NetworkBuffer.Type<SpecialFireworkStar> SERIALIZER = RecipeSerializers.FIREWORK_STAR;
    }

    public record SpecialFireworkStarFade(RecipeCategory.Crafting category) implements Data {
        public static final NetworkBuffer.Type<SpecialFireworkStarFade> SERIALIZER = RecipeSerializers.FIREWORK_STAR_FADE;
    }

    public record SpecialTippedArrow(RecipeCategory.Crafting category) implements Data {
        public static final NetworkBuffer.Type<SpecialTippedArrow> SERIALIZER = RecipeSerializers.TIPPED_ARROW;
    }

    public record SpecialBannerDuplicate(RecipeCategory.Crafting category) implements Data {
        public static final NetworkBuffer.Type<SpecialBannerDuplicate> SERIALIZER = RecipeSerializers.BANNER_DUPLICATE;
    }

    public record SpecialShieldDecoration(RecipeCategory.Crafting category) implements Data {
        public static final NetworkBuffer.Type<SpecialShieldDecoration> SERIALIZER = RecipeSerializers.SHIELD_DECORATION;
    }

    public record SpecialShulkerBoxColoring(RecipeCategory.Crafting category) implements Data {
        public static final NetworkBuffer.Type<SpecialShulkerBoxColoring> SERIALIZER = RecipeSerializers.SPECIAL_SHULKER_BOX_COLORING;
    }

    public record SpecialSuspiciousStew(RecipeCategory.Crafting category) implements Data {
        public static final NetworkBuffer.Type<SpecialSuspiciousStew> SERIALIZER = RecipeSerializers.SUSPICIOUS_STEW;
    }

    public record SpecialRepairItem(RecipeCategory.Crafting category) implements Data {
        public static final NetworkBuffer.Type<SpecialRepairItem> SERIALIZER = RecipeSerializers.REPAIR_ITEM;
    }

    public record DecoratedPot(RecipeCategory.Crafting category) implements Data {
        public static final NetworkBuffer.Type<DecoratedPot> SERIALIZER = RecipeSerializers.DECORATED_POT;
    }
}
