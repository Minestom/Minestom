package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.STRING;

public record CookieRequestPacket(@NotNull String key) implements
        ServerPacket.Login, ServerPacket.Configuration, ServerPacket.Play {
    public static final NetworkBuffer.Type<CookieRequestPacket> SERIALIZER = NetworkBufferTemplate.template(
            STRING, CookieRequestPacket::key,
            CookieRequestPacket::new);
}
