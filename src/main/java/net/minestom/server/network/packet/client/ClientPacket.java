package net.minestom.server.network.packet.client;

import net.minestom.server.network.packet.Packet;

/**
 * Represents a packet received from a client.
 * <p>
 * Packets are value-based, and should therefore not be reliant on identity.
 */
public non-sealed interface ClientPacket extends Packet {
}
