package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class OpenWindowPacket implements ServerPacket {

    public int windowId;
    public int windowType;
    public String title;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(windowId);
        writer.writeVarInt(windowType);
        writer.writeSizedString("{\"text\": \"" + title + " \"}");
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.OPEN_WINDOW;
    }
}
