package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class CloseWindowPacket implements ServerPacket {

    public int windowId;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(windowId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.CLOSE_WINDOW;
    }
}
