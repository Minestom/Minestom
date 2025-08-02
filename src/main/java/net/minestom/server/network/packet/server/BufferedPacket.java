package net.minestom.server.network.packet.server;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a buffer to directly write to the network.
 * <p>
 * May contain multiple packets.
 */
@ApiStatus.Internal
public record BufferedPacket(NetworkBuffer buffer,
                             long index, long length) implements SendablePacket {
    public BufferedPacket {
        buffer.readOnly();
    }
}
