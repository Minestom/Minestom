package net.minestom.server.network.packet.server;

import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a packet that can be sent to a {@link PlayerConnection}.
 */
@ApiStatus.Experimental
public sealed interface SendablePacket
        permits ServerPacket, CachedPacket, FramedPacket {
}
