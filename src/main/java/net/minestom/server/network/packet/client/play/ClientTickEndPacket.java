package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

public record ClientTickEndPacket() implements ClientPacket {
    public static final NetworkBuffer.Type<ClientTickEndPacket> SERIALIZER =
            NetworkBufferTemplate.template(ClientTickEndPacket::new);

}
