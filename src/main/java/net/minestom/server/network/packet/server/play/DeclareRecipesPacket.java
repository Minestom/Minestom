package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.validate.Check;

public class DeclareRecipesPacket implements ServerPacket {

    public Recipe[] recipes;

    @Override
    public void write(BinaryWriter writer) {
        Check.notNull(recipes, "Recipes cannot be null!");

        writer.writeVarInt(recipes.length);
        for (Recipe recipe : recipes) {
            recipe.write(writer);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DECLARE_RECIPES;
    }

    public static class Recipe {

        public String recipeId;
        public String recipeType;

        public String group;

        // crafting_shapeless
        // ++ group
        // ++ ingredients
        // ++ result

        // crafting_shaped
        public int width;
        public int height;
        // ++ group
        // ++ ingredients
        // ++ result

        // smelting, blasting, smoking and campfire
        // ++ group
        // ++ ingredient
        // ++ result
        public float experience;
        public int cookingTime;

        // smithing
        // ++ ingredient (base)
        public Ingredient additionIngredient;
        // ++ result


        public Ingredient ingredient;
        public Ingredient[] ingredients;
        public ItemStack result;


        private void write(BinaryWriter writer) {
            writer.writeSizedString(recipeType);
            writer.writeSizedString(recipeId);

            switch (recipeType) {
                case "crafting_shapeless": {
                    writer.writeSizedString(group);
                    writer.writeVarInt(ingredients.length);
                    for (Ingredient ingredient : ingredients) {
                        ingredient.write(writer);
                    }
                    writer.writeItemStack(result);
                    break;
                }
                case "crafting_shaped": {
                    writer.writeVarInt(width);
                    writer.writeVarInt(height);
                    writer.writeSizedString(group);
                    for (Ingredient ingredient : ingredients) {
                        ingredient.write(writer);
                    }
                    writer.writeItemStack(result);
                    break;
                }
                case "smelting":
                case "blasting":
                case "smoking":
                case "campfire_cooking": {
                    writer.writeSizedString(group);
                    ingredient.write(writer);
                    writer.writeItemStack(result);
                    writer.writeFloat(experience);
                    writer.writeVarInt(cookingTime);
                    break;
                }
                case "stonecutting": {
                    writer.writeSizedString(group);
                    ingredient.write(writer);
                    writer.writeItemStack(result);
                    break;
                }
                case "smithing": {
                    ingredient.write(writer);
                    additionIngredient.write(writer);
                    writer.writeItemStack(result);
                    break;
                }
            }
        }
    }

    public static class Ingredient {

        // The count of each item should be 1
        public ItemStack[] items;

        private void write(BinaryWriter writer) {
            writer.writeVarInt(items.length);
            for (ItemStack itemStack : items) {
                writer.writeItemStack(itemStack);
            }
        }

    }
}
