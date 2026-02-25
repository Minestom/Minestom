package net.minestom.server.network.packet.client.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.client.ClientPacket;

import java.util.Map;

import static net.minestom.server.network.NetworkBuffer.*;

public record ClientClickWindowPacket(int windowId, int stateId,
                                      short slot, byte button, ClickType clickType,
                                      Map<Short, ItemStack.Hash> changedSlots,
                                      ItemStack.Hash clickedItem) implements ClientPacket {
    public static final int MAX_CHANGED_SLOTS = 128;

    public static final NetworkBuffer.Type<ClientClickWindowPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, ClientClickWindowPacket::windowId,
            VAR_INT, ClientClickWindowPacket::stateId,
            SHORT, ClientClickWindowPacket::slot,
            BYTE, ClientClickWindowPacket::button,
            Enum(ClickType.class), ClientClickWindowPacket::clickType,
            SHORT.mapValue(ItemStack.Hash.NETWORK_TYPE, MAX_CHANGED_SLOTS), ClientClickWindowPacket::changedSlots,
            ItemStack.Hash.NETWORK_TYPE, ClientClickWindowPacket::clickedItem,
            ClientClickWindowPacket::new);

    public ClientClickWindowPacket {
        changedSlots = Map.copyOf(changedSlots);
    }

    public enum ClickType {
        PICKUP,
        QUICK_MOVE,
        SWAP,
        CLONE,
        THROW,
        QUICK_CRAFT,
        PICKUP_ALL
    }
}
