package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record WindowItemsPacket(int windowId, int stateId, List<ItemStack> items,
                                ItemStack carriedItem) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final int MAX_ENTRIES = 128;

    public static final NetworkBuffer.Type<WindowItemsPacket> SERIALIZER = NetworkBufferTemplate.template(
            VAR_INT, WindowItemsPacket::windowId,
            VAR_INT, WindowItemsPacket::stateId,
            ItemStack.NETWORK_TYPE.list(MAX_ENTRIES), WindowItemsPacket::items,
            ItemStack.NETWORK_TYPE, WindowItemsPacket::carriedItem,
            WindowItemsPacket::new);

    public WindowItemsPacket {
        items = List.copyOf(items);
    }

    @Override
    public Collection<Component> components() {
        final ArrayList<Component> components = new ArrayList<>();
        for (ItemStack itemStack : items) {
            components.addAll(ItemStack.textComponents(itemStack));
        }
        components.addAll(ItemStack.textComponents(carriedItem));
        return List.copyOf(components);
    }

    @Override
    public ServerPacket copyWithOperator(UnaryOperator<Component> operator) {
        return new WindowItemsPacket(
                this.windowId,
                this.stateId,
                this.items.stream().map(stack -> ItemStack.copyWithOperator(stack, operator)).toList(),
                ItemStack.copyWithOperator(this.carriedItem, operator)
        );
    }
}
