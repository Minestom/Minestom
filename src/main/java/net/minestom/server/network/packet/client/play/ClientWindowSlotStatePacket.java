package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.BOOLEAN;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

// This is the packet sent when you toggle a slot in a crafter UI
public record ClientWindowSlotStatePacket(int slot, int windowId, boolean newState) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientWindowSlotStatePacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientWindowSlotStatePacket::slot,
            VAR_INT, ClientWindowSlotStatePacket::windowId,
            BOOLEAN, ClientWindowSlotStatePacket::newState,
            ClientWindowSlotStatePacket::new);
}
