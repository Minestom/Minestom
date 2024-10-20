package net.minestom.server.recipe;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;
import static net.minestom.server.recipe.Recipe.*;
import static net.minestom.server.recipe.RecipeCategory.Cooking;
import static net.minestom.server.recipe.RecipeCategory.Crafting;

@ApiStatus.Internal
public final class RecipeSerializers {
    public static final Type<Data> DATA = RecipeType.NETWORK_TYPE
            .unionType(RecipeSerializers::dataSerializer, RecipeSerializers::recipeToType);

    public static final Type<Recipe> RECIPE = NetworkBufferTemplate.template(
            STRING, Recipe::id,
            DATA, Recipe::data,
            Recipe::new);

    public static final Type<Ingredient> INGREDIENT = NetworkBufferTemplate.template(
            // FIXME(1.21.2): This is really an ObjectSet, but currently ObjectSet does not properly support
            //  non-dynamic registry types. We need to improve how the tag system works generally.
            new Type<>() {
                @Override
                public void write(@NotNull NetworkBuffer buffer, List<ItemStack> value) {
                    // +1 because 0 indicates that an item tag name follows (in this case it does not).
                    buffer.write(VAR_INT, value.size() + 1);
                    for (ItemStack itemStack : value) {
                        buffer.write(ItemStack.STRICT_NETWORK_TYPE, itemStack);
                    }
                }

                @Override
                public List<ItemStack> read(@NotNull NetworkBuffer buffer) {
                    throw new UnsupportedOperationException("cannot read ingredients yet");
                }
            }, Ingredient::items,
            Ingredient::new
    );

