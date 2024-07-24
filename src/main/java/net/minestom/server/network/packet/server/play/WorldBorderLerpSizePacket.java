package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.DOUBLE;
import static net.minestom.server.network.NetworkBuffer.VAR_LONG;

public record WorldBorderLerpSizePacket(double oldDiameter, double newDiameter,
                                        long speed) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<WorldBorderLerpSizePacket> SERIALIZER = NetworkBufferTemplate.template(
            DOUBLE, WorldBorderLerpSizePacket::oldDiameter,
            DOUBLE, WorldBorderLerpSizePacket::newDiameter,
            VAR_LONG, WorldBorderLerpSizePacket::speed,
            WorldBorderLerpSizePacket::new);
}
