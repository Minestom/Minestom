package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record ClientSelectBundleItemPacket(int slot, int selectedIndex) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientSelectBundleItemPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientSelectBundleItemPacket::slot,
            VAR_INT, ClientSelectBundleItemPacket::selectedIndex,
            ClientSelectBundleItemPacket::new);
}
