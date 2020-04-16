package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientCreativeInventoryActionPacket extends ClientPlayPacket {

    public short slot;
    public ItemStack item;

    @Override
    public void read(PacketReader reader) {
        this.slot = reader.readShort();
        this.item = reader.readSlot();
    }
}
