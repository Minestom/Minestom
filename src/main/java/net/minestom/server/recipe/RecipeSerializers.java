package net.minestom.server.recipe;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;
import static net.minestom.server.recipe.Recipe.*;
import static net.minestom.server.recipe.RecipeCategory.Cooking;
import static net.minestom.server.recipe.RecipeCategory.Crafting;

public final class RecipeSerializers {
    public static final NetworkBuffer.Type<Recipe> RECIPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, Recipe shaped) {
            writer.write(STRING, shaped.id());
            writer.write(RecipeType.NETWORK_TYPE, shaped.data().type());
            var serializer = RecipeSerializers.dataSerializer(shaped.data().type());
            if (serializer == null)
                throw new UnsupportedOperationException("Unrecognized type: " + shaped.data().type());
            serializer.write(writer, shaped.data());
        }

        @Override
        public Recipe read(@NotNull NetworkBuffer reader) {
            final String identifier = reader.read(STRING);
            final RecipeType type = reader.read(RecipeType.NETWORK_TYPE);
            var serializer = RecipeSerializers.dataSerializer(type);
            if (serializer == null) throw new UnsupportedOperationException("Unrecognized type: " + type);
            final Data data = serializer.read(reader);
            return new Recipe(identifier, data);
        }
    };

    public static final NetworkBuffer.Type<Ingredient> INGREDIENT = NetworkBufferTemplate.template(
            ItemStack.STRICT_NETWORK_TYPE.list(MAX_INGREDIENTS), Ingredient::items,
            Ingredient::new
    );

    public static final NetworkBuffer.Type<Shaped> SHAPED = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, Shaped shaped) {
            writer.write(STRING, shaped.group());
            writer.writeEnum(Crafting.class, shaped.category());
            writer.write(VAR_INT, shaped.width());
            writer.write(VAR_INT, shaped.height());
            for (Ingredient ingredient : shaped.ingredients()) {
                writer.write(INGREDIENT, ingredient);
            }
            writer.write(ItemStack.STRICT_NETWORK_TYPE, shaped.result());
            writer.write(BOOLEAN, shaped.showNotification());
        }

        @Override
        public Shaped read(@NotNull NetworkBuffer reader) {
            String group = reader.read(STRING);
            Crafting category = reader.readEnum(Crafting.class);
            int width = reader.read(VAR_INT);
            int height = reader.read(VAR_INT);
            List<Ingredient> ingredients = new ArrayList<>();
            for (int slot = 0; slot < width * height; slot++) {
                ingredients.add(reader.read(INGREDIENT));
            }
            ItemStack result = reader.read(ItemStack.STRICT_NETWORK_TYPE);
            boolean showNotification = reader.read(BOOLEAN);
            return new Shaped(group, category, width, height, ingredients, result, showNotification);
        }
    };

    public static final NetworkBuffer.Type<Shapeless> SHAPELESS = NetworkBufferTemplate.template(
            STRING, Shapeless::group,
            Enum(Crafting.class), Shapeless::category,
            INGREDIENT.list(MAX_INGREDIENTS), Shapeless::ingredients,
            ItemStack.STRICT_NETWORK_TYPE, Shapeless::result,
            Shapeless::new
    );

    public static final NetworkBuffer.Type<Smelting> SMELTING = NetworkBufferTemplate.template(
            STRING, Smelting::group,
            Enum(Cooking.class), Smelting::category,
            INGREDIENT, Smelting::ingredient,
            ItemStack.STRICT_NETWORK_TYPE, Smelting::result,
            FLOAT, Smelting::experience,
            VAR_INT, Smelting::cookingTime,
            Smelting::new
    );

    public static final NetworkBuffer.Type<Blasting> BLASTING = NetworkBufferTemplate.template(
            STRING, Blasting::group,
            Enum(Cooking.class), Blasting::category,
            INGREDIENT, Blasting::ingredient,
            ItemStack.STRICT_NETWORK_TYPE, Blasting::result,
            FLOAT, Blasting::experience,
            VAR_INT, Blasting::cookingTime,
            Blasting::new
    );

    public static final NetworkBuffer.Type<Smoking> SMOKING = NetworkBufferTemplate.template(
            STRING, Smoking::group,
            Enum(Cooking.class), Smoking::category,
            INGREDIENT, Smoking::ingredient,
            ItemStack.STRICT_NETWORK_TYPE, Smoking::result,
            FLOAT, Smoking::experience,
            VAR_INT, Smoking::cookingTime,
            Smoking::new
    );

    public static final NetworkBuffer.Type<CampfireCooking> CAMPFIRE_COOKING = NetworkBufferTemplate.template(
            STRING, CampfireCooking::group,
            Enum(Cooking.class), CampfireCooking::category,
            INGREDIENT, CampfireCooking::ingredient,
            ItemStack.STRICT_NETWORK_TYPE, CampfireCooking::result,
            FLOAT, CampfireCooking::experience,
            VAR_INT, CampfireCooking::cookingTime,
            CampfireCooking::new
    );

    public static final NetworkBuffer.Type<Stonecutting> STONECUTTING = NetworkBufferTemplate.template(
            STRING, Stonecutting::group,
            INGREDIENT, Stonecutting::ingredient,
            ItemStack.STRICT_NETWORK_TYPE, Stonecutting::result,
            Stonecutting::new
    );

    public static final NetworkBuffer.Type<SmithingTransform> SMITHING_TRANSFORM = NetworkBufferTemplate.template(
            INGREDIENT, SmithingTransform::template,
            INGREDIENT, SmithingTransform::base,
            INGREDIENT, SmithingTransform::addition,
            ItemStack.STRICT_NETWORK_TYPE, SmithingTransform::result,
            SmithingTransform::new
    );

    public static final NetworkBuffer.Type<SmithingTrim> SMITHING_TRIM = NetworkBufferTemplate.template(
            INGREDIENT, SmithingTrim::template,
            INGREDIENT, SmithingTrim::base,
            INGREDIENT, SmithingTrim::addition,
            SmithingTrim::new
    );

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static NetworkBuffer.Type<Data> dataSerializer(RecipeType type) {
        return (Type) switch (type) {
            case SHAPED -> SHAPED;
            case SHAPELESS -> SHAPELESS;
            case SMELTING -> SMELTING;
            case BLASTING -> BLASTING;
            case SMOKING -> SMOKING;
            case CAMPFIRE_COOKING -> CAMPFIRE_COOKING;
            case STONECUTTING -> STONECUTTING;
            case SMITHING_TRANSFORM -> SMITHING_TRANSFORM;
            case SMITHING_TRIM -> SMITHING_TRIM;
            default -> throw new UnsupportedOperationException("Unrecognized type: " + type);
        };
    }
}
