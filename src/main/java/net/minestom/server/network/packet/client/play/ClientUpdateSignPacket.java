package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.SignTextSlot;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.BLOCK_POSITION;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientUpdateSignPacket(
        Point blockPosition,
        List<String> lines,
        SignTextSlot slot
) implements ClientPacket.Play {
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
        public void write(NetworkBuffer buffer, ClientUpdateSignPacket value) {
            buffer.write(BLOCK_POSITION, value.blockPosition);
            buffer.write(STRING, value.lines.get(0));
            buffer.write(STRING, value.lines.get(1));
            buffer.write(STRING, value.lines.get(2));
            buffer.write(STRING, value.lines.get(3));
            buffer.write(SignTextSlot.NETWORK_TYPE, value.slot);
        }

        @Override
        public ClientUpdateSignPacket read(NetworkBuffer buffer) {
            final Point blockPosition = buffer.read(BLOCK_POSITION);
            final List<String> lines = readLines(buffer);
            return new ClientUpdateSignPacket(blockPosition, lines, buffer.read(SignTextSlot.NETWORK_TYPE));
        }
    };

    private static List<String> readLines(NetworkBuffer reader) {
        return List.of(reader.read(STRING), reader.read(STRING),
                reader.read(STRING), reader.read(STRING));
    }
}
