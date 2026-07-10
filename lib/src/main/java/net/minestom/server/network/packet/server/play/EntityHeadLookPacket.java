package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityHeadLookPacket(int entityId, float yaw) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityHeadLookPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, EntityHeadLookPacket::entityId,
            BYTE, value -> (byte) (value.yaw * 256f / 360f),
            (entityId, yaw) -> new EntityHeadLookPacket(entityId, yaw * 360f / 256f)
    );
}
