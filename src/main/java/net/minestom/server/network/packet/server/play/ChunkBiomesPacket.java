package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record ChunkBiomesPacket(@NotNull List<@NotNull ChunkBiomeData> chunks) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ChunkBiomesPacket> SERIALIZER = NetworkBufferTemplate.template(
            ChunkBiomeData.SERIALIZER.list(), ChunkBiomesPacket::chunks,
            ChunkBiomesPacket::new);

    public ChunkBiomesPacket {
        chunks = List.copyOf(chunks);
    }

    public record ChunkBiomeData(int chunkX, int chunkZ, byte[] data) {
        public static final NetworkBuffer.Type<ChunkBiomeData> SERIALIZER = new NetworkBuffer.Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, ChunkBiomeData value) {
                buffer.write(INT, value.chunkZ); // x and z are inverted, not a bug
                buffer.write(INT, value.chunkX);
                buffer.write(BYTE_ARRAY, value.data);
            }

            @Override
            public ChunkBiomeData read(@NotNull NetworkBuffer buffer) {
                int chunkZ = buffer.read(INT);
                int chunkX = buffer.read(INT);
                byte[] data = buffer.read(BYTE_ARRAY);
                return new ChunkBiomeData(chunkX, chunkZ, data);
            }
        };
    }
}
