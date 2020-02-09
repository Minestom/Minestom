package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

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
        return 0x41;
    }
}
