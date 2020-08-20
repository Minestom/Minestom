package net.minestom.server.network.packet.server.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class SetSlotPacket implements ServerPacket {

    public byte windowId;
    public short slot;
    public ItemStack itemStack;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeShort(slot);
        writer.writeItemStack(itemStack);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_SLOT;
    }
}
