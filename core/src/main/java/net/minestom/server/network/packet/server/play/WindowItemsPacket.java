package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class WindowItemsPacket implements ServerPacket {

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
}
