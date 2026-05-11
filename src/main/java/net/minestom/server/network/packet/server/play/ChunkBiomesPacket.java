package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.BYTE_ARRAY;
import static net.minestom.server.network.NetworkBuffer.INT;

public record ChunkBiomesPacket(List<ChunkBiomeData> chunks) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ChunkBiomesPacket> SERIALIZER = NetworkBufferTemplate.template(
            ChunkBiomeData.SERIALIZER.list(), ChunkBiomesPacket::chunks,
            ChunkBiomesPacket::new);

    public ChunkBiomesPacket {
        chunks = List.copyOf(chunks);
    }

    public record ChunkBiomeData(int chunkX, int chunkZ, byte[] data) {
        // x and z are inverted, not a bug
        public static final NetworkBuffer.Type<ChunkBiomeData> SERIALIZER = NetworkBufferTemplate.template(
                INT, ChunkBiomeData::chunkZ,
                INT, ChunkBiomeData::chunkX,
                BYTE_ARRAY, ChunkBiomeData::data,
                (z, x, data) -> new ChunkBiomeData(x, z, data)
        );
    }
}
