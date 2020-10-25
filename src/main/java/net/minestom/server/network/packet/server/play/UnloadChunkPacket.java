package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class UnloadChunkPacket implements ServerPacket {

    public int chunkX, chunkZ;

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeInt(chunkX);
        writer.writeInt(chunkZ);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UNLOAD_CHUNK;
    }
}
