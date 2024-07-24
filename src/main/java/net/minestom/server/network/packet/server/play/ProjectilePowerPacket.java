package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.DOUBLE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ProjectilePowerPacket(
        int entityId, double accelerationPower
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ProjectilePowerPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ProjectilePowerPacket::entityId,
            DOUBLE, ProjectilePowerPacket::accelerationPower,
            ProjectilePowerPacket::new);
}
