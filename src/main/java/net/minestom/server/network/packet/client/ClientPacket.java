package net.minestom.server.network.packet.client;

import net.minestom.server.network.NetworkBuffer;

/**
 * Represents a packet received from a client.
 * <p>
 * Packets are value-based, and should therefore not be reliant on identity.
 */
public interface ClientPacket extends NetworkBuffer.Writer {
}
