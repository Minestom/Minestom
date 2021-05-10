package net.minestom.server.network.packet.server.handshake;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ResponsePacket implements ServerPacket {

    public String jsonResponse = "";

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(jsonResponse);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        jsonResponse = reader.readSizedString(Integer.MAX_VALUE);
    }

    @Override
    public int getId() {
        return 0x00;
    }
}
