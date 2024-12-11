package net.minestom.server.network.packet.server;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a buffer to directly write to the network.
 * <p>
 * May contain multiple packets.
 */
@ApiStatus.Internal
public record BufferedPacket(@NotNull NetworkBuffer buffer,
                             long index, long length) implements SendablePacket {
    public BufferedPacket {
        buffer.readOnly();
    }
}
