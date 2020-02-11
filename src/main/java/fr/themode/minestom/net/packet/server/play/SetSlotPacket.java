package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class SetSlotPacket implements ServerPacket {

    public byte windowId;
    public short slot;
    public ItemStack itemStack;

    @Override
    public void write(PacketWriter writer) {
        writer.writeByte(windowId);
        writer.writeShort(slot);
        writer.writeItemStack(itemStack);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_SLOT;
    }
}
