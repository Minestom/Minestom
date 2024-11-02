package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.STRING;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

// TODO(1.21.2): Recipe is now a RecipeDisplay object need to look further into it.
public record PlaceGhostRecipePacket(int windowId, @NotNull String recipe) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<PlaceGhostRecipePacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, PlaceGhostRecipePacket::windowId,
            STRING, PlaceGhostRecipePacket::recipe,
            PlaceGhostRecipePacket::new);
}
