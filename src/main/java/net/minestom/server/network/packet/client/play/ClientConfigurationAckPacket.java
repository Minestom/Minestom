package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

public record ClientConfigurationAckPacket() implements ClientPacket.Play {
    public static final NetworkBuffer.Type<ClientConfigurationAckPacket> SERIALIZER = NetworkBufferTemplate.template(new ClientConfigurationAckPacket());
}
