package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class CloseWindowPacket implements ServerPacket {

    public byte windowId;

    @Override
    public void write(PacketWriter writer) {
        writer.writeByte(windowId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CLOSE_WINDOW;
    }
}