    public static final Type<Shaped> SHAPED = new Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, Shaped shaped) {
            buffer.write(STRING, shaped.group());
            buffer.write(NetworkBuffer.Enum(Crafting.class), shaped.category());
            buffer.write(VAR_INT, shaped.width());
            buffer.write(VAR_INT, shaped.height());
            for (Ingredient ingredient : shaped.ingredients()) {
                buffer.write(INGREDIENT, ingredient);
            }
            buffer.write(ItemStack.STRICT_NETWORK_TYPE, shaped.result());
            buffer.write(BOOLEAN, shaped.showNotification());
        }

        @Override
        public Shaped read(@NotNull NetworkBuffer buffer) {
            String group = buffer.read(STRING);
            Crafting category = buffer.read(NetworkBuffer.Enum(Crafting.class));
            int width = buffer.read(VAR_INT);
            int height = buffer.read(VAR_INT);
            List<Ingredient> ingredients = new ArrayList<>();
            for (int slot = 0; slot < width * height; slot++) {
                ingredients.add(buffer.read(INGREDIENT));
            }
            ItemStack result = buffer.read(ItemStack.STRICT_NETWORK_TYPE);
            boolean showNotification = buffer.read(BOOLEAN);
            return new Shaped(group, category, width, height, ingredients, result, showNotification);
        }
    };

    public static final Type<Shapeless> SHAPELESS = NetworkBufferTemplate.template(
            STRING, Shapeless::group,
            Enum(Crafting.class), Shapeless::category,
            INGREDIENT.list(MAX_INGREDIENTS), Shapeless::ingredients,
            ItemStack.STRICT_NETWORK_TYPE, Shapeless::result,
            Shapeless::new
    );

    public static final Type<Smelting> SMELTING = NetworkBufferTemplate.template(
            STRING, Smelting::group,
            Enum(Cooking.class), Smelting::category,
            INGREDIENT, Smelting::ingredient,
            ItemStack.STRICT_NETWORK_TYPE, Smelting::result,
            FLOAT, Smelting::experience,
            VAR_INT, Smelting::cookingTime,
            Smelting::new
    );

    public static final Type<Blasting> BLASTING = NetworkBufferTemplate.template(
            STRING, Blasting::group,
            Enum(Cooking.class), Blasting::category,
            INGREDIENT, Blasting::ingredient,
            ItemStack.STRICT_NETWORK_TYPE, Blasting::result,
            FLOAT, Blasting::experience,
            VAR_INT, Blasting::cookingTime,
            Blasting::new
    );

    public static final Type<Smoking> SMOKING = NetworkBufferTemplate.template(
            STRING, Smoking::group,
            Enum(Cooking.class), Smoking::category,
            INGREDIENT, Smoking::ingredient,
            ItemStack.STRICT_NETWORK_TYPE, Smoking::result,
            FLOAT, Smoking::experience,
            VAR_INT, Smoking::cookingTime,
            Smoking::new
    );

    public static final Type<CampfireCooking> CAMPFIRE_COOKING = NetworkBufferTemplate.template(
            STRING, CampfireCooking::group,
            Enum(Cooking.class), CampfireCooking::category,
            INGREDIENT, CampfireCooking::ingredient,
            ItemStack.STRICT_NETWORK_TYPE, CampfireCooking::result,
            FLOAT, CampfireCooking::experience,
            VAR_INT, CampfireCooking::cookingTime,
            CampfireCooking::new
    );

    public static final Type<Stonecutting> STONECUTTING = NetworkBufferTemplate.template(
            STRING, Stonecutting::group,
            INGREDIENT, Stonecutting::ingredient,
            ItemStack.STRICT_NETWORK_TYPE, Stonecutting::result,
            Stonecutting::new
    );

    public static final Type<SmithingTransform> SMITHING_TRANSFORM = NetworkBufferTemplate.template(
            INGREDIENT, SmithingTransform::template,
            INGREDIENT, SmithingTransform::base,
            INGREDIENT, SmithingTransform::addition,
            ItemStack.STRICT_NETWORK_TYPE, SmithingTransform::result,
            SmithingTransform::new
    );

    public static final Type<SmithingTrim> SMITHING_TRIM = NetworkBufferTemplate.template(
            INGREDIENT, SmithingTrim::template,
            INGREDIENT, SmithingTrim::base,
            INGREDIENT, SmithingTrim::addition,
            SmithingTrim::new
    );

    public static final Type<SpecialArmorDye> ARMOR_DYE = NetworkBufferTemplate.template(
            Enum(Crafting.class), SpecialArmorDye::category, SpecialArmorDye::new
    );

    public static final Type<SpecialBookCloning> BOOK_CLONING = NetworkBufferTemplate.template(
            Enum(Crafting.class), SpecialBookCloning::category, SpecialBookCloning::new
    );

    public static final Type<SpecialMapCloning> MAP_CLONING = NetworkBufferTemplate.template(
            Enum(Crafting.class), SpecialMapCloning::category, SpecialMapCloning::new
    );

    public static final Type<SpecialMapExtending> MAP_EXTENDING = NetworkBufferTemplate.template(
            Enum(Crafting.class), SpecialMapExtending::category, SpecialMapExtending::new
    );

    public static final Type<SpecialFireworkRocket> FIREWORK_ROCKET = NetworkBufferTemplate.template(
            Enum(Crafting.class), SpecialFireworkRocket::category, SpecialFireworkRocket::new
    );

    public static final Type<SpecialFireworkStar> FIREWORK_STAR = NetworkBufferTemplate.template(
            Enum(Crafting.class), SpecialFireworkStar::category, SpecialFireworkStar::new
    );

    public static final Type<SpecialFireworkStarFade> FIREWORK_STAR_FADE = NetworkBufferTemplate.template(
            Enum(Crafting.class), SpecialFireworkStarFade::category, SpecialFireworkStarFade::new
    );

    public static final Type<SpecialTippedArrow> TIPPED_ARROW = NetworkBufferTemplate.template(
            Enum(Crafting.class), SpecialTippedArrow::category, SpecialTippedArrow::new
    );

    public static final Type<SpecialBannerDuplicate> BANNER_DUPLICATE = NetworkBufferTemplate.template(
            Enum(Crafting.class), SpecialBannerDuplicate::category, SpecialBannerDuplicate::new
    );

    public static final Type<SpecialShieldDecoration> SHIELD_DECORATION = NetworkBufferTemplate.template(
            Enum(Crafting.class), SpecialShieldDecoration::category, SpecialShieldDecoration::new
    );

    public static final Type<SpecialRepairItem> REPAIR_ITEM = NetworkBufferTemplate.template(
            Enum(Crafting.class), SpecialRepairItem::category, SpecialRepairItem::new
    );

    public static final Type<DecoratedPot> DECORATED_POT = NetworkBufferTemplate.template(
            Enum(Crafting.class), DecoratedPot::category, DecoratedPot::new
    );

    public static final Type<Transmute> TRANSMUTE = NetworkBufferTemplate.template(
            STRING, Transmute::group,
            Enum(Crafting.class), Transmute::category,
            INGREDIENT, Transmute::input,
            INGREDIENT, Transmute::material,
            Material.NETWORK_TYPE, Transmute::result,
            Transmute::new
    );


    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Type<Data> dataSerializer(RecipeType type) {
        return (Type) switch (type) {
            case CRAFTING -> null;
            case SMELTING -> SMELTING;
            case BLASTING -> BLASTING;
            case SMOKING -> SMOKING;
            case CAMPFIRE_COOKING -> CAMPFIRE_COOKING;
            case STONECUTTING -> STONECUTTING;
            case SMITHING -> null;
        };
    }

    public static RecipeType recipeToType(Data data) {
        return switch (data) {
            case Smelting ignored -> RecipeType.SMELTING;
            case Blasting ignored -> RecipeType.BLASTING;
            case Smoking ignored -> RecipeType.SMOKING;
            case CampfireCooking ignored -> RecipeType.CAMPFIRE_COOKING;
            case Stonecutting ignored -> RecipeType.STONECUTTING;
            default -> throw new IllegalStateException("Unexpected value: " + data);
        };
    }
}
