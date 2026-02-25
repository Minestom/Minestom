package net.minestom.server.network.packet.client.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import static net.minestom.server.network.NetworkBuffer.SHORT;

public record ClientCreativeInventoryActionPacket(short slot, ItemStack item) implements ClientPacket {
    public static final NetworkBuffer.Type<ClientCreativeInventoryActionPacket> SERIALIZER = NetworkBufferTemplate.template(
            SHORT, ClientCreativeInventoryActionPacket::slot,
            ItemStack.UNTRUSTED_NETWORK_TYPE, ClientCreativeInventoryActionPacket::item,
            ClientCreativeInventoryActionPacket::new);
}
