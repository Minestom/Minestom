package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.LONG;

public record ClientKeepAlivePacket(long id) implements ClientPacket {
    public ClientKeepAlivePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(LONG));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeLong(id);
    }
}
