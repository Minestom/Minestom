package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record WindowItemsPacket(byte windowId, int stateId, @NotNull List<ItemStack> items,
                                @NotNull ItemStack carriedItem) implements ServerPacket.Play, ServerPacket.ComponentHolding {
    public static final int MAX_ENTRIES = 128;

    public WindowItemsPacket {
        items = List.copyOf(items);
    }

    public WindowItemsPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BYTE), reader.read(VAR_INT), reader.readCollection(ItemStack.NETWORK_TYPE, MAX_ENTRIES),
                reader.read(ItemStack.NETWORK_TYPE));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BYTE, windowId);
        writer.write(VAR_INT, stateId);
        writer.writeCollection(ItemStack.NETWORK_TYPE, items);
        writer.write(ItemStack.NETWORK_TYPE, carriedItem);
    }

    @Override
    public @NotNull Collection<Component> components() {
        final var list = new ArrayList<>(this.items);
        list.add(this.carriedItem);

        final var components = new ArrayList<Component>();

        list.forEach(itemStack -> {
            components.addAll(itemStack.get(ItemComponent.LORE, List.of()));

            final var customName = itemStack.get(ItemComponent.CUSTOM_NAME);
            if (customName != null) {
                components.add(customName);
            }

            final var itemName = itemStack.get(ItemComponent.ITEM_NAME);
            if (itemName != null) {
                components.add(itemName);
            }
        });

        return components;
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        UnaryOperator<List<Component>> loreOperator = lines -> {
            final var translatedComponents = new ArrayList<Component>();
            lines.forEach(component -> translatedComponents.add(operator.apply(component)));
            return translatedComponents;
        };
        return new WindowItemsPacket(
                this.windowId,
                this.stateId,
                this.items.stream().map(stack -> stack
                        .with(ItemComponent.ITEM_NAME, operator)
                        .with(ItemComponent.CUSTOM_NAME, operator)
                        .with(ItemComponent.LORE, loreOperator))
                        .toList(),
                this.carriedItem
                        .with(ItemComponent.ITEM_NAME, operator)
                        .with(ItemComponent.CUSTOM_NAME, operator)
                        .with(ItemComponent.LORE, loreOperator)
        );
    }
}
