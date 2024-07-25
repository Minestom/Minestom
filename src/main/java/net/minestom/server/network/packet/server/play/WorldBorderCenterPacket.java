package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.DOUBLE;

public record WorldBorderCenterPacket(double x, double z) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<WorldBorderCenterPacket> SERIALIZER = NetworkBufferTemplate.template(
            DOUBLE, WorldBorderCenterPacket::x,
            DOUBLE, WorldBorderCenterPacket::z,
            WorldBorderCenterPacket::new);
}
