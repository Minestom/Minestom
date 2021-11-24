package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientKeepAlivePacket(long id) implements ClientPacket {
    public ClientKeepAlivePacket(BinaryReader reader) {
        this(reader.readLong());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeLong(id);
    }
}
