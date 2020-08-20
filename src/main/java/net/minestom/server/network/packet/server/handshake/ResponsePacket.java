package net.minestom.server.network.packet.server.handshake;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.binary.BinaryWriter;

public class ResponsePacket implements ServerPacket {

    public String jsonResponse;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeSizedString(jsonResponse);
    }

    @Override
    public int getId() {
        return 0x00;
    }
}
