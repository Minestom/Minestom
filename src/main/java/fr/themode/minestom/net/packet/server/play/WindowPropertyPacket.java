package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

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
