package net.minestom.server.event.server;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.Event;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.ping.ResponseDataConsumer;
import net.minestom.server.ping.ServerListPingVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Called when a {@link PlayerConnection} sends a status packet,
 * usually to display information on the server list.
 */
public class ServerListPingEvent extends Event implements CancellableEvent {
    private final PlayerConnection connection;
    private final ServerListPingVersion version;

    private boolean cancelled = false;
    private ResponseData responseData;

    /**
     * Creates a new server list ping event with no player connection.
     *
     * @param version the ping version to respond with
     */
    public ServerListPingEvent(@NotNull ServerListPingVersion version) {
        this(null, version);
    }

    /**
     * Creates a new server list ping event.
     *
     * @param connection the player connection, if the ping version is modern
     * @param version the ping version to respond with
     */
    public ServerListPingEvent(@Nullable PlayerConnection connection, @NotNull ServerListPingVersion version) {
        //noinspection deprecation we need to continue doing this until the consumer is removed
        ResponseDataConsumer consumer = MinecraftServer.getResponseDataConsumer();
        this.responseData = new ResponseData();

        if (consumer != null) {
            consumer.accept(connection, responseData);
        }

        this.connection = connection;
        this.version = version;
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

    /**
     * PlayerConnection of received packet.
     * Note that the player has not joined the server at this time.
     * This will be null for legacy server list pings.
     *
     * @return the playerConnection.
     */
    public @Nullable PlayerConnection getConnection() {
        return connection;
    }

    /**
     * Gets the ping version that the client is pinging with.
     *
     * @return the ping version
     * @see ServerListPingVersion
     */
    public @NotNull ServerListPingVersion getPingVersion() {
       return version;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Cancelling this event will cause you server to appear offline in the vanilla server list.
     *
     * @param cancel true if the event should be cancelled, false otherwise
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
