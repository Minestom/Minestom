package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minestom.server.network.NetworkBuffer.VECTOR3D;

public record MoveMinecartPacket(int entityId, @NotNull List<LerpStep> lerpSteps) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<MoveMinecartPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, MoveMinecartPacket::entityId,
            LerpStep.SERIALIZER.list(Short.MAX_VALUE), MoveMinecartPacket::lerpSteps,
            MoveMinecartPacket::new);

    public record LerpStep(
            @NotNull Point position, @NotNull Point velocity,
            float yaw, float pitch, float weight
    ) {
        public static final NetworkBuffer.Type<LerpStep> SERIALIZER = NetworkBufferTemplate.template(
                VECTOR3D, LerpStep::position,
                VECTOR3D, LerpStep::velocity,
                NetworkBuffer.FLOAT, LerpStep::yaw,
                NetworkBuffer.FLOAT, LerpStep::pitch,
                NetworkBuffer.FLOAT, LerpStep::weight,
                LerpStep::new);
    }
}
