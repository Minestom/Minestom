package net.minestom.server.network.packet.client.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientCreativeInventoryActionPacket extends ClientPlayPacket {

    public short slot;
    public ItemStack item;

    @Override
    public void read(BinaryReader reader) {
        this.slot = reader.readShort();
        this.item = reader.readSlot();
    }
}
