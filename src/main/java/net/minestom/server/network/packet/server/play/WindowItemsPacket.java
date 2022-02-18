package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record WindowItemsPacket(byte windowId, int stateId, @NotNull List<ItemStack> items,
                                @NotNull ItemStack carriedItem) implements ServerPacket {
    public WindowItemsPacket {
        items = List.copyOf(items);
    }

    public WindowItemsPacket(BinaryReader reader) {
        this(reader.readByte(), reader.readVarInt(), reader.readVarIntList(BinaryReader::readItemStack),
                reader.readItemStack());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeVarInt(stateId);
        writer.writeVarIntList(items, BinaryWriter::writeItemStack);
        writer.writeItemStack(carriedItem);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WINDOW_ITEMS;
    }
}
