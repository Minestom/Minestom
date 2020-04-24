package net.minestom.server.network.packet.client.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientCreativeInventoryActionPacket extends ClientPlayPacket {

    public short slot;
    public ItemStack item;

    @Override
    public void read(PacketReader reader) {
        this.slot = reader.readShort();
        this.item = reader.readSlot();
    }
}
