package net.minestom.server.network.packet.server.play;

import net.minestom.server.instance.Chunk;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class UpdateViewPositionPacket implements ServerPacket {

    private Chunk chunk;

    public UpdateViewPositionPacket(Chunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(chunk.getChunkX());
        writer.writeVarInt(chunk.getChunkZ());
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_VIEW_POSITION;
    }
}
