package net.minestom.server.network.packet.server.play;

import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minestom.server.network.NetworkBuffer.*;

public record ServerDataPacket(@NotNull Component motd, byte @Nullable [] iconBase64,
                               boolean enforcesSecureChat) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<ServerDataPacket> SERIALIZER = NetworkBufferTemplate.template(
            COMPONENT, ServerDataPacket::motd,
            BYTE_ARRAY.optional(), ServerDataPacket::iconBase64,
            BOOLEAN, ServerDataPacket::enforcesSecureChat,
            ServerDataPacket::new);
}
