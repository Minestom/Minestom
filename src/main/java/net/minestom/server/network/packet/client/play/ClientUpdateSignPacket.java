package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientUpdateSignPacket(
        @NotNull Point blockPosition,
        boolean isFrontText,
        @NotNull List<String> lines
) implements ClientPacket {
    public ClientUpdateSignPacket {
        lines = List.copyOf(lines);
        if (lines.size() != 4) {
            throw new IllegalArgumentException("Signs must have 4 lines!");
        }
        for (String line : lines) {
            if (line.length() > 384) {
                throw new IllegalArgumentException("Signs must have a maximum of 384 characters per line!");
            }
        }
    }

    public ClientUpdateSignPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BLOCK_POSITION), reader.read(BOOLEAN), readLines(reader));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BLOCK_POSITION, blockPosition);
        writer.write(BOOLEAN, isFrontText);
        writer.write(STRING, lines.get(0));
        writer.write(STRING, lines.get(1));
        writer.write(STRING, lines.get(2));
        writer.write(STRING, lines.get(3));
    }

    private static List<String> readLines(@NotNull NetworkBuffer reader) {
        return List.of(reader.read(STRING), reader.read(STRING),
                reader.read(STRING), reader.read(STRING));
    }
}
