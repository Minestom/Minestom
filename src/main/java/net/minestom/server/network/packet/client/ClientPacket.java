package net.minestom.server.network.packet.client;

import net.minestom.server.network.packet.Packet;

/**
 * Represents a packet received from a client.
 * <p>
 * Packets are value-based, and should therefore not be reliant on identity.
 */
public sealed interface ClientPacket extends Packet {
    non-sealed interface Handshake extends ClientPacket {
    }

    non-sealed interface Status extends ClientPacket {
    }

    non-sealed interface Login extends ClientPacket {
    }

    non-sealed interface Configuration extends ClientPacket {
    }

    non-sealed interface Play extends ClientPacket {
    }
}