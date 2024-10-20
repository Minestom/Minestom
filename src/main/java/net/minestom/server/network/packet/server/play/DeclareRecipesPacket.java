package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.recipe.Recipe;
import net.minestom.server.recipe.RecipeProperty;
import net.minestom.server.recipe.RecipeSerializers;
import net.minestom.server.recipe.display.SlotDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record DeclareRecipesPacket(
        @NotNull Map<RecipeProperty, List<Material>> itemProperties,
        @NotNull List<StonecutterRecipe> stonecutterRecipes
) implements ServerPacket.Play {
    private static final int MAX_ITEMS_PER_PROPERTY = Short.MAX_VALUE;
    private static final int MAX_STONECUTTER_RECIPES = Short.MAX_VALUE;

    public static final NetworkBuffer.Type<DeclareRecipesPacket> SERIALIZER = NetworkBufferTemplate.template(
            RecipeProperty.NETWORK_TYPE.mapValue(Material.NETWORK_TYPE.list(MAX_ITEMS_PER_PROPERTY)), DeclareRecipesPacket::itemProperties,
            StonecutterRecipe.NETWORK_TYPE.list(MAX_STONECUTTER_RECIPES), DeclareRecipesPacket::stonecutterRecipes,
            DeclareRecipesPacket::new);

    public DeclareRecipesPacket {
        itemProperties = Map.copyOf(itemProperties);
        stonecutterRecipes = List.copyOf(stonecutterRecipes);
    }

    public record StonecutterRecipe(
            @NotNull Recipe.Ingredient ingredient,
            @NotNull SlotDisplay optionDisplay
    ) {
        public static final NetworkBuffer.Type<StonecutterRecipe> NETWORK_TYPE = NetworkBufferTemplate.template(
                RecipeSerializers.INGREDIENT, StonecutterRecipe::ingredient,
                SlotDisplay.NETWORK_TYPE, StonecutterRecipe::optionDisplay,
                StonecutterRecipe::new);
    }

}
