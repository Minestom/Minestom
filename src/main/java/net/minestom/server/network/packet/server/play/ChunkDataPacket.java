package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.network.packet.server.play.data.ChunkData;
import net.minestom.server.network.packet.server.play.data.LightData;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ChunkDataPacket implements ServerPacket {
    public int chunkX, chunkZ;
    public ChunkData chunkData;
    public LightData lightData;

    public ChunkDataPacket() {
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(chunkX);
        writer.writeInt(chunkZ);
        this.chunkData.write(writer);
        this.lightData.write(writer);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.chunkX = reader.readInt();
        this.chunkZ = reader.readInt();
        // TODO read
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CHUNK_DATA;
    }
}