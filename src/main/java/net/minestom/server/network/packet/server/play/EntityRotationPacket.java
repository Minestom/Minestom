package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityRotationPacket(int entityId, float yaw, float pitch,
                                   boolean onGround) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityRotationPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, EntityRotationPacket value) {
            writer.write(VAR_INT, value.entityId);
            writer.write(BYTE, (byte) (value.yaw * 256 / 360));
            writer.write(BYTE, (byte) (value.pitch * 256 / 360));
            writer.write(BOOLEAN, value.onGround);
        }

        @Override
        public EntityRotationPacket read(@NotNull NetworkBuffer reader) {
            return new EntityRotationPacket(reader.read(VAR_INT),
                    reader.read(BYTE) * 360f / 256f, reader.read(BYTE) * 360f / 256f,
                    reader.read(BOOLEAN));
        }
    };
}
