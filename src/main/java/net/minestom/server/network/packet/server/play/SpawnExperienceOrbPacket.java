package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.*;

public record SpawnExperienceOrbPacket(int entityId,
                                       @NotNull Point position, short expCount) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<SpawnExperienceOrbPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, SpawnExperienceOrbPacket::entityId,
            VECTOR3D, SpawnExperienceOrbPacket::position,
            SHORT, SpawnExperienceOrbPacket::expCount,
            SpawnExperienceOrbPacket::new);
}
