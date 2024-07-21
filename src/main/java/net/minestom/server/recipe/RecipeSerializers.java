package net.minestom.server.recipe;

import net.minestom.server.item.ItemStack;
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
    public static final Type<Recipe> RECIPE = new Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, Recipe shaped) {
            writer.write(STRING, shaped.id());
            final RecipeType recipeType = recipeToType(shaped.data());
            writer.write(RecipeType.NETWORK_TYPE, recipeType);
            var serializer = RecipeSerializers.dataSerializer(recipeType);
            if (serializer == null)
                throw new UnsupportedOperationException("Unrecognized type: " + recipeType);
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

    public static final Type<Ingredient> INGREDIENT = NetworkBufferTemplate.template(
            ItemStack.STRICT_NETWORK_TYPE.list(MAX_INGREDIENTS), Ingredient::items,
            Ingredient::new
    );

    public static final Type<Shaped> SHAPED = new Type<>() {
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

    public static final Type<SpecialShulkerBoxColoring> SPECIAL_SHULKER_BOX_COLORING = NetworkBufferTemplate.template(
            Enum(Crafting.class), SpecialShulkerBoxColoring::category, SpecialShulkerBoxColoring::new
    );

    public static final Type<SpecialSuspiciousStew> SUSPICIOUS_STEW = NetworkBufferTemplate.template(
            Enum(Crafting.class), SpecialSuspiciousStew::category, SpecialSuspiciousStew::new
    );

    public static final Type<SpecialRepairItem> REPAIR_ITEM = NetworkBufferTemplate.template(
            Enum(Crafting.class), SpecialRepairItem::category, SpecialRepairItem::new
    );

    public static final Type<DecoratedPot> DECORATED_POT = NetworkBufferTemplate.template(
            Enum(Crafting.class), DecoratedPot::category, DecoratedPot::new
    );


    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Type<Data> dataSerializer(RecipeType type) {
        return (Type) switch (type) {
            case SHAPED -> SHAPED;
            case SHAPELESS -> SHAPELESS;
            case SPECIAL_ARMORDYE -> ARMOR_DYE;
            case SPECIAL_BOOKCLONING -> BOOK_CLONING;
            case SPECIAL_MAPCLONING -> MAP_CLONING;
            case SPECIAL_MAPEXTENDING -> MAP_EXTENDING;
            case SPECIAL_FIREWORK_ROCKET -> FIREWORK_ROCKET;
            case SPECIAL_FIREWORK_STAR -> FIREWORK_STAR;
            case SPECIAL_FIREWORK_STAR_FADE -> FIREWORK_STAR_FADE;
            case SPECIAL_TIPPEDARROW -> TIPPED_ARROW;
            case SPECIAL_BANNERDUPLICATE -> BANNER_DUPLICATE;
            case SPECIAL_SHIELDDECORATION -> SHIELD_DECORATION;
            case SPECIAL_SHULKERBOXCOLORING -> SPECIAL_SHULKER_BOX_COLORING;
            case SPECIAL_SUSPICIOUSSTEW -> SUSPICIOUS_STEW;
            case SPECIAL_REPAIRITEM -> REPAIR_ITEM;
            case SMELTING -> SMELTING;
            case BLASTING -> BLASTING;
            case SMOKING -> SMOKING;
            case CAMPFIRE_COOKING -> CAMPFIRE_COOKING;
            case STONECUTTING -> STONECUTTING;
            case SMITHING_TRANSFORM -> SMITHING_TRANSFORM;
            case SMITHING_TRIM -> SMITHING_TRIM;
            case DECORATED_POT -> DECORATED_POT;
        };
    }

    public static RecipeType recipeToType(Data data) {
        return switch (data) {
            case Shaped ignored -> RecipeType.SHAPED;
            case Shapeless ignored -> RecipeType.SHAPELESS;
            case Smelting ignored -> RecipeType.SMELTING;
            case Blasting ignored -> RecipeType.BLASTING;
            case Smoking ignored -> RecipeType.SMOKING;
            case CampfireCooking ignored -> RecipeType.CAMPFIRE_COOKING;
            case Stonecutting ignored -> RecipeType.STONECUTTING;
            case SmithingTransform ignored -> RecipeType.SMITHING_TRANSFORM;
            case SmithingTrim ignored -> RecipeType.SMITHING_TRIM;
            case SpecialArmorDye ignored -> RecipeType.SPECIAL_ARMORDYE;
            case SpecialBannerDuplicate ignored -> RecipeType.SPECIAL_BANNERDUPLICATE;
            case SpecialBookCloning ignored -> RecipeType.SPECIAL_BOOKCLONING;
            case DecoratedPot ignored -> RecipeType.DECORATED_POT;
            case SpecialFireworkRocket ignored -> RecipeType.SPECIAL_FIREWORK_ROCKET;
            case SpecialFireworkStar ignored -> RecipeType.SPECIAL_FIREWORK_STAR;
            case SpecialFireworkStarFade ignored -> RecipeType.SPECIAL_FIREWORK_STAR_FADE;
            case SpecialMapCloning ignored -> RecipeType.SPECIAL_MAPCLONING;
            case SpecialMapExtending ignored -> RecipeType.SPECIAL_MAPEXTENDING;
            case SpecialRepairItem ignored -> RecipeType.SPECIAL_REPAIRITEM;
            case SpecialShieldDecoration ignored -> RecipeType.SPECIAL_SHIELDDECORATION;
            case SpecialShulkerBoxColoring ignored -> RecipeType.SPECIAL_SHULKERBOXCOLORING;
            case SpecialSuspiciousStew ignored -> RecipeType.SPECIAL_SUSPICIOUSSTEW;
            case SpecialTippedArrow ignored -> RecipeType.SPECIAL_TIPPEDARROW;
        };
    }
}
