package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.SHORT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityVelocityPacket(int entityId, int velocityX, int velocityY, int velocityZ) implements ServerPacket {
    public EntityVelocityPacket {
        velocityX = MathUtils.clamp(velocityX, Short.MIN_VALUE, Short.MAX_VALUE);
        velocityY = MathUtils.clamp(velocityY, Short.MIN_VALUE, Short.MAX_VALUE);
        velocityZ = MathUtils.clamp(velocityZ, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    public EntityVelocityPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(SHORT), reader.read(SHORT), reader.read(SHORT));
    }

    public EntityVelocityPacket(int entityId, Point velocity) {
        this(entityId, (int) velocity.x(), (int) velocity.y(), (int) velocity.z());
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(SHORT, (short) velocityX);
        writer.write(SHORT, (short) velocityY);
        writer.write(SHORT, (short) velocityZ);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_VELOCITY;
    }
}
