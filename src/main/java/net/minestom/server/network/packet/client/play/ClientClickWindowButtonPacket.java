package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientClickWindowButtonPacket extends ClientPlayPacket {

    public byte windowId;
    public byte buttonId;

    @Override
    public void read(@NotNull BinaryBuffer reader) {
        this.windowId = reader.readByte();
        this.buttonId = reader.readByte();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
        writer.writeByte(buttonId);
    }
}
