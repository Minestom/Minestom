package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

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
