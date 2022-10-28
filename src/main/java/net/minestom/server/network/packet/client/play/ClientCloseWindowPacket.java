package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;

public record ClientCloseWindowPacket(byte windowId) implements ClientPacket {
    public ClientCloseWindowPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BYTE));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
    }
}
