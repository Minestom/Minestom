package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityAnimationPacket(int entityId, @NotNull Animation animation) implements ServerPacket.Play {
    public EntityAnimationPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), Animation.values()[reader.read(BYTE)]);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(BYTE, (byte) animation.ordinal());
    }

    public enum Animation {
        SWING_MAIN_ARM,
        TAKE_DAMAGE,
        LEAVE_BED,
        SWING_OFF_HAND,
        CRITICAL_EFFECT,
        MAGICAL_CRITICAL_EFFECT
    }
}
