package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;

public record ClientClickWindowButtonPacket(byte windowId, byte buttonId) implements ClientPacket {
    public ClientClickWindowButtonPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BYTE), reader.read(BYTE));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BYTE, windowId);
        writer.write(BYTE, buttonId);
    }
}
