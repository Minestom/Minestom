package net.minestom.server.network.packet.client.play;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientVehicleMovePacket(Pos position, boolean onGround) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientVehicleMovePacket> SERIALIZER = NetworkBufferTemplate.template(
            POS, ClientVehicleMovePacket::position,
            BOOLEAN, ClientVehicleMovePacket::onGround,
            ClientVehicleMovePacket::new);
}
