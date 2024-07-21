package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.recipe.Recipe;
import net.minestom.server.recipe.RecipeSerializers;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record DeclareRecipesPacket(@NotNull List<Recipe> recipes) implements ServerPacket.Play {
    public static final int MAX_RECIPES = Short.MAX_VALUE;

    public DeclareRecipesPacket {
        recipes = List.copyOf(recipes);
    }

    public DeclareRecipesPacket(@NotNull NetworkBuffer reader) {
        this(reader.readCollection(RecipeSerializers.RECIPE, MAX_RECIPES));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeCollection(RecipeSerializers.RECIPE, recipes);
    }

}
