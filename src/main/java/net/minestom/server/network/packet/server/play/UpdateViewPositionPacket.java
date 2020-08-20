package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class UpdateViewPositionPacket implements ServerPacket {

    public int chunkX, chunkZ;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(chunkX);
        writer.writeVarInt(chunkZ);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.UPDATE_VIEW_POSITION;
    }
}
