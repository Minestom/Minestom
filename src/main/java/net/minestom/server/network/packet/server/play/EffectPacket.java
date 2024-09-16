package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.*;

public record EffectPacket(int effectId, Point position, int data,
                           boolean disableRelativeVolume) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EffectPacket> SERIALIZER = NetworkBufferTemplate.template(
            INT, EffectPacket::effectId,
            BLOCK_POSITION, EffectPacket::position,
            INT, EffectPacket::data,
            BOOLEAN, EffectPacket::disableRelativeVolume,
            EffectPacket::new);
}
