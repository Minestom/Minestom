package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.RelativeFlags;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.intellij.lang.annotations.MagicConstant;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityTeleportPacket(
        int entityId, Pos position, Point delta,
        @MagicConstant(flagsFromClass = RelativeFlags.class) int flags,
        boolean onGround) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityTeleportPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, EntityTeleportPacket::entityId,
            VECTOR3D, EntityTeleportPacket::position,
            VECTOR3D, EntityTeleportPacket::delta,
            FLOAT, value -> value.position.yaw(),
            FLOAT, value -> value.position.pitch(),
            INT, EntityTeleportPacket::flags,
            BOOLEAN, EntityTeleportPacket::onGround,
            (entityId, absPosition, deltaMovement, yaw, pitch, flags, onGround) ->
                    new EntityTeleportPacket(entityId, new Pos(absPosition, yaw, pitch), deltaMovement, flags, onGround)
    );
}
