package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.SHORT;

public record ClientHeldItemChangePacket(short slot) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientHeldItemChangePacket> SERIALIZER = NetworkBufferTemplate.template(
            SHORT, ClientHeldItemChangePacket::slot,
            ClientHeldItemChangePacket::new);
}
