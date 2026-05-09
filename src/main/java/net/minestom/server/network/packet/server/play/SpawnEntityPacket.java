package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.*;

public record SpawnEntityPacket(
        int entityId, UUID uuid, EntityType type,
        Pos position, float headRot, int data,
        Vec velocity
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SpawnEntityPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, SpawnEntityPacket::entityId,
            UUID, SpawnEntityPacket::uuid,
            EntityType.NETWORK_TYPE, SpawnEntityPacket::type,
            DOUBLE, value -> value.position.x(),
            DOUBLE, value -> value.position.y(),
            DOUBLE, value -> value.position.z(),
            LP_VECTOR3, SpawnEntityPacket::velocity,
            BYTE, value -> (byte) (value.position.pitch() * 256 / 360),
            BYTE, value -> (byte) (value.position.yaw() * 256 / 360),
            BYTE, value -> (byte) (value.headRot * 256 / 360),
            VAR_INT, SpawnEntityPacket::data,
            (entityId, uuid, type, x, y, z, velocity, pitch, yaw, headRot, data) ->
                    new SpawnEntityPacket(entityId, uuid, type,
                            new Pos(x, y, z, yaw * 360f / 256f, pitch * 360f / 256f),
                            headRot * 360f / 256f, data, velocity)
    );
}
