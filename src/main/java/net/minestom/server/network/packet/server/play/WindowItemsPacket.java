package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class WindowItemsPacket implements ServerPacket {

    public byte windowId;
    public int stateId;
    public ItemStack[] items;
    public ItemStack carriedItem;

    public WindowItemsPacket(byte windowId, int stateId, ItemStack[] items, ItemStack carriedItem) {
        this.windowId = windowId;
        this.stateId = stateId;
        this.items = items;
        this.carriedItem = carriedItem;
    }

    public WindowItemsPacket() {
        this((byte) 0, 0, new ItemStack[]{}, ItemStack.AIR);
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
    public void read(@NotNull BinaryReader reader) {
        windowId = reader.readByte();
        stateId = reader.readVarInt();

        final int length = reader.readVarInt();
        items = new ItemStack[length];
        for (int i = 0; i < length; i++) {
            items[i] = reader.readItemStack();
        }
        carriedItem = reader.readItemStack();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WINDOW_ITEMS;
    }
}
