package net.minestom.server.network.packet.server.play;

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

            buffer.write(DOUBLE, value.position.x());
            buffer.write(DOUBLE, value.position.y());
            buffer.write(DOUBLE, value.position.z());

            buffer.write(LP_VECTOR3, value.velocity);

            buffer.write(BYTE, (byte) (value.position.pitch() * 256 / 360));
            buffer.write(BYTE, (byte) (value.position.yaw() * 256 / 360));
            buffer.write(BYTE, (byte) (value.headRot * 256 / 360));

            buffer.write(VAR_INT, value.data);
        }

        @Override
        public SpawnEntityPacket read(NetworkBuffer buffer) {
            int entityId = buffer.read(VAR_INT);
            UUID uuid = buffer.read(UUID);
            EntityType type = buffer.read(EntityType.NETWORK_TYPE);
            double x = buffer.read(DOUBLE), y = buffer.read(DOUBLE), z = buffer.read(DOUBLE);
            Vec velocity = buffer.read(LP_VECTOR3);
            float pitch = buffer.read(BYTE) * 360f / 256f;
            float yaw = buffer.read(BYTE) * 360f / 256f;
            float headRot = buffer.read(BYTE) * 360f / 256f;
            int data = buffer.read(VAR_INT);
            return new SpawnEntityPacket(
                    entityId, uuid, type,
                    new Pos(x, y, z, yaw, pitch),
                    headRot, data, velocity
            );
        }
    };
}
