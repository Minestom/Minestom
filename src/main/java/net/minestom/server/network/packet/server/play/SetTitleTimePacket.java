package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.INT;

public record SetTitleTimePacket(int fadeIn, int stay, int fadeOut) implements ServerPacket.Play {
    public SetTitleTimePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(INT), reader.read(INT), reader.read(INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(INT, fadeIn);
        writer.write(INT, stay);
        writer.write(INT, fadeOut);
    }

}
