package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityAnimationPacket(int entityId, Animation animation) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityAnimationPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, EntityAnimationPacket::entityId,
            Animation.NETWORK_TYPE, EntityAnimationPacket::animation,
            EntityAnimationPacket::new
    );

    public enum Animation {
        SWING_MAIN_ARM,
        TAKE_DAMAGE,
        LEAVE_BED,
        SWING_OFF_HAND,
        CRITICAL_EFFECT,
        MAGICAL_CRITICAL_EFFECT;

        public static final NetworkBuffer.Type<Animation> NETWORK_TYPE = NetworkBuffer.ByteEnum(Animation.class);
    }
}
