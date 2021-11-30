package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ChunkDataPacket(int chunkX, int chunkZ,
                              @NotNull ChunkData chunkData,
                              @NotNull LightData lightData) implements ServerPacket {
    public ChunkDataPacket(BinaryReader reader) {
        this(reader.readInt(), reader.readInt(),
                new ChunkData(reader),
                new LightData(reader));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(chunkX);
        writer.writeInt(chunkZ);
        writer.write(chunkData);
        writer.write(lightData);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CHUNK_DATA;
    }
}