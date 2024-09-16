package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientSelectTradePacket(int selectedSlot) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientSelectTradePacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientSelectTradePacket::selectedSlot,
            ClientSelectTradePacket::new);
}
