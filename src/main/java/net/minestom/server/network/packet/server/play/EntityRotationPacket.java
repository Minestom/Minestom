package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityRotationPacket(int entityId, float yaw, float pitch,
                                   boolean onGround) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityRotationPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, EntityRotationPacket::entityId,
            BYTE, value -> (byte) (value.yaw * 256f / 360f),
            BYTE, value -> (byte) (value.pitch * 256f / 360f),
            BOOLEAN, EntityRotationPacket::onGround,
            (entityId, yaw, pitch, onGround) -> new EntityRotationPacket(entityId,
                    yaw * 360f / 256f, pitch * 360f / 256f, onGround)
    );
}
