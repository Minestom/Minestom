package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.recipe.RecipeCategory;
import net.minestom.server.recipe.RecipeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record DeclareRecipesPacket(@NotNull List<DeclaredRecipe> recipes) implements ServerPacket.Play {
    public static final int MAX_RECIPES = Short.MAX_VALUE;
    public static final int MAX_INGREDIENTS = 128;

    public DeclareRecipesPacket {
        recipes = List.copyOf(recipes);
    }

    public DeclareRecipesPacket(@NotNull NetworkBuffer reader) {
        this(reader.readCollection(r -> {
            final String recipeId = r.read(STRING);
            final RecipeType type = r.read(RecipeType.NETWORK_TYPE);
            return switch (type) {
                case RecipeType.SHAPELESS -> new DeclaredShapelessCraftingRecipe(recipeId, reader);
                case RecipeType.SHAPED -> new DeclaredShapedCraftingRecipe(recipeId, reader);
                case RecipeType.SMELTING -> new DeclaredSmeltingRecipe(recipeId, reader);
                case RecipeType.BLASTING -> new DeclaredBlastingRecipe(recipeId, reader);
                case RecipeType.SMOKING -> new DeclaredSmokingRecipe(recipeId, reader);
                case RecipeType.CAMPFIRE_COOKING -> new DeclaredCampfireCookingRecipe(recipeId, reader);
                case RecipeType.STONECUTTING -> new DeclaredStonecutterRecipe(recipeId, reader);
                case RecipeType.SMITHING_TRIM -> new DeclaredSmithingTrimRecipe(recipeId, reader);
                case RecipeType.SMITHING_TRANSFORM -> new DeclaredSmithingTransformRecipe(recipeId, reader);
                default -> throw new UnsupportedOperationException("Unrecognized type: " + type);
            };
        }, MAX_RECIPES));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeCollection(recipes, (bWriter, recipe) -> {
            bWriter.write(STRING, recipe.recipeId());
            bWriter.write(RecipeType.NETWORK_TYPE, recipe.type());
            bWriter.write(recipe);
        });
    }

    @Override
    public int playId() {
        return ServerPacketIdentifier.DECLARE_RECIPES;
    }

    public sealed interface DeclaredRecipe extends NetworkBuffer.Writer
            permits DeclaredShapelessCraftingRecipe, DeclaredShapedCraftingRecipe,
            DeclaredSmeltingRecipe, DeclaredBlastingRecipe, DeclaredSmokingRecipe,
            DeclaredCampfireCookingRecipe, DeclaredStonecutterRecipe,
            DeclaredSmithingTrimRecipe, DeclaredSmithingTransformRecipe {
        @NotNull RecipeType type();

        @NotNull String recipeId();
    }

    public record DeclaredShapelessCraftingRecipe(@NotNull String recipeId, @NotNull String group,
                                                  @NotNull RecipeCategory.Crafting crafting,
                                                  @NotNull List<Ingredient> ingredients,
                                                  @NotNull ItemStack result) implements DeclaredRecipe {
        private DeclaredShapelessCraftingRecipe(@NotNull String recipeId, @NotNull NetworkBuffer reader) {
            this(recipeId, reader.read(STRING),
                    reader.readEnum(RecipeCategory.Crafting.class),
                    reader.readCollection(Ingredient::new, MAX_INGREDIENTS), reader.read(ItemStack.STRICT_NETWORK_TYPE));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, group);
            writer.writeEnum(RecipeCategory.Crafting.class, crafting);
            writer.writeCollection(ingredients);
            writer.write(ItemStack.STRICT_NETWORK_TYPE, result);
        }

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.SHAPELESS;
        }
    }

    public record DeclaredShapedCraftingRecipe(@NotNull String recipeId,
                                               @NotNull String group, @NotNull RecipeCategory.Crafting category,
                                               int width, int height, @NotNull List<Ingredient> ingredients,
                                               @NotNull ItemStack result, boolean showNotification) implements DeclaredRecipe {
        public DeclaredShapedCraftingRecipe {
            ingredients = List.copyOf(ingredients);
        }

        private DeclaredShapedCraftingRecipe(DeclaredShapedCraftingRecipe packet) {
            this(packet.recipeId, packet.group, packet.category, packet.width, packet.height, packet.ingredients, packet.result, packet.showNotification);
        }

        public DeclaredShapedCraftingRecipe(@NotNull String recipeId, @NotNull NetworkBuffer reader) {
            this(read(recipeId, reader));
        }

        private static DeclaredShapedCraftingRecipe read(@NotNull String recipeId, @NotNull NetworkBuffer reader) {
            String group = reader.read(STRING);
            RecipeCategory.Crafting category = reader.readEnum(RecipeCategory.Crafting.class);
            int width = reader.read(VAR_INT);
            int height = reader.read(VAR_INT);
            List<Ingredient> ingredients = new ArrayList<>();
            for (int slot = 0; slot < width * height; slot++) {
                ingredients.add(new Ingredient(reader));
            }
            ItemStack result = reader.read(ItemStack.STRICT_NETWORK_TYPE);
            boolean showNotification = reader.read(BOOLEAN);
            return new DeclaredShapedCraftingRecipe(recipeId, group, category, width, height, ingredients, result, showNotification);
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, group);
            writer.writeEnum(RecipeCategory.Crafting.class, category);
            writer.write(VAR_INT, width);
            writer.write(VAR_INT, height);
            for (Ingredient ingredient : ingredients) {
                ingredient.write(writer);
            }
            writer.write(ItemStack.STRICT_NETWORK_TYPE, result);
            writer.write(BOOLEAN, showNotification);
        }

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.SHAPED;
        }
    }

    public record DeclaredSmeltingRecipe(@NotNull String recipeId, @NotNull String group,
                                         @NotNull RecipeCategory.Cooking category, @NotNull Ingredient ingredient,
                                         @NotNull ItemStack result, float experience,
                                         int cookingTime) implements DeclaredRecipe {
        public DeclaredSmeltingRecipe(@NotNull String recipeId, @NotNull NetworkBuffer reader) {
            this(recipeId, reader.read(STRING),
                    reader.readEnum(RecipeCategory.Cooking.class),
                    new Ingredient(reader), reader.read(ItemStack.STRICT_NETWORK_TYPE),
                    reader.read(FLOAT), reader.read(VAR_INT));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, group);
            writer.writeEnum(RecipeCategory.Cooking.class, category);
            writer.write(ingredient);
            writer.write(ItemStack.STRICT_NETWORK_TYPE, result);
            writer.write(FLOAT, experience);
            writer.write(VAR_INT, cookingTime);
        }

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.SMELTING;
        }
    }

    public record DeclaredBlastingRecipe(@NotNull String recipeId, @NotNull String group,
                                         @NotNull RecipeCategory.Cooking category, @NotNull Ingredient ingredient,
                                         @NotNull ItemStack result, float experience,
                                         int cookingTime) implements DeclaredRecipe {
        public DeclaredBlastingRecipe(@NotNull String recipeId, @NotNull NetworkBuffer reader) {
            this(recipeId, reader.read(STRING),
                    reader.readEnum(RecipeCategory.Cooking.class),
                    new Ingredient(reader), reader.read(ItemStack.STRICT_NETWORK_TYPE),
                    reader.read(FLOAT), reader.read(VAR_INT));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, group);
            writer.writeEnum(RecipeCategory.Cooking.class, category);
            writer.write(ingredient);
            writer.write(ItemStack.STRICT_NETWORK_TYPE, result);
            writer.write(FLOAT, experience);
            writer.write(VAR_INT, cookingTime);
        }

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.BLASTING;
        }
    }

    public record DeclaredSmokingRecipe(@NotNull String recipeId, @NotNull String group,
                                        @NotNull RecipeCategory.Cooking category, @NotNull Ingredient ingredient,
                                        @NotNull ItemStack result, float experience,
                                        int cookingTime) implements DeclaredRecipe {
        public DeclaredSmokingRecipe(@NotNull String recipeId, @NotNull NetworkBuffer reader) {
            this(recipeId, reader.read(STRING),
                    reader.readEnum(RecipeCategory.Cooking.class),
                    new Ingredient(reader), reader.read(ItemStack.STRICT_NETWORK_TYPE),
                    reader.read(FLOAT), reader.read(VAR_INT));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, group);
            writer.writeEnum(RecipeCategory.Cooking.class, category);
            writer.write(ingredient);
            writer.write(ItemStack.STRICT_NETWORK_TYPE, result);
            writer.write(FLOAT, experience);
            writer.write(VAR_INT, cookingTime);
        }

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.SMOKING;
        }
    }

    public record DeclaredCampfireCookingRecipe(@NotNull String recipeId, @NotNull String group,
                                                @NotNull RecipeCategory.Cooking category, @NotNull Ingredient ingredient,
                                                @NotNull ItemStack result, float experience,
                                                int cookingTime) implements DeclaredRecipe {
        public DeclaredCampfireCookingRecipe(@NotNull String recipeId, @NotNull NetworkBuffer reader) {
            this(recipeId, reader.read(STRING),
                    reader.readEnum(RecipeCategory.Cooking.class),
                    new Ingredient(reader), reader.read(ItemStack.STRICT_NETWORK_TYPE),
                    reader.read(FLOAT), reader.read(VAR_INT));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, group);
            writer.writeEnum(RecipeCategory.Cooking.class, category);
            writer.write(ingredient);
            writer.write(ItemStack.STRICT_NETWORK_TYPE, result);
            writer.write(FLOAT, experience);
            writer.write(VAR_INT, cookingTime);
        }

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.CAMPFIRE_COOKING;
        }
    }

    public record DeclaredStonecutterRecipe(String recipeId, String group,
                                            Ingredient ingredient, ItemStack result) implements DeclaredRecipe {
        public DeclaredStonecutterRecipe(@NotNull String recipeId, @NotNull NetworkBuffer reader) {
            this(recipeId, reader.read(STRING),
                    new Ingredient(reader), reader.read(ItemStack.STRICT_NETWORK_TYPE));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, group);
            writer.write(ingredient);
            writer.write(ItemStack.STRICT_NETWORK_TYPE, result);
        }

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.STONECUTTING;
        }
    }

    public record DeclaredSmithingTransformRecipe(String recipeId, Ingredient template,
                                                  Ingredient base, Ingredient addition,
                                                  ItemStack result) implements DeclaredRecipe {
        public DeclaredSmithingTransformRecipe(@NotNull String recipeId, @NotNull NetworkBuffer reader) {
            this(recipeId, new Ingredient(reader), new Ingredient(reader), new Ingredient(reader), reader.read(ItemStack.STRICT_NETWORK_TYPE));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(template);
            writer.write(base);
            writer.write(addition);
            writer.write(ItemStack.STRICT_NETWORK_TYPE, result);
        }

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.SMITHING_TRANSFORM;
        }
    }

    public record DeclaredSmithingTrimRecipe(String recipeId, Ingredient template,
                                             Ingredient base, Ingredient addition) implements DeclaredRecipe {
        public DeclaredSmithingTrimRecipe(@NotNull String recipeId, @NotNull NetworkBuffer reader) {
            this(recipeId, new Ingredient(reader), new Ingredient(reader), new Ingredient(reader));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(template);
            writer.write(base);
            writer.write(addition);
        }

        @Override
        public @NotNull RecipeType type() {
            return RecipeType.SMITHING_TRIM;
        }
    }

    public record Ingredient(@Nullable List<ItemStack> items) implements NetworkBuffer.Writer {
        public Ingredient {
            items = items == null ? null : List.copyOf(items);
        }

        public Ingredient(@NotNull NetworkBuffer reader) {
            this(reader.readCollection(ItemStack.STRICT_NETWORK_TYPE, MAX_INGREDIENTS));
        }

        public void write(@NotNull NetworkBuffer writer) {
            writer.writeCollection(ItemStack.STRICT_NETWORK_TYPE, items);
        }
    }
}
