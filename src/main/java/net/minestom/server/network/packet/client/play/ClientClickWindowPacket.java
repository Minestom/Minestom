package net.minestom.server.network.packet.client.play;

import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientClickWindowPacket extends ClientPlayPacket {

    public byte windowId;
    public short slot;
    public byte button;
    public short actionNumber;
    public int mode;
    public ItemStack item = ItemStack.getAirItem();

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.windowId = reader.readByte();
        this.slot = reader.readShort();
        this.button = reader.readByte();
        this.actionNumber = reader.readShort();
        this.mode = reader.readVarInt();
        this.item = reader.readItemStack();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeShort(slot);
        writer.writeByte(button);
        writer.writeShort(actionNumber);
        writer.writeVarInt(mode);
        writer.writeItemStack(item);
    }
}
