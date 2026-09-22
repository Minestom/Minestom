package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityAnimationPacket(int entityId, Animation animation) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityAnimationPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, EntityAnimationPacket::entityId,
            NetworkBuffer.Enum(Animation.class), EntityAnimationPacket::animation,
            EntityAnimationPacket::new
    );

    public enum Animation {
        LEAVE_BED,
        CRITICAL_EFFECT,
        MAGICAL_CRITICAL_EFFECT
    }
}
