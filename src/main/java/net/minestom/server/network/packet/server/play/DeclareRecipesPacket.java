package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

import java.util.List;

public record DeclareRecipesPacket(@NotNull List<DeclaredRecipe> recipes) implements ServerPacket {
    public DeclareRecipesPacket {
        recipes = List.copyOf(recipes);
    }

    public DeclareRecipesPacket(BinaryReader reader) {
        this(reader.readVarIntList(r -> {
            final String type = r.readSizedString();
            return switch (type) {
                case "crafting_shapeless" -> new DeclaredShapelessCraftingRecipe(reader);
                case "crafting_shaped" -> new DeclaredShapedCraftingRecipe(reader);
                case "smelting" -> new DeclaredSmeltingRecipe(reader);
                case "blasting" -> new DeclaredBlastingRecipe(reader);
                case "smoking" -> new DeclaredSmokingRecipe(reader);
                case "campfire_cooking" -> new DeclaredCampfireCookingRecipe(reader);
                case "stonecutter" -> new DeclaredStonecutterRecipe(reader);
                case "smithing" -> new DeclaredSmithingRecipe(reader);
                default -> throw new UnsupportedOperationException("Unrecognized type: " + type);
            };
        }));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarIntList(recipes, (bWriter, recipe)->{
            bWriter.writeSizedString(recipe.type());
            bWriter.writeSizedString(recipe.recipeId());
            bWriter.write(recipe);
        });
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DECLARE_RECIPES;
    }

    public sealed interface DeclaredRecipe extends Writeable
            permits DeclaredShapelessCraftingRecipe, DeclaredShapedCraftingRecipe,
            DeclaredSmeltingRecipe, DeclaredBlastingRecipe, DeclaredSmokingRecipe,
            DeclaredCampfireCookingRecipe, DeclaredStonecutterRecipe, DeclaredSmithingRecipe {
        @NotNull String type();

        @NotNull String recipeId();
    }

    public record DeclaredShapelessCraftingRecipe(String recipeId, String group,
                                                  List<Ingredient> ingredients,
                                                  ItemStack result) implements DeclaredRecipe {
        private DeclaredShapelessCraftingRecipe(@NotNull BinaryReader reader) {
            this(reader.readSizedString(), reader.readSizedString(),
                    reader.readVarIntList(Ingredient::new), reader.readItemStack());
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(group);
            writer.writeVarIntList(ingredients, BinaryWriter::write);
            writer.writeItemStack(result);
        }

        @Override
        public @NotNull String type() {
            return "crafting_shapeless";
        }
    }

    public record DeclaredShapedCraftingRecipe(@NotNull String recipeId, int width, int height,
                                               @NotNull String group, @NotNull List<Ingredient> ingredients,
                                               @NotNull ItemStack result) implements DeclaredRecipe {
        public DeclaredShapedCraftingRecipe {
            ingredients = List.copyOf(ingredients);
        }

        private DeclaredShapedCraftingRecipe(DeclaredShapedCraftingRecipe packet) {
            this(packet.recipeId, packet.width, packet.height, packet.group, packet.ingredients, packet.result);
        }

        public DeclaredShapedCraftingRecipe(BinaryReader reader) {
            this(read(reader));
        }

        private static DeclaredShapedCraftingRecipe read(BinaryReader reader) {

            String recipeId = reader.readSizedString();
            int width = reader.readVarInt();
            int height = reader.readVarInt();
            String group = reader.readSizedString();
            List<Ingredient> ingredients = new ArrayList<>();
            for (int slot = 0; slot < width * height; slot++) {
                ingredients.add(new Ingredient(reader));
            }
            ItemStack result = reader.readItemStack();
            return new DeclaredShapedCraftingRecipe(recipeId, width, height, group, ingredients, result);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeVarInt(width);
            writer.writeVarInt(height);
            writer.writeSizedString(group);
            for (Ingredient ingredient : ingredients) {
                ingredient.write(writer);
            }
            writer.writeItemStack(result);
        }

        @Override
        public @NotNull String type() {
            return "crafting_shaped";
        }
    }

