package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.RelativeFlags;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityTeleportPacket(
        int entityId, Pos position, Point delta,
        @MagicConstant(flagsFromClass = RelativeFlags.class) int flags,
        boolean onGround) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<EntityTeleportPacket> SERIALIZER = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, @NotNull EntityTeleportPacket value) {
            buffer.write(VAR_INT, value.entityId);
            buffer.write(VECTOR3D, value.position.asVec());
            buffer.write(VECTOR3D, value.delta);
            buffer.write(FLOAT, value.position.yaw());
            buffer.write(FLOAT, value.position.pitch());
            buffer.write(INT, value.flags);
            buffer.write(BOOLEAN, value.onGround);
        }

        @Override
        public @NotNull EntityTeleportPacket read(@NotNull NetworkBuffer buffer) {
            int entityId = buffer.read(VAR_INT);
            // Order is x,y,z for position, then x,y,z for delta move, then yaw and pitch
            Point absPosition = buffer.read(VECTOR3D);
            Point deltaMovement = buffer.read(VECTOR3D);
            return new EntityTeleportPacket(entityId, new Pos(absPosition, buffer.read(FLOAT), buffer.read(FLOAT)),
                            deltaMovement, buffer.read(INT), buffer.read(BOOLEAN));
        }
    };
}
