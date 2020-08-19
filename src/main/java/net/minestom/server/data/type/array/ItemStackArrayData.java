package net.minestom.server.data.type.array;

import net.minestom.server.data.DataType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class ItemStackArrayData extends DataType<ItemStack[]> {
    @Override
    public void encode(BinaryWriter binaryWriter, ItemStack[] value) {
        binaryWriter.writeVarInt(value.length);
        for (ItemStack itemStack : value) {
            binaryWriter.writeItemStack(itemStack);
        }
    }

    @Override
    public ItemStack[] decode(BinaryReader binaryReader) {
        ItemStack[] items = new ItemStack[binaryReader.readVarInt()];
        for (int i = 0; i < items.length; i++) {
            items[i] = binaryReader.readSlot();
        }
        return items;
    }
}
