package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class KeepAlivePacket implements ServerPacket {

    public long id;

    public KeepAlivePacket(long id) {
        this.id = id;
    }

    @Override
    public void write(PacketWriter writer) {
        writer.writeLong(id);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.KEEP_ALIVE;
    }
}
