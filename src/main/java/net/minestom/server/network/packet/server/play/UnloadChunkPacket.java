package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class UnloadChunkPacket implements ServerPacket {

    public int chunkX, chunkZ;

    @Override
    public void write(PacketWriter writer) {
        writer.writeInt(chunkX);
        writer.writeInt(chunkZ);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UNLOAD_CHUNK;
    }
}
