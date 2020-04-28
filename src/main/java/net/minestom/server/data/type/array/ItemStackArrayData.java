package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;

import java.util.ArrayList;
import java.util.List;

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
        List<ItemStack> items = new ArrayList<>();
        int size = packetReader.readVarInt();
        for (int i = 0; i < size; i++) {
            items.add(packetReader.readSlot());
        }
        ItemStack[] array = items.toArray(new ItemStack[items.size()]);
        return array;
    }
}
