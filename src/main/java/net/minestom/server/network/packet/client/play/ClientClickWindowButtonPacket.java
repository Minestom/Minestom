package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientClickWindowButtonPacket(byte windowId, byte buttonId) implements ClientPacket {
    public ClientClickWindowButtonPacket(BinaryReader reader) {
        this(reader.readByte(), reader.readByte());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeByte(buttonId);
    }
}
