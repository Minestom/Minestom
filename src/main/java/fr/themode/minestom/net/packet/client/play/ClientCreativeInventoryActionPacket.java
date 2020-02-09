package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientCreativeInventoryActionPacket extends ClientPlayPacket {

    public short slot;
    public ItemStack item;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readShort(value -> slot = value);
        reader.readSlot(itemStack -> {
            item = itemStack;
            callback.run();
        });
    }
}
