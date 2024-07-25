package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityHeadLookPacket(int entityId, float yaw) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityHeadLookPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, EntityHeadLookPacket value) {
            buffer.write(VAR_INT, value.entityId);
            buffer.write(BYTE, (byte) (value.yaw * 256 / 360));
        }

        @Override
        public EntityHeadLookPacket read(@NotNull NetworkBuffer buffer) {
            return new EntityHeadLookPacket(buffer.read(VAR_INT), (buffer.read(BYTE) * 360f) / 256f);
        }
    };
}
