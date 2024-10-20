package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record EntityTeleportPacket(int entityId, Pos position, Point delta, int flags, boolean onGround) implements ServerPacket.Play {
    public static final int FLAG_X = 1;
    public static final int FLAG_Y = 1 << 1;
    public static final int FLAG_Z = 1 << 2;
    public static final int FLAG_Y_ROT = 1 << 3;
    public static final int FLAG_X_ROT = 1 << 4;
    public static final int FLAG_DELTA_X = 1 << 5;
    public static final int FLAG_DELTA_Y = 1 << 6;
    public static final int FLAG_DELTA_Z = 1 << 7;
    public static final int FLAG_ROTATE_DELTA = 1 << 8;

    public static final int FLAG_DELTA = FLAG_DELTA_X | FLAG_DELTA_Y | FLAG_DELTA_Z | FLAG_ROTATE_DELTA;
    public static final int FLAG_ROTATION = FLAG_X_ROT | FLAG_Y_ROT;
    public static final int FLAG_ALL = FLAG_X | FLAG_Y | FLAG_Z | FLAG_ROTATION | FLAG_DELTA;

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
