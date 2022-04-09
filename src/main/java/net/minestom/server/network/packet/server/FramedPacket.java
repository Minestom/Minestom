package net.minestom.server.network.packet.server;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Represents a packet which is already framed. (packet id+payload) + optional compression
 * Can be used if you want to send the exact same buffer to multiple clients without processing it more than once.
 * <p>
 * The {@link ByteBuffer} will ultimately become a MemorySegment once out of incubation.
 */
@ApiStatus.Internal
public record FramedPacket(@NotNull ServerPacket packet,
                           @NotNull ByteBuffer body) implements SendablePacket {
    public FramedPacket {
        body = body.position(0).asReadOnlyBuffer();
    }
}
