package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

public class ItemStackArrayData extends DataType<ItemStack[]> {
    @Override
    public void encode(PacketWriter packetWriter, ItemStack[] value) {
        packetWriter.writeVarInt(value.length);
        for (ItemStack itemStack : value) {
            packetWriter.writeItemStack(itemStack);
        }
    }

    @Override
    public ItemStack[] decode(PacketReader packetReader) {
        ItemStack[] items = new ItemStack[packetReader.readVarInt()];
        for (int i = 0; i < items.length; i++) {
            items[i] = packetReader.readSlot();
        }
        return items;
    }
}
