package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

public record GameTestHighlightPosPacket(
        Point absoluteBlockPosition,
        Point relativeBlockPosition
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<GameTestHighlightPosPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.BLOCK_POSITION, GameTestHighlightPosPacket::absoluteBlockPosition,
            NetworkBuffer.BLOCK_POSITION, GameTestHighlightPosPacket::relativeBlockPosition,
            GameTestHighlightPosPacket::new);
}
