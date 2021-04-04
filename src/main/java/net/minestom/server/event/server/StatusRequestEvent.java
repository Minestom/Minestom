package net.minestom.server.event.server;

import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.Event;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.ping.ResponseData;
import org.jetbrains.annotations.NotNull;


/**
 * Called by a StatusRequestPacket.
 * Can be used to modify the {@link ResponseData} response data.
 */
public class StatusRequestEvent extends Event implements CancellableEvent {

    private final ResponseData responseData;
    private final PlayerConnection connection;

    private boolean cancelled;

    public StatusRequestEvent(@NotNull ResponseData responseData, @NotNull PlayerConnection connection) {
        this.responseData = responseData;
        this.connection = connection;
    }

    /**
     * ResponseData being returned.
     *
     * @return the response data being returned
     */
    public ResponseData getResponseData() {
        return responseData;
    }

    /**
     * PlayerConnection of received packet.
     *
     * Note that the player has not joined the server at this time.
     *
     * @return the playerConnection.
     */

    public PlayerConnection getConnection() {
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
