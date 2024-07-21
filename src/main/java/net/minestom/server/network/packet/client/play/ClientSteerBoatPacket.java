package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;

public record ClientSteerBoatPacket(boolean leftPaddleTurning, boolean rightPaddleTurning) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientSteerBoatPacket> SERIALIZER = NetworkBufferTemplate.template(
            BOOLEAN, ClientSteerBoatPacket::leftPaddleTurning,
            BOOLEAN, ClientSteerBoatPacket::rightPaddleTurning,
            ClientSteerBoatPacket::new);
}
