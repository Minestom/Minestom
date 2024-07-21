package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

public record CookieRequestPacket(@NotNull String key) implements
        ServerPacket.Login, ServerPacket.Configuration, ServerPacket.Play {

    public CookieRequestPacket(@NotNull NetworkBuffer reader) {
        this(reader.read(NetworkBuffer.STRING));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.STRING, key);
    }

}
