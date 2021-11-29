package net.minestom.server.network.packet.server.handshake;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ResponsePacket(@NotNull String jsonResponse) implements ServerPacket {
    public ResponsePacket(BinaryReader reader) {
        this(reader.readSizedString());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(jsonResponse);
    }

    @Override
    public int getId() {
        return 0x00;
    }
}
