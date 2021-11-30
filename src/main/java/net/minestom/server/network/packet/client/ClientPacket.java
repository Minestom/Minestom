package net.minestom.server.network.packet.client;

import net.minestom.server.utils.binary.Writeable;

/**
 * Represents a packet received from a client.
 * <p>
 * Packets are value-based, and should therefore be reliant on identity.
 */
public interface ClientPacket extends Writeable {
}
