package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientWindowConfirmationPacket extends ClientPlayPacket {

    public byte windowId;
    public short actionNumber;
    public boolean accepted;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.windowId = reader.readByte();
        this.actionNumber = reader.readShort();
        this.accepted = reader.readBoolean();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeShort(actionNumber);
        writer.writeBoolean(accepted);
    }
}
