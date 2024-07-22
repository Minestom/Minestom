package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityTeleportPacket(int entityId, Pos position, boolean onGround) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityTeleportPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, @NotNull EntityTeleportPacket value) {
            writer.write(VAR_INT, value.entityId);
            writer.write(DOUBLE, value.position.x());
            writer.write(DOUBLE, value.position.y());
            writer.write(DOUBLE, value.position.z());
            writer.write(BYTE, (byte) (value.position.yaw() * 256f / 360f));
            writer.write(BYTE, (byte) (value.position.pitch() * 256f / 360f));
            writer.write(BOOLEAN, value.onGround);
        }

        @Override
        public @NotNull EntityTeleportPacket read(@NotNull NetworkBuffer reader) {
            return new EntityTeleportPacket(reader.read(VAR_INT), new Pos(reader.read(DOUBLE), reader.read(DOUBLE), reader.read(DOUBLE),
                            reader.read(BYTE) * 360f / 256f, reader.read(BYTE) * 360f / 256f),
                    reader.read(BOOLEAN));
        }
    };
}
