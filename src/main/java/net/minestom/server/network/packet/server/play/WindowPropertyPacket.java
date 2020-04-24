package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class WindowPropertyPacket implements ServerPacket {

    public byte windowId;
    public short property;
    public short value;

    @Override
    public void write(PacketWriter writer) {
        writer.writeByte(windowId);
        writer.writeShort(property);
        writer.writeShort(value);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WINDOW_PROPERTY;
    }
}
