package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;

public class ItemStackData extends DataType<ItemStack> {
    @Override
    public void encode(BinaryWriter writer, ItemStack value) {
        writer.writeItemStack(value);
    }

    @Override
    public ItemStack decode(BinaryReader reader) {
        return reader.readSlot();
    }
}
