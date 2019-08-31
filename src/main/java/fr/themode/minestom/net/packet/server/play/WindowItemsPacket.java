package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class WindowItemsPacket implements ServerPacket {

    public int windowId;
    public short count;
    public ItemStack[] items;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(windowId);

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
        return 0x14;
    }
}
