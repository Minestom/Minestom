package net.minestom.server.network.packet.server;

import net.minestom.server.adventure.ComponentHolder;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a packet which can be sent to a player using {@link PlayerConnection#sendPacket(SendablePacket)}.
 * <p>
 * Packets are value-based, and should therefore not be reliant on identity.
 */
public sealed interface ServerPacket extends NetworkBuffer.Writer, SendablePacket permits
        ServerPacket.Configuration, ServerPacket.Status, ServerPacket.Login, ServerPacket.Play {

    /**
     * Gets the id of this packet.
     * <p>
     * Written in the final buffer header so it needs to match the client id.
     *
     * @return the id of this packet
     */
    default int getId(@NotNull ConnectionState state) {
        final int id = switch (state) {
            case HANDSHAKE -> -1;
            case CONFIGURATION -> this instanceof Configuration configuration ? configuration.configurationId() : -1;
            case STATUS -> this instanceof Status status ? status.statusId() : -1;
            case LOGIN -> this instanceof Login login ? login.loginId() : -1;
            case PLAY -> this instanceof Play play ? play.playId() : -1;
        };
        if (id != -1) return id;
        // Invalid state, generate error
        List<ConnectionState> validStates = new ArrayList<>();
        if (this instanceof Configuration) validStates.add(ConnectionState.CONFIGURATION);
        if (this instanceof Status) validStates.add(ConnectionState.STATUS);
        if (this instanceof Login) validStates.add(ConnectionState.LOGIN);
        if (this instanceof Play) validStates.add(ConnectionState.PLAY);
        return PacketUtils.invalidPacketState(getClass(), state, validStates.toArray(ConnectionState[]::new));
    }

    non-sealed interface Configuration extends ServerPacket {
        int configurationId();
    }

    non-sealed interface Status extends ServerPacket {
        int statusId();
    }

    non-sealed interface Login extends ServerPacket {
        int loginId();
    }

    non-sealed interface Play extends ServerPacket {
        int playId();
    }

    interface ComponentHolding extends ComponentHolder<ServerPacket> {
    }
}
