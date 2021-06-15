package net.minestom.server.event.server;

import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;
import org.jetbrains.annotations.NotNull;


/**
 * Called when a {@link PlayerConnection} sends a ping packet,
 * usually after the status packet. Only used in versions since the netty rewrite; 1.7+
 *
 * @see ServerListPingEvent
 */
public class ClientPingServerEvent implements CancellableEvent {
    private static final UpdateOption DEFAULT_DELAY = new UpdateOption(0, TimeUnit.MILLISECOND);

    private final PlayerConnection connection;
    private long payload;

    private boolean cancelled = false;
    private UpdateOption delay;

    /**
     * Creates a new client ping server event with 0 delay
     *
     * @param connection the player connection
     * @param payload    the payload the client sent
     */
    public ClientPingServerEvent(@NotNull PlayerConnection connection, long payload) {
        this.connection = connection;
        this.payload = payload;
        this.delay = DEFAULT_DELAY;
    }

    /**
     * Creates a new client ping server event with 0 delay
     *
     * @param connection the player connection
     * @param payload    the payload the client sent
     */
    public ClientPingServerEvent(@NotNull PlayerConnection connection, long payload, UpdateOption delay) {
        this.connection = connection;
        this.payload = payload;
        this.delay = delay;
    }


    /**
     * PlayerConnection of received packet. Note that the player has not joined the server
     * at this time.
     *
     * @return the connection.
     */
    public @NotNull PlayerConnection getConnection() {
        return connection;
    }

    /**
     * Payload of received packet. May be any number; vanilla uses a system dependant time value.
     *
     * @return the payload
     */
    public long getPayload() {
        return payload;
    }

    /**
     * Sets the payload to respond with.
     * <p>
     * Note: This should be the same as the client sent, however vanilla 1.17 seems to be OK with a different payload.
     *
     * @param payload the payload
     */
    public void setPayload(long payload) {
        this.payload = payload;
    }

    /**
     * Gets the delay until minestom will send the ping response packet.
     *
     * @return the delay
     */
    public @NotNull UpdateOption getDelay() {
        return delay;
    }

    /**
     * Adds to the delay until minestom will send the ping response packet.
     *
     * @param delay the delay
     */
    public void addDelay(@NotNull UpdateOption delay) {
        this.delay = new UpdateOption(this.delay.toMilliseconds() + delay.toMilliseconds(), TimeUnit.MILLISECOND);
    }

    /**
     * Sets the delay until minestom will send the ping response packet.
     *
     * @param delay the delay
     */
    public void setDelay(@NotNull UpdateOption delay) {
        this.delay = delay;
    }

    /**
     * Clears the delay until minestom will send the ping response packet.
     */
    public void noDelay() {
        this.delay = DEFAULT_DELAY;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Cancelling this event will cause the server to appear offline in the vanilla server list.
     *
     * @param cancel true if the event should be cancelled, false otherwise
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
