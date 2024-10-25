package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientPlaceRecipePacket(byte windowId, String recipe, boolean makeAll) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientPlaceRecipePacket> SERIALIZER = NetworkBufferTemplate.template(
            BYTE, ClientPlaceRecipePacket::windowId,
            STRING, ClientPlaceRecipePacket::recipe,
            BOOLEAN, ClientPlaceRecipePacket::makeAll,
            ClientPlaceRecipePacket::new);
    public ClientPlaceRecipePacket {
        if (recipe.length() > 256) {
            throw new IllegalArgumentException("'recipe' cannot be longer than 256 characters.");
        }
    }
}
