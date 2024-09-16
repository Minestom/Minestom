package net.minestom.server.network.packet.client.status;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.BYTE;

public record LegacyServerListPingPacket(byte payload) implements ClientPacket {
    public static final NetworkBuffer.Type<LegacyServerListPingPacket> SERIALIZER = NetworkBufferTemplate.template(
            BYTE, LegacyServerListPingPacket::payload,
            LegacyServerListPingPacket::new);
}
