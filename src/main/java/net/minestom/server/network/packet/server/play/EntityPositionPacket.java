package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityPositionPacket(int entityId, short deltaX, short deltaY, short deltaZ, boolean onGround)
        implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityPositionPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, EntityPositionPacket::entityId,
            SHORT, EntityPositionPacket::deltaX,
            SHORT, EntityPositionPacket::deltaY,
            SHORT, EntityPositionPacket::deltaZ,
            BOOLEAN, EntityPositionPacket::onGround,
            EntityPositionPacket::new);

    @NotNull
    public static EntityPositionPacket getPacket(int entityId,
                                                 @NotNull Pos newPosition, @NotNull Pos oldPosition,
                                                 boolean onGround) {
        final short deltaX = (short) ((newPosition.x() * 32 - oldPosition.x() * 32) * 128);
        final short deltaY = (short) ((newPosition.y() * 32 - oldPosition.y() * 32) * 128);
        final short deltaZ = (short) ((newPosition.z() * 32 - oldPosition.z() * 32) * 128);
        return new EntityPositionPacket(entityId, deltaX, deltaY, deltaZ, onGround);
    }
}
