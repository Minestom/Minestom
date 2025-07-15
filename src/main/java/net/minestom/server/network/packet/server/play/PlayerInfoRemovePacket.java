package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.List;
import java.util.UUID;

public record PlayerInfoRemovePacket(List<UUID> uuids) implements ServerPacket.Play {
    public static final int MAX_ENTRIES = 1024;

    public static final NetworkBuffer.Type<PlayerInfoRemovePacket> SERIALIZER = NetworkBufferTemplate.template(
            NetworkBuffer.UUID.list(MAX_ENTRIES), PlayerInfoRemovePacket::uuids,
            PlayerInfoRemovePacket::new);

    public PlayerInfoRemovePacket(UUID uuid) {
        this(List.of(uuid));
    }

    public PlayerInfoRemovePacket {
        uuids = List.copyOf(uuids);
    }
}
