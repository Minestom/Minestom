package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientSetDisplayedRecipePacket(@NotNull String recipeId) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientSetDisplayedRecipePacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, ClientSetDisplayedRecipePacket::recipeId,
            ClientSetDisplayedRecipePacket::new);

    public ClientSetDisplayedRecipePacket {
        if (recipeId.length() > 256) {
            throw new IllegalArgumentException("'recipeId' cannot be longer than 256 characters.");
        }
    }
}
