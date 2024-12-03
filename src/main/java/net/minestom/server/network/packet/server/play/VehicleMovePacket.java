package net.minestom.server.network.packet.server.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.POS;

public record VehicleMovePacket(@NotNull Pos position) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<VehicleMovePacket> SERIALIZER = NetworkBufferTemplate.template(
            POS, VehicleMovePacket::position, VehicleMovePacket::new);
}
