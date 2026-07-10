package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.BYTE;

public record HeldItemChangePacket(byte slot) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<HeldItemChangePacket> SERIALIZER = NetworkBufferTemplate.template(
            BYTE, HeldItemChangePacket::slot,
            HeldItemChangePacket::new);
}
