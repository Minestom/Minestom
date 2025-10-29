package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityRotationPacket(int entityId, float yaw, float pitch,
                                   boolean onGround) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityRotationPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, EntityRotationPacket::entityId,
            LP_ANGLE, EntityRotationPacket::yaw,
            LP_ANGLE, EntityRotationPacket::pitch,
            BOOLEAN, EntityRotationPacket::onGround,
            EntityRotationPacket::new
    );
}
