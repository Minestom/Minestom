package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientCraftRecipeRequest(byte windowId, String recipe, boolean makeAll) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientCraftRecipeRequest> SERIALIZER = NetworkBufferTemplate.template(
            BYTE, ClientCraftRecipeRequest::windowId,
            STRING, ClientCraftRecipeRequest::recipe,
            BOOLEAN, ClientCraftRecipeRequest::makeAll,
            ClientCraftRecipeRequest::new);
    public ClientCraftRecipeRequest {
        if (recipe.length() > 256) {
            throw new IllegalArgumentException("'recipe' cannot be longer than 256 characters.");
        }
    }
}
