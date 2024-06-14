package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record ResourcePackPopPacket(@Nullable UUID id) implements ServerPacket.Configuration, ServerPacket.Play {
    public ResourcePackPopPacket(@NotNull NetworkBuffer reader) {
        this(reader.readOptional(NetworkBuffer.UUID));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeOptional(NetworkBuffer.UUID, id);
    }

    @Override
    public int configurationId() {
        return ServerPacketIdentifier.CONFIGURATION_RESOURCE_PACK_POP_PACKET;
    }

    @Override
    public int playId() {
        return ServerPacketIdentifier.RESOURCE_PACK_POP;
    }
}
