package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.SHORT;

public record WindowPropertyPacket(byte windowId, short property, short value) implements ServerPacket.Play {
    public WindowPropertyPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BYTE), reader.read(SHORT), reader.read(SHORT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BYTE, windowId);
        writer.write(SHORT, property);
        writer.write(SHORT, value);
    }

}
