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

    /**
     * If not null, the server will switch state immediately after sending this packet
     *
     * <p>WARNING: A cached or framed packet will currently never go through writeServerPacketSync,
     * so a state change inside one of them will never actually be triggered. Currently, cached
     * packets are never used for packets that change state, so this is not a problem.</p>
     */
    default @Nullable ConnectionState nextState() {
        return null;
    }

}
