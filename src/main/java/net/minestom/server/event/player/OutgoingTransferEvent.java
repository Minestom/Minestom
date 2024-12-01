package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} is about to be redirected to another server.
 */
public class OutgoingTransferEvent implements PlayerEvent, CancellableEvent {
    private final @NotNull Player player;
    private @NotNull String host;
    private int port;
    private boolean cancelled;

    public OutgoingTransferEvent(@NotNull Player player, @NotNull String host, int port) {
        this.player = player;
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * Returns the player who is being transferred.
     * @return The player who is being transferred.
     */
    @Override
    public @NotNull Player getPlayer() {
        return this.player;
    }

    /**
     * Returns the address of the server that the player is being transferred to.
     * @return The address of the server that the player is being transferred to.
     */
    public @NotNull String getHost() {
        return this.host;
    }

    /**
     * Returns the port of the server that the player is being transferred to.
     * @return The port of the server that the player is being transferred to.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Changes the address of the server that the player will be transferred to.
     * @param host The address of the new server that the player will be transferred to
     */
    public void setHost(@NotNull String host) {
        this.host = host;
    }

    /**
     * Changes the port of the server that the player will be transferred to.
     * @param port The port of the new server that the player will be transferred to
     */
    public void setPort(int port) {
        this.port = port;
    }
}
