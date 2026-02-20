package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.recipe.display.RecipeDisplay;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.function.UnaryOperator;

public record PlaceGhostRecipePacket(int windowId, RecipeDisplay recipe) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final NetworkBuffer.Type<PlaceGhostRecipePacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, PlaceGhostRecipePacket::windowId,
            RecipeDisplay.NETWORK_TYPE, PlaceGhostRecipePacket::recipe,
            PlaceGhostRecipePacket::new);

    @Override
    public @Unmodifiable Collection<Component> components() {
        return recipe.components();
    }

    @Override
    public ServerPacket copyWithOperator(UnaryOperator<Component> operator) {
        return new PlaceGhostRecipePacket(windowId, recipe.copyWithOperator(operator));
    }
}
