package net.minestom.server.network.packet.client.login;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

public record ClientLoginAcknowledgedPacket() implements ClientPacket.Login {
    public static final ClientLoginAcknowledgedPacket INSTANCE = new ClientLoginAcknowledgedPacket();
    public static final NetworkBuffer.Type<ClientLoginAcknowledgedPacket> SERIALIZER = NetworkBufferTemplate.template(INSTANCE);
}
