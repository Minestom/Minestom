package net.minestom.server.network.packet.client.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientCreativeInventoryActionPacket(short slot, @NotNull ItemStack item) implements ClientPacket {
    public ClientCreativeInventoryActionPacket(BinaryReader reader) {
        this(reader.readShort(), reader.readItemStack());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeShort(slot);
        writer.writeItemStack(item);
    }
}
