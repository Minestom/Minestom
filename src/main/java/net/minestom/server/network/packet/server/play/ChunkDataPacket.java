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
        public void write(@NotNull NetworkBuffer writer, ChunkDataPacket value) {
            writer.write(INT, value.chunkX);
            writer.write(INT, value.chunkZ);
            writer.write(value.chunkData);
            writer.write(value.lightData);
        }

        @Override
        public ChunkDataPacket read(@NotNull NetworkBuffer reader) {
            return new ChunkDataPacket(reader.read(INT), reader.read(INT),
                    new ChunkData(reader),
                    new LightData(reader));
        }
    };
}