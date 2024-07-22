package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.INT;

public record UnloadChunkPacket(int chunkX, int chunkZ) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<UnloadChunkPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, UnloadChunkPacket value) {
            // Client reads this as a single long in big endian, so we have to write it backwards
            writer.write(INT, value.chunkZ);
            writer.write(INT, value.chunkX);
        }

        @Override
        public UnloadChunkPacket read(@NotNull NetworkBuffer reader) {
            int z = reader.read(INT);
            int x = reader.read(INT);
            return new UnloadChunkPacket(x, z);
        }
    };
}
