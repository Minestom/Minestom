package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.SHORT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record EntityVelocityPacket(int entityId, short velocityX, short velocityY,
                                   short velocityZ) implements ServerPacket {
    public EntityVelocityPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT), reader.read(SHORT), reader.read(SHORT), reader.read(SHORT));
    }

    public EntityVelocityPacket(int entityId, Point velocity) {
        this(entityId, (short) velocity.x(), (short) velocity.y(), (short) velocity.z());
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, entityId);
        writer.write(SHORT, velocityX);
        writer.write(SHORT, velocityY);
        writer.write(SHORT, velocityZ);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.ENTITY_VELOCITY;
    }
}
