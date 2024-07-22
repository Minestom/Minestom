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

    public static final NetworkBuffer.Type<ClientUpdateSignPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer writer, @NotNull ClientUpdateSignPacket value) {
            writer.write(BLOCK_POSITION, value.blockPosition);
            writer.write(BOOLEAN, value.isFrontText);
            writer.write(STRING, value.lines.get(0));
            writer.write(STRING, value.lines.get(1));
            writer.write(STRING, value.lines.get(2));
            writer.write(STRING, value.lines.get(3));
        }

        @Override
        public @NotNull ClientUpdateSignPacket read(@NotNull NetworkBuffer reader) {
            return new ClientUpdateSignPacket(reader.read(BLOCK_POSITION), reader.read(BOOLEAN), readLines(reader));
        }
    };

    private static List<String> readLines(@NotNull NetworkBuffer reader) {
        return List.of(reader.read(STRING), reader.read(STRING),
                reader.read(STRING), reader.read(STRING));
    }
}
