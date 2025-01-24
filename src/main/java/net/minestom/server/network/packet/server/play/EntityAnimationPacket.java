package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityAnimationPacket(int entityId, @NotNull Animation animation) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityAnimationPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, EntityAnimationPacket value) {
            buffer.write(VAR_INT, value.entityId);
            buffer.write(BYTE, (byte) value.animation.ordinal());
        }

        @Override
        public EntityAnimationPacket read(@NotNull NetworkBuffer buffer) {
            return new EntityAnimationPacket(buffer.read(VAR_INT), Animation.values()[buffer.read(BYTE)]);
        }
    };

    public enum Animation {
        SWING_MAIN_ARM,
        TAKE_DAMAGE,
        LEAVE_BED,
        SWING_OFF_HAND,
        CRITICAL_EFFECT,
        MAGICAL_CRITICAL_EFFECT
    }
}
