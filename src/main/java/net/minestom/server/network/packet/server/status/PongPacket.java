package net.minestom.server.network.packet.server.status;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.LONG;

public record PongPacket(long number) implements ServerPacket {
    public PongPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(LONG));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(LONG, number);
    }

    @Override
    public int getId() {
        return 0x01;
    }
}
