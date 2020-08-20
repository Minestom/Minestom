package net.minestom.server.network.packet.client.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;

public class ClientClickWindowPacket extends ClientPlayPacket {

    public byte windowId;
    public short slot;
    public byte button;
    public short actionNumber;
    public int mode;
    public ItemStack item;

    @Override
    public void read(BinaryReader reader) {
        this.windowId = reader.readByte();
        this.slot = reader.readShort();
        this.button = reader.readByte();
        this.actionNumber = reader.readShort();
        this.mode = reader.readVarInt();
        this.item = reader.readSlot();
    }
}
