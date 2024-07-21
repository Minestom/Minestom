package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.SHORT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityVelocityPacket(int entityId, short velocityX, short velocityY,
                                   short velocityZ) implements ServerPacket.Play {
    public EntityVelocityPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(SHORT), reader.read(SHORT), reader.read(SHORT));
    }

    public EntityVelocityPacket(int entityId, Point velocity) {
        this(
                entityId,
                (short) MathUtils.clamp(velocity.x(), Short.MIN_VALUE, Short.MAX_VALUE),
                (short) MathUtils.clamp(velocity.y(), Short.MIN_VALUE, Short.MAX_VALUE),
                (short) MathUtils.clamp(velocity.z(), Short.MIN_VALUE, Short.MAX_VALUE)
        );
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(SHORT, velocityX);
        writer.write(SHORT, velocityY);
        writer.write(SHORT, velocityZ);
    }

}
