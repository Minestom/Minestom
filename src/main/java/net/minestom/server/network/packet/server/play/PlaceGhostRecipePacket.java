package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.recipe.display.RecipeDisplay;
import org.jetbrains.annotations.NotNull;

public record PlaceGhostRecipePacket(int windowId, @NotNull RecipeDisplay recipe) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<PlaceGhostRecipePacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, PlaceGhostRecipePacket::windowId,
            RecipeDisplay.NETWORK_TYPE, PlaceGhostRecipePacket::recipe,
            PlaceGhostRecipePacket::new);
}
