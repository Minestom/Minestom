package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Readable;
import net.minestom.server.utils.binary.Writeable;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public class DeclareRecipesPacket implements ServerPacket {

    public DeclaredRecipe[] recipes = new DeclaredRecipe[0];

    public DeclareRecipesPacket() {
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        Check.notNull(recipes, "Recipes cannot be null!");

        writer.writeVarInt(recipes.length);
        for (DeclaredRecipe recipe : recipes) {
            recipe.write(writer);
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        int recipeCount = reader.readVarInt();
        recipes = new DeclaredRecipe[recipeCount];
        for (int i = 0; i < recipeCount; i++) {
            String type = reader.readSizedString();
            String id = reader.readSizedString();

            switch (type) {
                case "crafting_shapeless" -> recipes[i] = new DeclaredShapelessCraftingRecipe(id, reader);
                case "crafting_shaped" -> recipes[i] = new DeclaredShapedCraftingRecipe(id, reader);
                case "smelting" -> recipes[i] = new DeclaredSmeltingRecipe(id, reader);
                case "blasting" -> recipes[i] = new DeclaredBlastingRecipe(id, reader);
                case "smoking" -> recipes[i] = new DeclaredSmokingRecipe(id, reader);
                case "campfire_cooking" -> recipes[i] = new DeclaredCampfireCookingRecipe(id, reader);
                case "stonecutter" -> recipes[i] = new DeclaredStonecutterRecipe(id, reader);
                case "smithing" -> recipes[i] = new DeclaredSmithingRecipe(id, reader);
                default -> throw new UnsupportedOperationException("Unrecognized type: " + type + " (id is " + id + ")");
            }
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.DECLARE_RECIPES;
    }

    public abstract static class DeclaredRecipe implements Writeable, Readable {
        protected final String recipeId;
        protected final String recipeType;

        protected DeclaredRecipe(@NotNull String recipeId, @NotNull String recipeType) {
            this.recipeId = recipeId;
            this.recipeType = recipeType;
        }

        public void write(@NotNull BinaryWriter writer) {
            writer.writeSizedString(recipeType);
            writer.writeSizedString(recipeId);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            throw new UnsupportedOperationException("'read' must be implemented inside subclasses!");
        }
    }

    public static class DeclaredShapelessCraftingRecipe extends DeclaredRecipe {
        private String group;
        private Ingredient[] ingredients;
        private ItemStack result;

        public DeclaredShapelessCraftingRecipe(
                @NotNull String recipeId,
                @NotNull String group,
                @NotNull Ingredient[] ingredients,
                @NotNull ItemStack result
        ) {
            super(recipeId, "crafting_shapeless");
            this.group = group;
            this.ingredients = ingredients;
            this.result = result;
        }

        private DeclaredShapelessCraftingRecipe(@NotNull String recipeId, @NotNull BinaryReader reader) {
            super(recipeId, "crafting_shapeless");
            read(reader);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            // Write type & id
            super.write(writer);
            // Write recipe specific stuff.
            writer.writeSizedString(group);
            writer.writeVarInt(ingredients.length);
            for (Ingredient ingredient : ingredients) {
                ingredient.write(writer);
            }
            writer.writeItemStack(result);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            group = reader.readSizedString();
            int count = reader.readVarInt();
            ingredients = new Ingredient[count];
            for (int i = 0; i < count; i++) {
                ingredients[i] = new Ingredient();
                ingredients[i].read(reader);
            }
            result = reader.readItemStack();
        }
    }

    public static class DeclaredShapedCraftingRecipe extends DeclaredRecipe {
        public int width;
        public int height;
        private String group;
        private Ingredient[] ingredients;
        private ItemStack result;

        public DeclaredShapedCraftingRecipe(
                @NotNull String recipeId,
                int width,
                int height,
                @NotNull String group,
                @NotNull Ingredient[] ingredients,
                @NotNull ItemStack result
        ) {
            super(recipeId, "crafting_shaped");
            this.group = group;
            this.ingredients = ingredients;
            this.result = result;
            this.width = width;
            this.height = height;
        }

        private DeclaredShapedCraftingRecipe(@NotNull String recipeId, @NotNull BinaryReader reader) {
            super(recipeId, "crafting_shaped");
            read(reader);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            // Write type & id
            super.write(writer);
            // Write recipe specific stuff.
            writer.writeVarInt(width);
            writer.writeVarInt(height);
            writer.writeSizedString(group);
            for (Ingredient ingredient : ingredients) {
                ingredient.write(writer);
            }
            writer.writeItemStack(result);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            width = reader.readVarInt();
            height = reader.readVarInt();
            group = reader.readSizedString();
            ingredients = new Ingredient[width * height];
            for (int i = 0; i < width * height; i++) {
                ingredients[i] = new Ingredient();
                ingredients[i].read(reader);
            }
            result = reader.readItemStack();
        }
    }

    public static class DeclaredSmeltingRecipe extends DeclaredRecipe {
        private String group;
        private Ingredient ingredient;
        private ItemStack result;
        private float experience;
        private int cookingTime;

        public DeclaredSmeltingRecipe(
                @NotNull String recipeId,
                @NotNull String group,
                @NotNull Ingredient ingredient,
                @NotNull ItemStack result,
                float experience,
                int cookingTime
        ) {
            super(recipeId, "smelting");
            this.group = group;
            this.ingredient = ingredient;
            this.result = result;
            this.experience = experience;
            this.cookingTime = cookingTime;
        }

        private DeclaredSmeltingRecipe(@NotNull String recipeId, @NotNull BinaryReader reader) {
            super(recipeId, "smelting");
            read(reader);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            // Write type & id
            super.write(writer);
            // Write recipe specific stuff.
            writer.writeSizedString(group);
            ingredient.write(writer);
            writer.writeItemStack(result);
            writer.writeFloat(experience);
            writer.writeVarInt(cookingTime);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            group = reader.readSizedString();
            ingredient = new Ingredient();
            ingredient.read(reader);
            result = reader.readItemStack();
            experience = reader.readFloat();
            cookingTime = reader.readVarInt();
        }
    }

    public static class DeclaredBlastingRecipe extends DeclaredRecipe {
        private String group;
        private Ingredient ingredient;
        private ItemStack result;
        private float experience;
        private int cookingTime;

        public DeclaredBlastingRecipe(
                @NotNull String recipeId,
                @NotNull String group,
                @NotNull Ingredient ingredient,
                @NotNull ItemStack result,
                float experience,
                int cookingTime
        ) {
            super(recipeId, "blasting");
            this.group = group;
            this.ingredient = ingredient;
            this.result = result;
            this.experience = experience;
            this.cookingTime = cookingTime;
        }

        private DeclaredBlastingRecipe(@NotNull String recipeId, @NotNull BinaryReader reader) {
            super(recipeId, "blasting");
            read(reader);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            // Write type & id
            super.write(writer);
            // Write recipe specific stuff.
            writer.writeSizedString(group);
            ingredient.write(writer);
            writer.writeItemStack(result);
            writer.writeFloat(experience);
            writer.writeVarInt(cookingTime);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            group = reader.readSizedString();
            ingredient = new Ingredient();
            ingredient.read(reader);
            result = reader.readItemStack();
            experience = reader.readFloat();
            cookingTime = reader.readVarInt();
        }
    }

    public static class DeclaredSmokingRecipe extends DeclaredRecipe {
        private String group;
        private Ingredient ingredient;
        private ItemStack result;
        private float experience;
        private int cookingTime;

        public DeclaredSmokingRecipe(
                @NotNull String recipeId,
                @NotNull String group,
                @NotNull Ingredient ingredient,
                @NotNull ItemStack result,
                float experience,
                int cookingTime
        ) {
            super(recipeId, "smoking");
            this.group = group;
            this.ingredient = ingredient;
            this.result = result;
            this.experience = experience;
            this.cookingTime = cookingTime;
        }

        private DeclaredSmokingRecipe(@NotNull String recipeId, @NotNull BinaryReader reader) {
            super(recipeId, "smoking");
            read(reader);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            // Write type & id
            super.write(writer);
            // Write recipe specific stuff.
            writer.writeSizedString(group);
            ingredient.write(writer);
            writer.writeItemStack(result);
            writer.writeFloat(experience);
            writer.writeVarInt(cookingTime);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            group = reader.readSizedString();
            ingredient = new Ingredient();
            ingredient.read(reader);
            result = reader.readItemStack();
            experience = reader.readFloat();
            cookingTime = reader.readVarInt();
        }
    }

    public static class DeclaredCampfireCookingRecipe extends DeclaredRecipe {
        private String group;
        private Ingredient ingredient;
        private ItemStack result;
        private float experience;
        private int cookingTime;

        public DeclaredCampfireCookingRecipe(
                @NotNull String recipeId,
                @NotNull String group,
                @NotNull Ingredient ingredient,
                @NotNull ItemStack result,
                float experience,
                int cookingTime
        ) {
            super(recipeId, "campfire_cooking");
            this.group = group;
            this.ingredient = ingredient;
            this.result = result;
            this.experience = experience;
            this.cookingTime = cookingTime;
        }

        private DeclaredCampfireCookingRecipe(@NotNull String recipeId, @NotNull BinaryReader reader) {
            super(recipeId, "campfire_cooking");
            read(reader);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            // Write type & id
            super.write(writer);
            // Write recipe specific stuff.
            writer.writeSizedString(group);
            ingredient.write(writer);
            writer.writeItemStack(result);
            writer.writeFloat(experience);
            writer.writeVarInt(cookingTime);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            group = reader.readSizedString();
            ingredient = new Ingredient();
            ingredient.read(reader);
            result = reader.readItemStack();
            experience = reader.readFloat();
            cookingTime = reader.readVarInt();
        }
    }

    public static class DeclaredStonecutterRecipe extends DeclaredRecipe {
        private String group;
        private Ingredient ingredient;
        private ItemStack result;

        public DeclaredStonecutterRecipe(
                @NotNull String recipeId,
                @NotNull String group,
                @NotNull Ingredient ingredient,
                @NotNull ItemStack result
        ) {
            super(recipeId, "stonecutter");
            this.group = group;
            this.ingredient = ingredient;
            this.result = result;
        }

        private DeclaredStonecutterRecipe(@NotNull String recipeId, @NotNull BinaryReader reader) {
            super(recipeId, "stonecutter");
            read(reader);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            // Write type & id
            super.write(writer);
            // Write recipe specific stuff.
            writer.writeSizedString(group);
            ingredient.write(writer);
            writer.writeItemStack(result);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            group = reader.readSizedString();
            ingredient = new Ingredient();
            ingredient.read(reader);
            result = reader.readItemStack();
        }
    }

    public final static class DeclaredSmithingRecipe extends DeclaredRecipe {
        private Ingredient base;
        private Ingredient addition;
        private ItemStack result;

        public DeclaredSmithingRecipe(
                @NotNull String recipeId,
                @NotNull Ingredient base,
                @NotNull Ingredient addition,
                @NotNull ItemStack result
        ) {
            super(recipeId, "smithing");
            this.base = base;
            this.addition = addition;
            this.result = result;
        }

        private DeclaredSmithingRecipe(@NotNull String recipeId, @NotNull BinaryReader reader) {
            super(recipeId, "smithing");
            read(reader);
        }

        @Override
        public void write(@NotNull BinaryWriter writer) {
            // Write type & id
            super.write(writer);
            // Write recipe specific stuff.
            base.write(writer);
            addition.write(writer);
            writer.writeItemStack(result);
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            base = new Ingredient();
            addition = new Ingredient();
            base.read(reader);
            addition.read(reader);
            result = reader.readItemStack();
        }
    }

    public static class Ingredient implements Writeable, Readable {

        // The count of each item should be 1
        public ItemStack[] items = new ItemStack[0];

        public void write(BinaryWriter writer) {
            writer.writeVarInt(items.length);
            for (ItemStack itemStack : items) {
                writer.writeItemStack(itemStack);
            }
        }

        @Override
        public void read(@NotNull BinaryReader reader) {
            items = new ItemStack[reader.readVarInt()];
            for (int i = 0; i < items.length; i++) {
                items[i] = reader.readItemStack();
            }
        }
    }
}
