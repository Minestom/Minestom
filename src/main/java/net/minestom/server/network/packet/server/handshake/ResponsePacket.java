package net.minestom.server.network.packet.server.handshake;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;

public class ResponsePacket implements ServerPacket {

    public String jsonResponse;

    @Override
    public void write(PacketWriter writer) {
        writer.writeSizedString(jsonResponse);
    }

    @Override
    public int getId() {
        return 0x00;
    }
}
