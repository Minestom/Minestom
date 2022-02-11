package net.minestom.server.network.packet.server;

import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.binary.Writeable;

/**
 * Represents a packet which can be sent to a player using {@link PlayerConnection#sendPacket(SendablePacket)}.
 * <p>
 * Packets are value-based, and should therefore not be reliant on identity.
 */
public non-sealed interface ServerPacket extends Writeable, SendablePacket {

    /**
     * Gets the id of this packet.
     * <p>
     * Written in the final buffer header so it needs to match the client id.
     *
     * @return the id of this packet
     */
    int getId();
}
