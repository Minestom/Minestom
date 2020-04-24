package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class SpawnPositionPacket implements ServerPacket {

    public int x, y, z;

    @Override
    public void write(PacketWriter writer) {
        writer.writeBlockPosition(x, y, z);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SPAWN_POSITION;
    }
}
