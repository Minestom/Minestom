package net.minestom.server.event.server;

import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.ping.ServerListPingType;
import net.minestom.server.ping.Status;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Called when a {@link PlayerConnection} sends a status packet,
 * usually to display information on the server list.
 */
public class ServerListPingEvent implements CancellableEvent {
    private final PlayerConnection connection;
    private final ServerListPingType type;

    private boolean cancelled;
    private Status status;

    /**
     * Creates a new server list ping event with no player connection.
     *
     * @param type the ping type to respond with
     */
    public ServerListPingEvent(ServerListPingType type) {
        this(null, type);
    }

    /**
     * Creates a new server list ping event.
     *
     * @param connection the player connection, if the ping type is modern
     * @param type       the ping type to respond with
     */
    public ServerListPingEvent(@Nullable PlayerConnection connection, ServerListPingType type) {
        this.status = Status.builder().build();
        this.connection = connection;
        this.type = type;
    }

    /**
     * Gets the response data that is sent to the client.
     * This is mutable and can be modified to change what is returned.
     *
     * @return the response data being returned
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the response data, overwriting the exiting data.
     *
     * @param status the new data
     */
    public void setStatus(Status status) {
        this.status = Objects.requireNonNull(status);
    }

    /**
     * PlayerConnection of received packet. Note that the player has not joined the server
     * at this time. This will <b>only</b> be non-null for modern server list pings.
     *
     * @return the playerConnection.
     */
    public @Nullable PlayerConnection getConnection() {
        return connection;
    }

    /**
     * Gets the ping type that the client is pinging with.
     *
     * @return the ping type
     */
    public ServerListPingType getPingType() {
        return type;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Cancelling this event will cause the server to appear offline in the vanilla server list.
     * Note that this will have no effect if the ping version is {@link ServerListPingType#OPEN_TO_LAN}.
     *
     * @param cancel true if the event should be cancelled, false otherwise
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
