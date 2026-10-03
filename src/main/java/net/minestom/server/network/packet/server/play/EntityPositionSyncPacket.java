package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.PositionPath;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.FLOAT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityPositionSyncPacket(
        int entityId, PositionPath position,
        float yaw, float pitch, boolean onGround
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityPositionSyncPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, EntityPositionSyncPacket::entityId,
            PositionPath.NETWORK_TYPE, EntityPositionSyncPacket::position,
            FLOAT, EntityPositionSyncPacket::yaw,
            FLOAT, EntityPositionSyncPacket::pitch,
            BOOLEAN, EntityPositionSyncPacket::onGround,
            EntityPositionSyncPacket::new);
}
