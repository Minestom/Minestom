package net.minestom.server.event.server;

import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.MutableEvent;
import net.minestom.server.event.trait.mutation.EventMutator;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.ping.ServerListPingType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Called when a {@link PlayerConnection} sends a status packet,
 * usually to display information on the server list.
 */
public record ServerListPingEvent(@Nullable PlayerConnection connection, @NotNull ServerListPingType type, @NotNull ResponseData responseData, boolean cancelled) implements CancellableEvent<ServerListPingEvent> {

    /**
     * Creates a new server list ping event with no player connection.
     *
     * @param type the ping type to respond with
     */
    public ServerListPingEvent(@NotNull ServerListPingType type) {
        this(null, type);
    }

    /**
     * Creates a new server list ping event.
     *
     * @param connection the player connection, if the ping type is modern
     * @param type       the ping type to respond with
     */
    public ServerListPingEvent(@Nullable PlayerConnection connection, @NotNull ServerListPingType type) {
        this(connection, type, new ResponseData(), false);
    }

    /**
     * Gets the response data that is sent to the client.
     * This is mutable and can be modified to change what is returned.
     *
     * @return the response data being returned
     */
    public @NotNull ResponseData responseData() {
        return responseData;
    }

    /**
     * PlayerConnection of received packet. Note that the player has not joined the server
     * at this time. This will <b>only</b> be non-null for modern server list pings.
     *
     * @return the playerConnection.
     */
    public @Nullable PlayerConnection connection() {
        return connection;
    }

    /**
     * Gets the ping type that the client is pinging with.
     *
     * @return the ping type
     */
    public @NotNull ServerListPingType type() {
        return type;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutatorCancellable<ServerListPingEvent> {
        private final PlayerConnection connection;
        private final ServerListPingType type;

        private ResponseData responseData;
        private boolean cancelled;

        public Mutator(ServerListPingEvent event) {
            this.connection = event.connection;
            this.type = event.type;
            this.responseData = event.responseData;
            this.cancelled = event.cancelled;
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

        /**
         * Gets the response data that is sent to the client.
         * This is mutable and can be modified to change what is returned.
         *
         * @return the response data being returned
         */
        public @NotNull ResponseData getResponseData() {
            return responseData;
        }

        /**
         * Sets the response data, overwriting the exiting data.
         *
         * @param responseData the new data
         */
        public void setResponseData(@NotNull ResponseData responseData) {
            this.responseData = Objects.requireNonNull(responseData);
        }

        @Contract(pure = true)
        @Override
        public @NotNull ServerListPingEvent mutated() {
            return new ServerListPingEvent(this.connection, this.type, this.responseData, this.cancelled);
        }
    }
}
