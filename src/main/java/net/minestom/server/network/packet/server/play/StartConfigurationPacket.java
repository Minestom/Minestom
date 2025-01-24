package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

public record StartConfigurationPacket() implements ServerPacket.Play {
    public static final NetworkBuffer.Type<StartConfigurationPacket> SERIALIZER = NetworkBufferTemplate.template(StartConfigurationPacket::new);
}
