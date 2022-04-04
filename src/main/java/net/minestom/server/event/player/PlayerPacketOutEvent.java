package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Listen to outgoing packets asynchronously.
 * <p>
 * Currently, do not support viewable packets.
 */
@ApiStatus.Experimental
public class PlayerPacketOutEvent implements PlayerEvent, CancellableEvent {

    private final Player player;
    private boolean updated = false;
    private ServerPacket packet;
    private boolean cancelled = false;

    public PlayerPacketOutEvent(Player player, ServerPacket packet) {
        this.player = player;
        this.packet = packet;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    /**
     * @return The current packet.
     */
    public @NotNull ServerPacket getPacket() {
        return packet;
    }

    /**
     * Sets a new packet that should be sent instead.
     *
     * @param packet the new packet.
     */
    public void setPacket(@NotNull ServerPacket packet) {
        this.packet = packet;
        this.updated = true;
    }

    /**
     * Indicates if the event has been cancelled.
     *
     * @return true if the event has been cancelled.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Cancels the event.
     *
     * @param cancel true if the event should be cancelled, false otherwise.
     */
    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    /**
     * Indicates if the packet has been updated.
     *
     * @return true if the packet was updated.
     */
    public boolean isUpdated() {
        return updated;
    }
}
