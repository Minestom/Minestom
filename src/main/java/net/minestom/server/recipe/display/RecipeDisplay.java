package net.minestom.server.recipe.display;

import net.kyori.adventure.text.Component;
import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public sealed interface RecipeDisplay extends ComponentHolder<RecipeDisplay> {
    NetworkBuffer.Type<RecipeDisplay> NETWORK_TYPE = RecipeDisplayType.NETWORK_TYPE
            .unionType(RecipeDisplay::dataSerializer, RecipeDisplay::recipeDisplayToType);

    record CraftingShapeless(
            List<SlotDisplay> ingredients,
            SlotDisplay result,
            SlotDisplay craftingStation
    ) implements RecipeDisplay {
        private static final int MAX_INGREDIENTS = Short.MAX_VALUE;

        public static final NetworkBuffer.Type<CraftingShapeless> NETWORK_TYPE = NetworkBufferTemplate.template(
                SlotDisplay.NETWORK_TYPE.list(MAX_INGREDIENTS), CraftingShapeless::ingredients,
                SlotDisplay.NETWORK_TYPE, CraftingShapeless::result,
                SlotDisplay.NETWORK_TYPE, CraftingShapeless::craftingStation,
                CraftingShapeless::new);

        @Override
        public Collection<Component> components() {
            final var components = new ArrayList<Component>();
            for (SlotDisplay ingredient : ingredients)
                components.addAll(ingredient.components());
            components.addAll(result.components());
            components.addAll(craftingStation.components());
            return List.copyOf(components);
        }

        @Override
        public RecipeDisplay copyWithOperator(UnaryOperator<Component> operator) {
            final var newIngredients = new ArrayList<SlotDisplay>();
            for (SlotDisplay ingredient : ingredients)
                newIngredients.add(ingredient.copyWithOperator(operator));
            return new CraftingShapeless(newIngredients, result.copyWithOperator(operator), craftingStation.copyWithOperator(operator));
        }
    }

    record CraftingShaped(
            int width, int height,
            List<SlotDisplay> ingredients,
            SlotDisplay result,
            SlotDisplay craftingStation
    ) implements RecipeDisplay {
        private static final int MAX_INGREDIENTS = Short.MAX_VALUE;

        public static final NetworkBuffer.Type<CraftingShaped> NETWORK_TYPE = NetworkBufferTemplate.template(
                NetworkBuffer.VAR_INT, CraftingShaped::width,
                NetworkBuffer.VAR_INT, CraftingShaped::height,
                SlotDisplay.NETWORK_TYPE.list(MAX_INGREDIENTS), CraftingShaped::ingredients,
                SlotDisplay.NETWORK_TYPE, CraftingShaped::result,
                SlotDisplay.NETWORK_TYPE, CraftingShaped::craftingStation,
                CraftingShaped::new);

        public CraftingShaped {
            if (ingredients.size() != width * height)
                throw new IllegalArgumentException("Invalid shaped recipe, ingredients size must be equal to width * height");
            ingredients = List.copyOf(ingredients);
        }

        @Override
        public Collection<Component> components() {
            final var components = new ArrayList<Component>();
            for (SlotDisplay ingredient : ingredients)
                components.addAll(ingredient.components());
            components.addAll(result.components());
            components.addAll(craftingStation.components());
            return List.copyOf(components);
        }

        @Override
        public RecipeDisplay copyWithOperator(UnaryOperator<Component> operator) {
            final var newIngredients = new ArrayList<SlotDisplay>();
            for (SlotDisplay ingredient : ingredients)
                newIngredients.add(ingredient.copyWithOperator(operator));
            return new CraftingShaped(width, height, newIngredients, result.copyWithOperator(operator), craftingStation.copyWithOperator(operator));
        }
    }

    record Furnace(
            SlotDisplay ingredient,
            SlotDisplay fuel,
            SlotDisplay result,
            SlotDisplay craftingStation,
            int duration, float experience
    ) implements RecipeDisplay {
        public static final NetworkBuffer.Type<Furnace> NETWORK_TYPE = NetworkBufferTemplate.template(
                SlotDisplay.NETWORK_TYPE, Furnace::ingredient,
                SlotDisplay.NETWORK_TYPE, Furnace::fuel,
                SlotDisplay.NETWORK_TYPE, Furnace::result,
                SlotDisplay.NETWORK_TYPE, Furnace::craftingStation,
                NetworkBuffer.VAR_INT, Furnace::duration,
                NetworkBuffer.FLOAT, Furnace::experience,
                Furnace::new);

        @Override
        public Collection<Component> components() {
            final var components = new ArrayList<Component>();
            components.addAll(ingredient.components());
            components.addAll(fuel.components());
            components.addAll(result.components());
            components.addAll(craftingStation.components());
            return List.copyOf(components);
        }

        @Override
        public RecipeDisplay copyWithOperator(UnaryOperator<Component> operator) {
            return new Furnace(ingredient.copyWithOperator(operator), fuel.copyWithOperator(operator),
                    result.copyWithOperator(operator), craftingStation.copyWithOperator(operator),
                    duration, experience);
        }
    }

    record Stonecutter(
            SlotDisplay ingredient,
            SlotDisplay result,
            SlotDisplay craftingStation
    ) implements RecipeDisplay {
        public static final NetworkBuffer.Type<Stonecutter> NETWORK_TYPE = NetworkBufferTemplate.template(
                SlotDisplay.NETWORK_TYPE, Stonecutter::ingredient,
                SlotDisplay.NETWORK_TYPE, Stonecutter::result,
                SlotDisplay.NETWORK_TYPE, Stonecutter::craftingStation,
                Stonecutter::new);

        @Override
        public Collection<Component> components() {
            final var components = new ArrayList<Component>();
            components.addAll(ingredient.components());
            components.addAll(result.components());
            components.addAll(craftingStation.components());
            return List.copyOf(components);
        }

        @Override
        public RecipeDisplay copyWithOperator(UnaryOperator<Component> operator) {
            return new Stonecutter(ingredient.copyWithOperator(operator), result.copyWithOperator(operator),
                    craftingStation.copyWithOperator(operator));
        }
    }

    record Smithing(
            SlotDisplay template,
            SlotDisplay base,
            SlotDisplay addition,
            SlotDisplay result,
            SlotDisplay craftingStation
    ) implements RecipeDisplay {
        public static final NetworkBuffer.Type<Smithing> NETWORK_TYPE = NetworkBufferTemplate.template(
                SlotDisplay.NETWORK_TYPE, Smithing::template,
                SlotDisplay.NETWORK_TYPE, Smithing::base,
                SlotDisplay.NETWORK_TYPE, Smithing::addition,
                SlotDisplay.NETWORK_TYPE, Smithing::result,
                SlotDisplay.NETWORK_TYPE, Smithing::craftingStation,
                Smithing::new);

        @Override
        public Collection<Component> components() {
            final var components = new ArrayList<Component>();
            components.addAll(template.components());
            components.addAll(base.components());
            components.addAll(addition.components());
            components.addAll(result.components());
            components.addAll(craftingStation.components());
            return List.copyOf(components);
        }

        @Override
        public RecipeDisplay copyWithOperator(UnaryOperator<Component> operator) {
            return new Smithing(template.copyWithOperator(operator), base.copyWithOperator(operator),
                    addition.copyWithOperator(operator), result.copyWithOperator(operator), craftingStation.copyWithOperator(operator));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static NetworkBuffer.Type<RecipeDisplay> dataSerializer(RecipeDisplayType type) {
        return (NetworkBuffer.Type) switch (type) {
            case CRAFTING_SHAPELESS -> CraftingShapeless.NETWORK_TYPE;
            case CRAFTING_SHAPED -> CraftingShaped.NETWORK_TYPE;
            case FURNACE -> Furnace.NETWORK_TYPE;
            case STONECUTTER -> Stonecutter.NETWORK_TYPE;
            case SMITHING -> Smithing.NETWORK_TYPE;
        };
    }

    private static RecipeDisplayType recipeDisplayToType(RecipeDisplay recipeDisplay) {
        return switch (recipeDisplay) {
            case CraftingShapeless ignored -> RecipeDisplayType.CRAFTING_SHAPELESS;
            case CraftingShaped ignored -> RecipeDisplayType.CRAFTING_SHAPED;
            case Furnace ignored -> RecipeDisplayType.FURNACE;
            case Stonecutter ignored -> RecipeDisplayType.STONECUTTER;
            case Smithing ignored -> RecipeDisplayType.SMITHING;
        };
    }
}
