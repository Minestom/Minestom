package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.FLOAT;

public record ClientSteerVehiclePacket(float sideways, float forward,
                                       byte flags) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientSteerVehiclePacket> SERIALIZER = NetworkBufferTemplate.template(
            FLOAT, ClientSteerVehiclePacket::sideways,
            FLOAT, ClientSteerVehiclePacket::forward,
            BYTE, ClientSteerVehiclePacket::flags,
            ClientSteerVehiclePacket::new);
}
