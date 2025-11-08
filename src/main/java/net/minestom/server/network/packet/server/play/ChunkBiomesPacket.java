package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.Arrays;
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
        public static final NetworkBuffer.Type<ChunkBiomeData> SERIALIZER = NetworkBufferTemplate.template(
                INT, ChunkBiomeData::chunkZ, // x and z are inverted, not a bug
                INT, ChunkBiomeData::chunkX,
                BYTE_ARRAY, ChunkBiomeData::data,
                (chunkZ, chunkX, data) -> new ChunkBiomeData(chunkX, chunkZ, data));

        public ChunkBiomeData {
            data = data.clone();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ChunkBiomeData(int x, int z, byte[] data1))) return false;
            return chunkX() == x && chunkZ() == z && Arrays.equals(data(), data1);
        }

        @Override
        public int hashCode() {
            int result = chunkX();
            result = 31 * result + chunkZ();
            result = 31 * result + Arrays.hashCode(data());
            return result;
        }
    }
}
