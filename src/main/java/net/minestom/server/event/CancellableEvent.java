package net.minestom.server.event;

/**
 * Represent an event which can be cancelled
 */
public class CancellableEvent extends Event {

    private boolean cancelled;

    /**
     * Get if the event will be cancelled or not
     *
     * @return true if the event should be cancelled, false otherwise
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Mark the event as cancelled or not
     *
     * @param cancel true if the event should be cancelled, false otherwise
     */
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

}
