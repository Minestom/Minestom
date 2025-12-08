package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;

import java.util.Objects;

/**
 * Called when a {@link Player} is about to be redirected to another server.
 */
public class OutgoingTransferEvent implements PlayerEvent, CancellableEvent {
    private final Player player;
    private String host;
    private int port;
    private boolean cancelled;

    public OutgoingTransferEvent(Player player, String host, int port) {
        this.player = Objects.requireNonNull(player);
        this.host = Objects.requireNonNull(host);
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

    @Override
    public Player getPlayer() {
        return this.player;
    }

    /**
     * Returns the address of the target server that the player will be transferred to.
     * @return The address of the target server that the player will be transferred to.
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Returns the port of the target server that the player will be transferred to.
     * @return The port of the target server that the player will be transferred to.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Changes the address of the target server that the player will be transferred to.
     * @param host The address of the target server that the player will be transferred to
     */
    public void setHost(String host) {
        this.host = Objects.requireNonNull(host);
    }

    /**
     * Changes the port of the target server that the player will be transferred to.
     * @param port The port of the target server that the player will be transferred to
     */
    public void setPort(int port) {
        this.port = port;
    }
}
