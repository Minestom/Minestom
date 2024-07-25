package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityTeleportPacket(int entityId, Pos position, boolean onGround) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityTeleportPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, @NotNull EntityTeleportPacket value) {
            buffer.write(VAR_INT, value.entityId);
            buffer.write(DOUBLE, value.position.x());
            buffer.write(DOUBLE, value.position.y());
            buffer.write(DOUBLE, value.position.z());
            buffer.write(BYTE, (byte) (value.position.yaw() * 256f / 360f));
            buffer.write(BYTE, (byte) (value.position.pitch() * 256f / 360f));
            buffer.write(BOOLEAN, value.onGround);
        }

        @Override
        public @NotNull EntityTeleportPacket read(@NotNull NetworkBuffer buffer) {
            return new EntityTeleportPacket(buffer.read(VAR_INT), new Pos(buffer.read(DOUBLE), buffer.read(DOUBLE), buffer.read(DOUBLE),
                            buffer.read(BYTE) * 360f / 256f, buffer.read(BYTE) * 360f / 256f),
                    buffer.read(BOOLEAN));
        }
    };
}
