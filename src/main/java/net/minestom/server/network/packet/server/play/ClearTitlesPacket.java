package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record ClearTitlesPacket(boolean reset) implements ServerPacket.Play {
    public ClearTitlesPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BOOLEAN, reset);
    }

}