    public record DeclaredSmeltingRecipe(@NotNull String recipeId, @NotNull String group,
                                         @NotNull Ingredient ingredient, @NotNull ItemStack result,
                                         float experience, int cookingTime) implements DeclaredRecipe {
        public DeclaredSmeltingRecipe(BinaryReader reader) {
            this(reader.readSizedString(), reader.readSizedString(),
                    new Ingredient(reader), reader.readItemStack(),
                    reader.readFloat(), reader.readVarInt());
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(group);
            writer.write(ingredient);
            writer.writeItemStack(result);
            writer.writeFloat(experience);
            writer.writeVarInt(cookingTime);
        }

        @Override
        public @NotNull String type() {
            return "smelting";
        }
    }

    public record DeclaredBlastingRecipe(@NotNull String recipeId, @NotNull String group,
                                         @NotNull Ingredient ingredient, @NotNull ItemStack result,
                                         float experience, int cookingTime) implements DeclaredRecipe {
        public DeclaredBlastingRecipe(BinaryReader reader) {
            this(reader.readSizedString(), reader.readSizedString(),
                    new Ingredient(reader), reader.readItemStack(),
                    reader.readFloat(), reader.readVarInt());
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(group);
            writer.write(ingredient);
            writer.writeItemStack(result);
            writer.writeFloat(experience);
            writer.writeVarInt(cookingTime);
        }

        @Override
        public @NotNull String type() {
            return "blasting";
        }
    }

    public record DeclaredSmokingRecipe(@NotNull String recipeId, @NotNull String group,
                                        @NotNull Ingredient ingredient, @NotNull ItemStack result,
                                        float experience, int cookingTime) implements DeclaredRecipe {
        public DeclaredSmokingRecipe(BinaryReader reader) {
            this(reader.readSizedString(), reader.readSizedString(),
                    new Ingredient(reader), reader.readItemStack(),
                    reader.readFloat(), reader.readVarInt());
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(group);
            writer.write(ingredient);
            writer.writeItemStack(result);
            writer.writeFloat(experience);
            writer.writeVarInt(cookingTime);
        }

        @Override
        public @NotNull String type() {
            return "smoking";
        }
    }

    public record DeclaredCampfireCookingRecipe(@NotNull String recipeId, @NotNull String group,
                                                @NotNull Ingredient ingredient, @NotNull ItemStack result,
                                                float experience, int cookingTime) implements DeclaredRecipe {
        public DeclaredCampfireCookingRecipe(BinaryReader reader) {
            this(reader.readSizedString(), reader.readSizedString(),
                    new Ingredient(reader), reader.readItemStack(),
                    reader.readFloat(), reader.readVarInt());
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(group);
            writer.write(ingredient);
            writer.writeItemStack(result);
            writer.writeFloat(experience);
            writer.writeVarInt(cookingTime);
        }

        @Override
        public @NotNull String type() {
            return "campfire_cooking";
        }
    }

    public record DeclaredStonecutterRecipe(String recipeId, String group,
                                            Ingredient ingredient, ItemStack result) implements DeclaredRecipe {
        public DeclaredStonecutterRecipe(@NotNull BinaryReader reader) {
            this(reader.readSizedString(), reader.readSizedString(),
                    new Ingredient(reader), reader.readItemStack());
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(group);
            writer.write(ingredient);
            writer.writeItemStack(result);
        }

        @Override
        public @NotNull String type() {
            return "stonecutter";
        }
    }

    public record DeclaredSmithingRecipe(String recipeId, Ingredient base, Ingredient addition,
                                         ItemStack result) implements DeclaredRecipe {
        public DeclaredSmithingRecipe(@NotNull BinaryReader reader) {
            this(reader.readSizedString(), new Ingredient(reader), new Ingredient(reader), reader.readItemStack());
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            writer.write(base);
            writer.write(addition);
            writer.writeItemStack(result);
        }

        @Override
        public @NotNull String type() {
            return "smithing";
        }
    }

    public record Ingredient(@NotNull List<ItemStack> items) implements Writeable {
        public Ingredient {
            items = List.copyOf(items);
        }

        public Ingredient(BinaryReader reader) {
            this(reader.readVarIntList(BinaryReader::readItemStack));
        }

        public void write(BinaryWriter writer) {
            writer.writeVarIntList(items, BinaryWriter::writeItemStack);
        }
    }
}
