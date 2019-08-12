package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class WindowItemsPacket implements ServerPacket {

    public byte windowId;
    public short count;
    public ItemStack[] items;

    // TODO slot data (Array of Slot)

    @Override
    public void write(Buffer buffer) {
        buffer.putByte(windowId);
        buffer.putShort(count);

        if (items == null)
            return;
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null) {
                buffer.putBoolean(false);
            } else {
                buffer.putBoolean(true);
                Utils.writeVarInt(buffer, item.getItemId());
                buffer.putByte(item.getCount());
                buffer.putByte((byte) 0); // End nbt TODO
            }
        }
    }

    @Override
    public int getId() {
        return 0x14;
    }
}
