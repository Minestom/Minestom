package net.minestom.server.event;

/**
 * Represents an {@link Event} which can be cancelled.
 */
public class CancellableEvent extends Event {

    private boolean cancelled;

    /**
     * Gets if the {@link Event} should be cancelled or not.
     *
     * @return true if the {@link Event} should be cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Marks the {@link Event} as cancelled or not.
     *
     * @param cancel true if the {@link Event} should be cancelled, false otherwise
     */
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

}
