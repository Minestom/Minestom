package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientKeepAlivePacket extends ClientPlayPacket {

    public long id;

    @Override
    public void read(@NotNull BinaryBuffer reader) {
        this.id = reader.readLong();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeLong(id);
    }
}
