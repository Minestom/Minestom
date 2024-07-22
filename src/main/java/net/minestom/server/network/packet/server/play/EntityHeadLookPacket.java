package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityHeadLookPacket(int entityId, float yaw) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityHeadLookPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, EntityHeadLookPacket value) {
            writer.write(VAR_INT, value.entityId);
            writer.write(BYTE, (byte) (value.yaw * 256 / 360));
        }

        @Override
        public EntityHeadLookPacket read(@NotNull NetworkBuffer reader) {
            return new EntityHeadLookPacket(reader.read(VAR_INT), (reader.read(BYTE) * 360f) / 256f);
        }
    };
}
