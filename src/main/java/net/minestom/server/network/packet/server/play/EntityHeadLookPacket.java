package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.LP_ROTATION;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityHeadLookPacket(int entityId, float yaw) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityHeadLookPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, EntityHeadLookPacket::entityId,
            LP_ROTATION, EntityHeadLookPacket::yaw,
            EntityHeadLookPacket::new
    );
}
