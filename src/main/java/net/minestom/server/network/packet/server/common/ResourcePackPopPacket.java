package net.minestom.server.network.packet.server.common;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record ResourcePackPopPacket(@Nullable UUID id) implements ServerPacket {

    public ResourcePackPopPacket(@NotNull NetworkBuffer reader) {
        this(reader.readOptional(NetworkBuffer.UUID));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeOptional(NetworkBuffer.UUID, id);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case CONFIGURATION -> ServerPacketIdentifier.CONFIGURATION_RESOURCE_PACK_POP_PACKET;
            case PLAY -> ServerPacketIdentifier.RESOURCE_PACK_POP;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.CONFIGURATION, ConnectionState.PLAY);
        };
    }
}
