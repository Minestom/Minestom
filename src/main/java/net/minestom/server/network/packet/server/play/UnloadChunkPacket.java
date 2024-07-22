package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.INT;

public record UnloadChunkPacket(int chunkX, int chunkZ) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<UnloadChunkPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, UnloadChunkPacket value) {
            // Client reads this as a single long in big endian, so we have to write it backwards
            buffer.write(INT, value.chunkZ);
            buffer.write(INT, value.chunkX);
        }

        @Override
        public UnloadChunkPacket read(@NotNull NetworkBuffer buffer) {
            int z = buffer.read(INT);
            int x = buffer.read(INT);
            return new UnloadChunkPacket(x, z);
        }
    };
}
