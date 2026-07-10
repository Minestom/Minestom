package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

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

    public static EntityPositionPacket getPacket(int entityId,
                                                 Pos newPosition, Pos oldPosition,
                                                 boolean onGround) {
        final short deltaX = CoordConversion.deltaShort4096(newPosition.x(), oldPosition.x());
        final short deltaY = CoordConversion.deltaShort4096(newPosition.y(), oldPosition.y());
        final short deltaZ = CoordConversion.deltaShort4096(newPosition.z(), oldPosition.z());
        return new EntityPositionPacket(entityId, deltaX, deltaY, deltaZ, onGround);
    }
}
