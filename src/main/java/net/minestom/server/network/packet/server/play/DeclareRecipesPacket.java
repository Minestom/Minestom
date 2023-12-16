package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.recipe.RecipeCategory;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record DeclareRecipesPacket(@NotNull List<DeclaredRecipe> recipes) implements ServerPacket {
    public DeclareRecipesPacket {
        recipes = List.copyOf(recipes);
    }

    public DeclareRecipesPacket(@NotNull NetworkBuffer reader) {
        this(reader.readCollection(r -> {
            final String type = r.read(STRING);
            return switch (type) {
                case "crafting_shapeless" -> new DeclaredShapelessCraftingRecipe(reader);
                case "crafting_shaped" -> new DeclaredShapedCraftingRecipe(reader);
                case "smelting" -> new DeclaredSmeltingRecipe(reader);
                case "blasting" -> new DeclaredBlastingRecipe(reader);
                case "smoking" -> new DeclaredSmokingRecipe(reader);
                case "campfire_cooking" -> new DeclaredCampfireCookingRecipe(reader);
                case "stonecutting" -> new DeclaredStonecutterRecipe(reader);
                case "smithing_trim" -> new DeclaredSmithingTrimRecipe(reader);
                case "smithing_transform" -> new DeclaredSmithingTransformRecipe(reader);
                default -> throw new UnsupportedOperationException("Unrecognized type: " + type);
            };
        }));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeCollection(recipes, (bWriter, recipe) -> {
            bWriter.write(STRING, recipe.type());
            bWriter.write(STRING, recipe.recipeId());
            bWriter.write(recipe);
        });
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.DECLARE_RECIPES;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }

    public sealed interface DeclaredRecipe extends NetworkBuffer.Writer
            permits DeclaredShapelessCraftingRecipe, DeclaredShapedCraftingRecipe,
            DeclaredSmeltingRecipe, DeclaredBlastingRecipe, DeclaredSmokingRecipe,
            DeclaredCampfireCookingRecipe, DeclaredStonecutterRecipe,
            DeclaredSmithingTrimRecipe, DeclaredSmithingTransformRecipe {
        @NotNull String type();

        @NotNull String recipeId();
    }

    public record DeclaredShapelessCraftingRecipe(@NotNull String recipeId, @NotNull String group,
                                                  @NotNull RecipeCategory.Crafting crafting,
                                                  @NotNull List<Ingredient> ingredients,
                                                  @NotNull ItemStack result) implements DeclaredRecipe {
        private DeclaredShapelessCraftingRecipe(@NotNull NetworkBuffer reader) {
            this(reader.read(STRING), reader.read(STRING),
                    reader.readEnum(RecipeCategory.Crafting.class),
                    reader.readCollection(Ingredient::new), reader.read(ITEM));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, group);
            writer.writeEnum(RecipeCategory.Crafting.class, crafting);
            writer.writeCollection(ingredients);
            writer.write(ITEM, result);
        }

        @Override
        public @NotNull String type() {
            return "crafting_shapeless";
        }
    }

    public record DeclaredShapedCraftingRecipe(@NotNull String recipeId, int width, int height,
                                               @NotNull String group, @NotNull RecipeCategory.Crafting category,
                                               @NotNull List<Ingredient> ingredients,
                                               @NotNull ItemStack result, boolean showNotification) implements DeclaredRecipe {
        public DeclaredShapedCraftingRecipe {
            ingredients = List.copyOf(ingredients);
        }

        private DeclaredShapedCraftingRecipe(DeclaredShapedCraftingRecipe packet) {
            this(packet.recipeId, packet.width, packet.height, packet.group, packet.category, packet.ingredients, packet.result, packet.showNotification);
        }

        public DeclaredShapedCraftingRecipe(@NotNull NetworkBuffer reader) {
            this(read(reader));
        }

        private static DeclaredShapedCraftingRecipe read(@NotNull NetworkBuffer reader) {

            String recipeId = reader.read(STRING);
            int width = reader.read(VAR_INT);
            int height = reader.read(VAR_INT);
            String group = reader.read(STRING);
            RecipeCategory.Crafting category = reader.readEnum(RecipeCategory.Crafting.class);
            List<Ingredient> ingredients = new ArrayList<>();
            for (int slot = 0; slot < width * height; slot++) {
                ingredients.add(new Ingredient(reader));
            }
            ItemStack result = reader.read(ITEM);
            boolean showNotification = reader.read(BOOLEAN);
            return new DeclaredShapedCraftingRecipe(recipeId, width, height, group, category, ingredients, result, showNotification);
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(VAR_INT, width);
            writer.write(VAR_INT, height);
            writer.write(STRING, group);
            writer.writeEnum(RecipeCategory.Crafting.class, category);
            for (Ingredient ingredient : ingredients) {
                ingredient.write(writer);
            }
            writer.write(ITEM, result);
            writer.write(BOOLEAN, showNotification);
        }

        @Override
        public @NotNull String type() {
            return "crafting_shaped";
        }
    }

    public record DeclaredSmeltingRecipe(@NotNull String recipeId, @NotNull String group,
                                         @NotNull RecipeCategory.Cooking category, @NotNull Ingredient ingredient,
                                         @NotNull ItemStack result, float experience,
                                         int cookingTime) implements DeclaredRecipe {
        public DeclaredSmeltingRecipe(@NotNull NetworkBuffer reader) {
            this(reader.read(STRING), reader.read(STRING),
                    reader.readEnum(RecipeCategory.Cooking.class),
                    new Ingredient(reader), reader.read(ITEM),
                    reader.read(FLOAT), reader.read(VAR_INT));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, group);
            writer.writeEnum(RecipeCategory.Cooking.class, category);
            writer.write(ingredient);
            writer.write(ITEM, result);
            writer.write(FLOAT, experience);
            writer.write(VAR_INT, cookingTime);
        }

        @Override
        public @NotNull String type() {
            return "smelting";
        }
    }

    public record DeclaredBlastingRecipe(@NotNull String recipeId, @NotNull String group,
                                         @NotNull RecipeCategory.Cooking category, @NotNull Ingredient ingredient,
                                         @NotNull ItemStack result, float experience,
                                         int cookingTime) implements DeclaredRecipe {
        public DeclaredBlastingRecipe(@NotNull NetworkBuffer reader) {
            this(reader.read(STRING), reader.read(STRING),
                    reader.readEnum(RecipeCategory.Cooking.class),
                    new Ingredient(reader), reader.read(ITEM),
                    reader.read(FLOAT), reader.read(VAR_INT));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, group);
            writer.writeEnum(RecipeCategory.Cooking.class, category);
            writer.write(ingredient);
            writer.write(ITEM, result);
            writer.write(FLOAT, experience);
            writer.write(VAR_INT, cookingTime);
        }

        @Override
        public @NotNull String type() {
            return "blasting";
        }
    }

    public record DeclaredSmokingRecipe(@NotNull String recipeId, @NotNull String group,
                                        @NotNull RecipeCategory.Cooking category, @NotNull Ingredient ingredient,
                                        @NotNull ItemStack result, float experience,
                                        int cookingTime) implements DeclaredRecipe {
        public DeclaredSmokingRecipe(@NotNull NetworkBuffer reader) {
            this(reader.read(STRING), reader.read(STRING),
                    reader.readEnum(RecipeCategory.Cooking.class),
                    new Ingredient(reader), reader.read(ITEM),
                    reader.read(FLOAT), reader.read(VAR_INT));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, group);
            writer.writeEnum(RecipeCategory.Cooking.class, category);
            writer.write(ingredient);
            writer.write(ITEM, result);
            writer.write(FLOAT, experience);
            writer.write(VAR_INT, cookingTime);
        }

        @Override
        public @NotNull String type() {
            return "smoking";
        }
    }

    public record DeclaredCampfireCookingRecipe(@NotNull String recipeId, @NotNull String group,
                                                @NotNull RecipeCategory.Cooking category, @NotNull Ingredient ingredient,
                                                @NotNull ItemStack result, float experience,
                                                int cookingTime) implements DeclaredRecipe {
        public DeclaredCampfireCookingRecipe(@NotNull NetworkBuffer reader) {
            this(reader.read(STRING), reader.read(STRING),
                    reader.readEnum(RecipeCategory.Cooking.class),
                    new Ingredient(reader), reader.read(ITEM),
                    reader.read(FLOAT), reader.read(VAR_INT));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, group);
            writer.writeEnum(RecipeCategory.Cooking.class, category);
            writer.write(ingredient);
            writer.write(ITEM, result);
            writer.write(FLOAT, experience);
            writer.write(VAR_INT, cookingTime);
        }

        @Override
        public @NotNull String type() {
            return "campfire_cooking";
        }
    }

    public record DeclaredStonecutterRecipe(String recipeId, String group,
                                            Ingredient ingredient, ItemStack result) implements DeclaredRecipe {
        public DeclaredStonecutterRecipe(@NotNull NetworkBuffer reader) {
            this(reader.read(STRING), reader.read(STRING),
                    new Ingredient(reader), reader.read(ITEM));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(STRING, group);
            writer.write(ingredient);
            writer.write(ITEM, result);
        }

        @Override
        public @NotNull String type() {
            return "stonecutting";
        }
    }

    public record DeclaredSmithingTransformRecipe(String recipeId, Ingredient template,
                                                  Ingredient base, Ingredient addition,
                                                  ItemStack result) implements DeclaredRecipe {
        public DeclaredSmithingTransformRecipe(@NotNull NetworkBuffer reader) {
            this(reader.read(STRING), new Ingredient(reader), new Ingredient(reader), new Ingredient(reader), reader.read(ITEM));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(template);
            writer.write(base);
            writer.write(addition);
            writer.write(ITEM, result);
        }

        @Override
        public @NotNull String type() {
            return "smithing_transform";
        }
    }

    public record DeclaredSmithingTrimRecipe(String recipeId, Ingredient template,
                                             Ingredient base, Ingredient addition) implements DeclaredRecipe {
        public DeclaredSmithingTrimRecipe(@NotNull NetworkBuffer reader) {
            this(reader.read(STRING), new Ingredient(reader), new Ingredient(reader), new Ingredient(reader));
        }

        @Override
        public void write(@NotNull NetworkBuffer writer) {
            writer.write(template);
            writer.write(base);
            writer.write(addition);
        }

        @Override
        public @NotNull String type() {
            return "smithing_trim";
        }
    }

    public record Ingredient(@Nullable List<ItemStack> items) implements NetworkBuffer.Writer {
        public Ingredient {
            items = items == null ? null : List.copyOf(items);
        }

        public Ingredient(@NotNull NetworkBuffer reader) {
            this(reader.readCollection(ITEM));
        }

        public void write(@NotNull NetworkBuffer writer) {
            writer.writeCollection(ITEM, items);
        }
    }
}
