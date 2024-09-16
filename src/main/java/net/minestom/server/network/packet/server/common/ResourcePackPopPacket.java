package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record ResourcePackPopPacket(@Nullable UUID id) implements ServerPacket.Configuration, ServerPacket.Play {
    public static final NetworkBuffer.Type<ResourcePackPopPacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.UUID.optional(), ResourcePackPopPacket::id,
            ResourcePackPopPacket::new);
}
