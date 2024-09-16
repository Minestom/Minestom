package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.recipe.Recipe;
import net.minestom.server.recipe.RecipeSerializers;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record DeclareRecipesPacket(@NotNull List<Recipe> recipes) implements ServerPacket.Play {
    public static final int MAX_RECIPES = Short.MAX_VALUE;

    public static final NetworkBuffer.Type<DeclareRecipesPacket> SERIALIZER = NetworkBufferTemplate.template(
            RecipeSerializers.RECIPE.list(MAX_RECIPES), DeclareRecipesPacket::recipes,
            DeclareRecipesPacket::new);

    public DeclareRecipesPacket {
        recipes = List.copyOf(recipes);
    }
}
