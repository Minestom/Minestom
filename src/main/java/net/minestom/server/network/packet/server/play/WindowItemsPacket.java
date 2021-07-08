package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ComponentHoldingServerPacket;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public class WindowItemsPacket implements ComponentHoldingServerPacket {

    public byte windowId;
    public ItemStack[] items;

    /**
     * Default constructor, required for reflection operations.
     */
    public WindowItemsPacket() {}

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);

        if (items == null) {
            writer.writeShort((short) 0);
            return;
        }

        writer.writeShort((short) items.length);
        for (ItemStack item : items) {
            writer.writeItemStack(item);
        }
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        windowId = reader.readByte();

        short length = reader.readShort();
        items = new ItemStack[length];
        for (int i = 0; i < length; i++) {
            items[i] = reader.readItemStack();
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WINDOW_ITEMS;
    }

    @Override
    public @NotNull Collection<Component> components() {
        List<Component> components = new ArrayList<>();
        for (ItemStack item : items) {
            components.addAll(item.components());
        }
        return components;
    }

    @Override
    public @NotNull ServerPacket copyWithOperator(@NotNull UnaryOperator<Component> operator) {
        WindowItemsPacket packet = new WindowItemsPacket();
        packet.items = new ItemStack[this.items.length];
        for (int i = 0; i < this.items.length; i++) {
            packet.items[i] = this.items[i].copyWithOperator(operator);
        }
        return packet;
    }
}
