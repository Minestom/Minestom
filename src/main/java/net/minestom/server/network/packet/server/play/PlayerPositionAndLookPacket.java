package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.RelativeFlags;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record PlayerPositionAndLookPacket(
        int teleportId, @NotNull Point position, @NotNull Point delta,
        float yaw, float pitch,
        @MagicConstant(flagsFromClass = RelativeFlags.class) int flags
) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<PlayerPositionAndLookPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, PlayerPositionAndLookPacket::teleportId,
            VECTOR3D, PlayerPositionAndLookPacket::position,
            VECTOR3D, PlayerPositionAndLookPacket::delta,
            FLOAT, PlayerPositionAndLookPacket::yaw,
            FLOAT, PlayerPositionAndLookPacket::pitch,
            INT, PlayerPositionAndLookPacket::flags,
            PlayerPositionAndLookPacket::new);
}