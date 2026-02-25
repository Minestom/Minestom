package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityRotationPacket(int entityId, float yaw, float pitch,
                                   boolean onGround) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityRotationPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(NetworkBuffer buffer, EntityRotationPacket value) {
            buffer.write(VAR_INT, value.entityId);
            buffer.write(BYTE, (byte) (value.yaw * 256 / 360));
            buffer.write(BYTE, (byte) (value.pitch * 256 / 360));
            buffer.write(BOOLEAN, value.onGround);
        }

        @Override
        public EntityRotationPacket read(NetworkBuffer buffer) {
            return new EntityRotationPacket(buffer.read(VAR_INT),
                    buffer.read(BYTE) * 360f / 256f, buffer.read(BYTE) * 360f / 256f,
                    buffer.read(BOOLEAN));
        }
    };
}
