package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientClickWindowPacket extends ClientPlayPacket {

    public byte windowId;
    public short slot;
    public byte button;
    public short actionNumber;
    public int mode;
    public ItemStack item;

    @Override
    public void read(PacketReader reader) {
        this.windowId = reader.readByte();
        this.slot = reader.readShort();
        this.button = reader.readByte();
        this.actionNumber = reader.readShort();
        this.mode = reader.readVarInt();
        this.item = reader.readSlot();
    }
}
