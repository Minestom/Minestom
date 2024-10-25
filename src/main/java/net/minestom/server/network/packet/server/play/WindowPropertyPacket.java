package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.SHORT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record WindowPropertyPacket(int windowId, short property, short value) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<WindowPropertyPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, WindowPropertyPacket::windowId,
            SHORT, WindowPropertyPacket::property,
            SHORT, WindowPropertyPacket::value,
            WindowPropertyPacket::new);
}
