package net.minestom.server.event.server;

import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.Event;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.ping.ResponseData;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link PlayerConnection} sends a status packet,
 * usually to display information on the server list.
 */
public class ServerListPingEvent extends Event implements CancellableEvent {
    private boolean cancelled = false;

    private final ResponseData responseData;
    private final PlayerConnection connection;


    public ServerListPingEvent(ResponseData responseData, PlayerConnection connection) {
        this.responseData = responseData;
        this.connection = connection;
    }

    /**
     * ResponseData being returned.
     *
     * @return the response data being returned
     */
    public @NotNull ResponseData getResponseData() {
        return responseData;
    }

    /**
     * PlayerConnection of received packet.
     *
     * Note that the player has not joined the server at this time.
     *
     * @return the playerConnection.
     */
    public @NotNull PlayerConnection getConnection() {
        return connection;
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
