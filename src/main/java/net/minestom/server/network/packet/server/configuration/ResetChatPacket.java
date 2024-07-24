package net.minestom.server.network.packet.server.configuration;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

public record ResetChatPacket() implements ServerPacket.Configuration {
    public static final NetworkBuffer.Type<ResetChatPacket> SERIALIZER = NetworkBufferTemplate.template(ResetChatPacket::new);
}
