package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class SetSlotPacket implements ServerPacket {

    public byte windowId;
    public short slot;
    public ItemStack itemStack;

    @Override
    public void write(Buffer buffer) {
        buffer.putByte(windowId);
        buffer.putShort(slot);
        Utils.writeItemStack(buffer, itemStack);
    }

    @Override
    public int getId() {
        return 0x16;
    }
}
