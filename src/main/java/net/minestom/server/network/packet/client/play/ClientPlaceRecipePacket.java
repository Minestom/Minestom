package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientPlaceRecipePacket(byte windowId, int recipeDisplayId, boolean makeAll) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientPlaceRecipePacket> SERIALIZER = NetworkBufferTemplate.template(
            BYTE, ClientPlaceRecipePacket::windowId,
            VAR_INT, ClientPlaceRecipePacket::recipeDisplayId,
            BOOLEAN, ClientPlaceRecipePacket::makeAll,
            ClientPlaceRecipePacket::new);
}
