package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.minestom.server.network.NetworkBuffer.*;

public record SpawnEntityPacket(int entityId, @NotNull UUID uuid, int type,
                                @NotNull Pos position, float headRot, int data,
                                short velocityX, short velocityY, short velocityZ) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SpawnEntityPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, @NotNull SpawnEntityPacket value) {
            buffer.write(VAR_INT, value.entityId);
            buffer.write(UUID, value.uuid);
            buffer.write(VAR_INT, value.type);

            buffer.write(DOUBLE, value.position.x());
            buffer.write(DOUBLE, value.position.y());
            buffer.write(DOUBLE, value.position.z());

            buffer.write(BYTE, (byte) (value.position.pitch() * 256 / 360));
            buffer.write(BYTE, (byte) (value.position.yaw() * 256 / 360));
            buffer.write(BYTE, (byte) (value.headRot * 256 / 360));

            buffer.write(VAR_INT, value.data);

            buffer.write(SHORT, value.velocityX);
            buffer.write(SHORT, value.velocityY);
            buffer.write(SHORT, value.velocityZ);
        }

        @Override
        public @NotNull SpawnEntityPacket read(@NotNull NetworkBuffer buffer) {
            return new SpawnEntityPacket(buffer.read(VAR_INT), buffer.read(UUID), buffer.read(VAR_INT),
                    new Pos(buffer.read(DOUBLE), buffer.read(DOUBLE), buffer.read(DOUBLE),
                            buffer.read(BYTE) * 360f / 256f, buffer.read(BYTE) * 360f / 256f), buffer.read(BYTE) * 360f / 256f,
                    buffer.read(VAR_INT), buffer.read(SHORT), buffer.read(SHORT), buffer.read(SHORT));
        }
    };
}
