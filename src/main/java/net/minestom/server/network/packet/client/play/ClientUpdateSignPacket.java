package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientUpdateSignPacket(
        Point blockPosition,
        boolean isFrontText,
        List<String> lines
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

    public static final NetworkBuffer.Type<ClientUpdateSignPacket> SERIALIZER = NetworkBufferTemplate.template(
            BLOCK_POSITION, ClientUpdateSignPacket::blockPosition,
            BOOLEAN, ClientUpdateSignPacket::isFrontText,
            STRING, p -> p.lines.getFirst(),
            STRING, p -> p.lines.get(1),
            STRING, p -> p.lines.get(2),
            STRING, p -> p.lines.get(3),
            (pos, isFront, l0, l1, l2, l3) -> new ClientUpdateSignPacket(pos, isFront, List.of(l0, l1, l2, l3))
    );
}
