package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.INT;

public record EntityStatusPacket(int entityId, byte status) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityStatusPacket> SERIALIZER = NetworkBufferTemplate.template(
            INT, EntityStatusPacket::entityId,
            BYTE, EntityStatusPacket::status,
            EntityStatusPacket::new);
}
