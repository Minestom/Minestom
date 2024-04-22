package net.minestom.server.network.packet.client.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.ClientPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.RAW_BYTES;
import static net.minestom.server.network.NetworkBuffer.STRING;

public record ClientPluginMessagePacket(@NotNull String channel, byte[] data) implements ClientPacket {
    public ClientPluginMessagePacket {
        if (channel.length() > 256)
            throw new IllegalArgumentException("Channel cannot be more than 256 characters long");
    }

    public ClientPluginMessagePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(STRING), reader.read(RAW_BYTES));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(STRING, channel);
        writer.write(RAW_BYTES, data);
    }
}
