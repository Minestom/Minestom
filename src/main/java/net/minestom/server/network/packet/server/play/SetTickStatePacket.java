package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.FLOAT;

public record SetTickStatePacket(float tickRate, boolean isFrozen) implements ServerPacket.Play {

    public SetTickStatePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(FLOAT), reader.read(BOOLEAN));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(FLOAT, tickRate);
        writer.write(BOOLEAN, isFrozen);
    }

}
