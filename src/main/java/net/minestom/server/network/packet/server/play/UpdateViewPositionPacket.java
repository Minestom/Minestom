package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class UpdateViewPositionPacket implements ServerPacket {

    public int chunkX, chunkZ;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(chunkX);
        writer.writeVarInt(chunkZ);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_VIEW_POSITION;
    }
}
