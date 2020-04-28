package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class ItemStackData extends DataType<ItemStack> {
    @Override
    public void encode(PacketWriter packetWriter, ItemStack value) {
        packetWriter.writeItemStack(value);
    }

    @Override
    public ItemStack decode(PacketReader packetReader, byte[] value) {
        return packetReader.readSlot();
    }
}
