package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;

import java.util.Objects;

/**
 * Called when a {@link Player} is about to be redirected to another server.
 * <br>
 * It can be canceled to prevent the transfer from occurring.
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
     *
     * @return the target host, usually an IP or domain name
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Returns the port of the target server that the player will be transferred to.
     *
     * @return the target port
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Changes the address of the target server that the player will be transferred to.
     *
     * @param host the address of the target server, usually an IP or domain name
     */
    public void setHost(String host) {
        this.host = Objects.requireNonNull(host);
    }

    /**
     * Changes the port of the target server that the player will be transferred to.
     *
     * @param port the target port
     */
    public void setPort(int port) {
        this.port = port;
    }
}
