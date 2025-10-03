package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

public record EntityVelocityPacket(int entityId, Vec velocity) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityVelocityPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, EntityVelocityPacket::entityId,
            NetworkBuffer.LP_VECTOR3, EntityVelocityPacket::velocity,
            EntityVelocityPacket::new);
}
