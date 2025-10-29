package net.minestom.server.network.packet.server;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.PacketParser;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a packet that can be sent to a {@link PlayerConnection}.
 */
public sealed interface SendablePacket
        permits BufferedPacket, CachedPacket, FramedPacket, LazyPacket, ServerPacket {

    static @Nullable ServerPacket extractServerPacket(SendablePacket packet, ConnectionState state, @Nullable PacketParser<ServerPacket> writer) {
        return switch (packet) {
            case ServerPacket serverPacket -> serverPacket;
            case CachedPacket cachedPacket -> cachedPacket.packet(state, writer);
            case FramedPacket framedPacket -> framedPacket.packet();
            case LazyPacket lazyPacket -> lazyPacket.packet();
            case BufferedPacket _ -> null;
        };
    }
}
