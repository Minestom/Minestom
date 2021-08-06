package net.minestom.server.data.type;

import net.minestom.server.data.DataType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ItemStackData extends DataType<ItemStack> {
    @Override
    public void encode(@NotNull BinaryWriter writer, @NotNull ItemStack value) {
        writer.writeItemStack(value);
    }

    @NotNull
    @Override
    public ItemStack decode(@NotNull BinaryBuffer reader) {
        return reader.readItemStack();
    }
}
