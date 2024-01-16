package net.minestom.server.network.packet.server;

import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a packet which can be sent to a player using {@link PlayerConnection#sendPacket(SendablePacket)}.
 * <p>
 * Packets are value-based, and should therefore not be reliant on identity.
 */
public non-sealed interface ServerPacket extends NetworkBuffer.Writer, SendablePacket {

    /**
     * Gets the id of this packet.
     * <p>
     * Written in the final buffer header so it needs to match the client id.
     *
     * @return the id of this packet
     */
    int getId(@NotNull ConnectionState state);

}
