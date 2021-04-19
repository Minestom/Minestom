package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientCloseWindowPacket extends ClientPlayPacket {

    public byte windowId;

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.windowId = reader.readByte();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(windowId);
    }
}
