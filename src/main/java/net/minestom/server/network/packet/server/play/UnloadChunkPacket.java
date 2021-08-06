package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class UnloadChunkPacket implements ServerPacket {

    public int chunkX, chunkZ;

    public UnloadChunkPacket(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public UnloadChunkPacket() {
        this(0, 0);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(chunkX);
        writer.writeInt(chunkZ);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        chunkX = reader.readInt();
        chunkZ = reader.readInt();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UNLOAD_CHUNK;
    }
}
