package net.minestom.server.network.packet.client.configuration;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

public record ClientFinishConfigurationPacket() implements ClientPacket {
    public static final NetworkBuffer.Type<ClientFinishConfigurationPacket> SERIALIZER = NetworkBufferTemplate.template(ClientFinishConfigurationPacket::new);
}
