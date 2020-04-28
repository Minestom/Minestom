package net.minestom.server.data.type;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minestom.server.data.DataType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.Utils;

public class ItemStackData extends DataType<ItemStack> {
    @Override
    public byte[] encode(ItemStack value) {
        PacketWriter packetWriter = new PacketWriter();
        packetWriter.writeItemStack(value);
        return packetWriter.toByteArray();
    }

    @Override
    public ItemStack decode(byte[] value) {
        ByteBuf buffer = Unpooled.wrappedBuffer(value);
        ItemStack itemStack = Utils.readItemStack(new PacketReader(buffer));
        return itemStack;
    }
}
