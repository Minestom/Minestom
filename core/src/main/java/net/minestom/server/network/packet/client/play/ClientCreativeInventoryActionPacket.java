package net.minestom.server.network.packet.client.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientCreativeInventoryActionPacket extends ClientPlayPacket {

    public short slot;
    public ItemStack item = ItemStack.AIR;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.slot = reader.readShort();
        this.item = reader.readItemStack();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeShort(slot);
        writer.writeItemStack(item);
    }
}
