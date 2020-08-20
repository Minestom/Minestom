package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class CloseWindowPacket implements ServerPacket {

    public byte windowId;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeByte(windowId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CLOSE_WINDOW;
    }
}
