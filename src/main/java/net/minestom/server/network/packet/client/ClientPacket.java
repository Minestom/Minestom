package net.minestom.server.network.packet.client;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a packet received from a client.
 * <p>
 * Packets are value-based, and should therefore not be reliant on identity.
 */
public interface ClientPacket extends NetworkBuffer.Writer {
    /**
     * Whether the packet should process immediately, or wait until
     * the next server tick.
     *
     * @return true if the packet should process immediately
     */
    @ApiStatus.Internal
    default boolean shouldProcessImmediately() {
        return false;
    }
}
