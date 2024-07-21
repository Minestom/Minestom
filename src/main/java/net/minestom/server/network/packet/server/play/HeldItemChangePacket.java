package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;

public record HeldItemChangePacket(byte slot) implements ServerPacket.Play {
    public HeldItemChangePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BYTE));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BYTE, slot);
    }

}
