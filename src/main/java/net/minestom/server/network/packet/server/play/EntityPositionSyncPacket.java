package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityPositionSyncPacket(
        int entityId, @NotNull Point position, @NotNull Point delta,
        float yaw, float pitch, boolean onGround
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityPositionSyncPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, EntityPositionSyncPacket::entityId,
            VECTOR3D, EntityPositionSyncPacket::position,
            VECTOR3D, EntityPositionSyncPacket::delta,
            FLOAT, EntityPositionSyncPacket::yaw,
            FLOAT, EntityPositionSyncPacket::pitch,
            BOOLEAN, EntityPositionSyncPacket::onGround,
            EntityPositionSyncPacket::new);
}
