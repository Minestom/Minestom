package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.*;

public record WorldEventPacket(int effectId, Point position, int data,
                               boolean disableRelativeVolume) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<WorldEventPacket> SERIALIZER = NetworkBufferTemplate.template(
            INT, WorldEventPacket::effectId,
            BLOCK_POSITION, WorldEventPacket::position,
            INT, WorldEventPacket::data,
            BOOLEAN, WorldEventPacket::disableRelativeVolume,
            WorldEventPacket::new);
}
