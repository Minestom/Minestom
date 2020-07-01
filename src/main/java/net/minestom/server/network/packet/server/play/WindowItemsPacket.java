package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class WindowItemsPacket implements ServerPacket {

    public byte windowId;
    public ItemStack[] items;

    @Override
    public void write(PacketWriter writer) {
        writer.writeByte(windowId);

        if (items == null) {
            writer.writeShort((short) 0);
            return;
        }

        writer.writeShort((short) items.length);
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            writer.writeItemStack(item);
        }
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.WINDOW_ITEMS;
    }
}
