package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

/**
 * The ClientSpectateEntityPacket is sent when the client clicks on an entity to spectate it.
 */
public record ClientSpectateEntityPacket(int targetId) implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientSpectateEntityPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, ClientSpectateEntityPacket::targetId,
            ClientSpectateEntityPacket::new);
}
