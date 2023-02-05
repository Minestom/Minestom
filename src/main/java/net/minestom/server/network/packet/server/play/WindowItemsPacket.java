package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;
import static net.minestom.server.network.NetworkBuffer.*;

public record WindowItemsPacket(byte windowId, int stateId, @NotNull List<ItemStack> items,
                                @NotNull ItemStack carriedItem) implements ComponentHoldingServerPacket {
    public WindowItemsPacket {
        items = List.copyOf(items);
    }

    public WindowItemsPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(BYTE), reader.read(VAR_INT), reader.readCollection(ITEM),
                reader.read(ITEM));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(BYTE, windowId);
        writer.write(VAR_INT, stateId);
        writer.writeCollection(ITEM, items);
        writer.write(ITEM, carriedItem);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WINDOW_ITEMS;
    }

    @Override
    public @NotNull Collection<Component> components() {
        final var list = new ArrayList<>(this.items);
        list.add(this.carriedItem);

        final var components = new ArrayList<Component>();

        list.forEach(itemStack -> {
            components.addAll(itemStack.getLore());

            final var displayName = itemStack.getDisplayName();
            if (displayName == null) return;

            components.add(displayName);
        });

        return components;
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        return new WindowItemsPacket(
                this.windowId,
                this.stateId,
                this.items.stream().map(stack -> stack.withDisplayName(operator).withLore(lines -> {
                    final var translatedComponents = new ArrayList<Component>();
                    lines.forEach(component -> translatedComponents.add(operator.apply(component)));
                    return translatedComponents;
                })).toList(),
                this.carriedItem.withDisplayName(operator).withLore(lines -> {
                    final var translatedComponents = new ArrayList<Component>();
                    lines.forEach(component -> translatedComponents.add(operator.apply(component)));
                    return translatedComponents;
                })
        );
    }
}
