package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record WindowItemsPacket(byte windowId, int stateId, ItemStack[] items,
                                ItemStack carriedItem) implements ServerPacket {
    public WindowItemsPacket(BinaryReader reader) {
        this(reader.readByte(), reader.readVarInt(), reader.readItemStackArray(), ItemStack.AIR);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeVarInt(stateId);

        if (items == null) {
            writer.writeVarInt(0);
        } else {
            writer.writeVarInt(items.length);
            for (ItemStack item : items) {
                writer.writeItemStack(item);
            }
        }
        writer.writeItemStack(carriedItem);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WINDOW_ITEMS;
    }
}
