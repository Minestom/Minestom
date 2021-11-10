package net.minestom.server.network.packet.server.handshake;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ResponsePacket implements ServerPacket {

    public String jsonResponse;

    public ResponsePacket(String jsonResponse) {
        this.jsonResponse = jsonResponse;
    }

    public ResponsePacket() {
        this("");
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(jsonResponse);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        jsonResponse = reader.readSizedString();
    }

    @Override
    public int getId() {
        return 0x00;
    }
}
