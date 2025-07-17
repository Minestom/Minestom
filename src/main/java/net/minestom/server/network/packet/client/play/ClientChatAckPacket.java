package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientChatAckPacket(int offset) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientChatAckPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientChatAckPacket::offset,
            ClientChatAckPacket::new);
}
