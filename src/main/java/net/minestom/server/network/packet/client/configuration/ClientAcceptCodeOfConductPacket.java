package net.minestom.server.network.packet.client.configuration;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

public record ClientAcceptCodeOfConductPacket() implements ClientPacket.Configuration {
    public static final ClientAcceptCodeOfConductPacket INSTANCE = new ClientAcceptCodeOfConductPacket();
    public static final NetworkBuffer.Type<ClientAcceptCodeOfConductPacket> SERIALIZER = NetworkBufferTemplate.template(INSTANCE);
}
