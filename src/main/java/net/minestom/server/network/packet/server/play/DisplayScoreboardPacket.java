package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record DisplayScoreboardPacket(byte position, String scoreName) implements ServerPacket.Play {
    public DisplayScoreboardPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BYTE), reader.read(STRING));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BYTE, position);
        writer.write(STRING, scoreName);
    }

}
