package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.INT;

public record ChunkDataPacket(int chunkX, int chunkZ,
                              @NotNull ChunkData chunkData,
                              @NotNull LightData lightData) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ChunkDataPacket> SERIALIZER = NetworkBufferTemplate.template(
            INT, ChunkDataPacket::chunkX,
            INT, ChunkDataPacket::chunkZ,
            ChunkData.NETWORK_TYPE, ChunkDataPacket::chunkData,
            LightData.SERIALIZER, ChunkDataPacket::lightData,
            ChunkDataPacket::new
    );
}