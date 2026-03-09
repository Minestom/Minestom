package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

public record ClientAttackPacket(int targetId) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientAttackPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, ClientAttackPacket::targetId,
            ClientAttackPacket::new
    );
}
