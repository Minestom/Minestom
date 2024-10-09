package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record PlayerPositionAndLookPacket(
        int teleportId, @NotNull Point position, @NotNull Point delta,
        float yaw, float pitch, int flags
) implements ServerPacket.Play {
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

    public static final NetworkBuffer.Type<PlayerPositionAndLookPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, PlayerPositionAndLookPacket::teleportId,
            VECTOR3D, PlayerPositionAndLookPacket::position,
            VECTOR3D, PlayerPositionAndLookPacket::delta,
            FLOAT, PlayerPositionAndLookPacket::yaw,
            FLOAT, PlayerPositionAndLookPacket::pitch,
            INT, PlayerPositionAndLookPacket::flags,
            PlayerPositionAndLookPacket::new);
}