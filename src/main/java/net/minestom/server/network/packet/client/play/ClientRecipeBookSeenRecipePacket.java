package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientRecipeBookSeenRecipePacket(int index) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientRecipeBookSeenRecipePacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientRecipeBookSeenRecipePacket::index,
            ClientRecipeBookSeenRecipePacket::new);
}
