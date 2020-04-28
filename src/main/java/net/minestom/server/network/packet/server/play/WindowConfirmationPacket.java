package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class WindowConfirmationPacket implements ServerPacket {

    public byte windowId;
    public short actionNumber;
    public boolean accepted;

    @Override
    public void write(PacketWriter writer) {
        writer.writeByte(windowId);
        writer.writeShort(actionNumber);
        writer.writeBoolean(accepted);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WINDOW_CONFIRMATION;
    }
}
