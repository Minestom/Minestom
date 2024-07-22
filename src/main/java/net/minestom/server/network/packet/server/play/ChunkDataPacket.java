package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.INT;

public record ChunkDataPacket(int chunkX, int chunkZ,
                              @NotNull ChunkData chunkData,
                              @NotNull LightData lightData) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ChunkDataPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, ChunkDataPacket value) {
            buffer.write(INT, value.chunkX);
            buffer.write(INT, value.chunkZ);
            buffer.write(value.chunkData);
            buffer.write(value.lightData);
        }

        @Override
        public ChunkDataPacket read(@NotNull NetworkBuffer buffer) {
            return new ChunkDataPacket(buffer.read(INT), buffer.read(INT),
                    new ChunkData(buffer),
                    new LightData(buffer));
        }
    };
}