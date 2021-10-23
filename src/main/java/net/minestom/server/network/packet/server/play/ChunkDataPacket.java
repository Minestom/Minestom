package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ChunkDataPacket(int chunkX, int chunkZ, ChunkData chunkData, LightData lightData)
        implements ServerPacket {
    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(chunkX);
        writer.writeInt(chunkZ);
        this.chunkData.write(writer);
        this.lightData.write(writer);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CHUNK_DATA;
    }
}