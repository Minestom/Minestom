package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.POS;

public record ClientVehicleMovePacket(@NotNull Pos position) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientVehicleMovePacket> SERIALIZER = NetworkBufferTemplate.template(
            POS, ClientVehicleMovePacket::position,
            ClientVehicleMovePacket::new);
}
