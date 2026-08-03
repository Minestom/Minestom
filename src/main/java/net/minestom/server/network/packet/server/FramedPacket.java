package net.minestom.server.network.packet.server;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a packet which is already framed. (packet id+payload) + optional compression
 * Can be used if you want to send the exact same buffer to multiple clients without processing it more than once.
 */
@ApiStatus.Internal
public record FramedPacket(ServerPacket packet, //TODO(valhalla) requires changing from SoftReference
                           NetworkBuffer body) implements SendablePacket {
    public FramedPacket {
        body = body.readOnly().readIndex(0);
    }
}
