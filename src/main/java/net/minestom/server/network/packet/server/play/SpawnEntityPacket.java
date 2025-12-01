package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.*;

public record SpawnEntityPacket(
        int entityId, UUID uuid, EntityType type,
        Pos position, float headRot, int data,
        Vec velocity
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SpawnEntityPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(NetworkBuffer buffer, SpawnEntityPacket value) {
            buffer.write(VAR_INT, value.entityId);
            buffer.write(UUID, value.uuid);
            buffer.write(EntityType.NETWORK_TYPE, value.type);
            buffer.write(VECTOR3D, value.position);
            buffer.write(LP_VECTOR3, value.velocity);
            buffer.write(LP_ANGLE, value.position.pitch());
            buffer.write(LP_ANGLE, value.position.yaw());
            buffer.write(LP_ANGLE, value.headRot);
            buffer.write(VAR_INT, value.data);
        }

        @Override
        public SpawnEntityPacket read(NetworkBuffer buffer) {
            int entityId = buffer.read(VAR_INT);
            UUID uuid = buffer.read(UUID);
            EntityType type = buffer.read(EntityType.NETWORK_TYPE);
            Point xyz = buffer.read(VECTOR3D);
            Vec velocity = buffer.read(LP_VECTOR3);
            float pitch = buffer.read(LP_ANGLE);
            float yaw = buffer.read(LP_ANGLE);
            float headRot = buffer.read(LP_ANGLE);
            int data = buffer.read(VAR_INT);
            return new SpawnEntityPacket(
                    entityId, uuid, type,
                    new Pos(xyz, yaw, pitch),
                    headRot, data, velocity
            );
        }
    };
}
