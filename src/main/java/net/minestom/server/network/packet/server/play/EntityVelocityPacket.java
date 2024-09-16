package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.MathUtils;

import static net.minestom.server.network.NetworkBuffer.SHORT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityVelocityPacket(int entityId, short velocityX, short velocityY,
                                   short velocityZ) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityVelocityPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, EntityVelocityPacket::entityId,
            SHORT, EntityVelocityPacket::velocityX,
            SHORT, EntityVelocityPacket::velocityY,
            SHORT, EntityVelocityPacket::velocityZ,
            EntityVelocityPacket::new);

    public EntityVelocityPacket(int entityId, Point velocity) {
        this(
                entityId,
                (short) MathUtils.clamp(velocity.x(), Short.MIN_VALUE, Short.MAX_VALUE),
                (short) MathUtils.clamp(velocity.y(), Short.MIN_VALUE, Short.MAX_VALUE),
                (short) MathUtils.clamp(velocity.z(), Short.MIN_VALUE, Short.MAX_VALUE)
        );
    }
}
