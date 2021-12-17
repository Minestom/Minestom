package net.minestom.server.network.packet.server;

import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a packet that can be sent to a {@link PlayerConnection}.
 */
@ApiStatus.Experimental
public sealed interface SendablePacket
        permits ServerPacket, CachedPacket, FramedPacket {

    @ApiStatus.Experimental
    static @NotNull ServerPacket extractServerPacket(@NotNull SendablePacket packet) {
        if (packet instanceof ServerPacket serverPacket) {
            return serverPacket;
        } else if (packet instanceof CachedPacket cachedPacket) {
            return cachedPacket.packet();
        } else if (packet instanceof FramedPacket framedPacket) {
            return framedPacket.packet();
        } else {
            throw new RuntimeException("Unknown packet type: " + packet.getClass().getName());
        }
    }
}
