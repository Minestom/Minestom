package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.BYTE;

public record CloseWindowPacket(byte windowId) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<CloseWindowPacket> SERIALIZER = NetworkBufferTemplate.template(
            BYTE, CloseWindowPacket::windowId,
            CloseWindowPacket::new);
}
