package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class WindowItemsPacket implements ServerPacket {

    public int windowId;
    public short count;
    public ItemStack[] items;

    // TODO slot data (Array of Slot)

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, windowId);
        buffer.putShort(count);

        if (items == null)
            return;
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            Utils.writeItemStack(buffer, item);
        }
    }

    @Override
    public int getId() {
        return 0x14;
    }
}
