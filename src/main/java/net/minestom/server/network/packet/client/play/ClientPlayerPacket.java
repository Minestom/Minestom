package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record ClientPlayerPacket(boolean onGround) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientPlayerPacket> SERIALIZER = NetworkBufferTemplate.template(
            BOOLEAN, ClientPlayerPacket::onGround,
            ClientPlayerPacket::new);
}
