package net.minestom.server.network.packet.server;

import net.minestom.server.network.ConnectionState;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a packet that can be sent to a {@link net.minestom.server.network.player.PlayerConnection}.
 */
public sealed interface SendablePacket
        permits BufferedPacket, CachedPacket, FramedPacket, ServerPacket {

    static @Nullable ServerPacket extractServerPacket(ConnectionState state, SendablePacket packet) {
        return switch (packet) {
            case ServerPacket serverPacket -> serverPacket;
            case CachedPacket cachedPacket -> cachedPacket.packet(state);
            case FramedPacket framedPacket -> framedPacket.packet();
            case BufferedPacket bufferedPacket -> null;
        };
    }
}
