package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public record PlayerInfoRemovePacket(@NotNull List<@NotNull UUID> uuids) implements ServerPacket {
    public PlayerInfoRemovePacket(@NotNull UUID uuid) {
        this(List.of(uuid));
    }

    public PlayerInfoRemovePacket {
        uuids = List.copyOf(uuids);
    }

    public PlayerInfoRemovePacket(@NotNull NetworkBuffer reader) {
        this(reader.readCollection(NetworkBuffer.UUID));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.writeCollection(NetworkBuffer.UUID, uuids);
    }

    @Override
    public int getId(@NotNull ConnectionState state) {
        return switch (state) {
            case PLAY -> ServerPacketIdentifier.PLAYER_INFO_REMOVE;
            default -> PacketUtils.invalidPacketState(getClass(), state, ConnectionState.PLAY);
        };
    }
}
