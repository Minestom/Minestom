package net.minestom.server.event;

public class CancellableEvent extends Event {

    private boolean cancelled;

    /**
     * @return true if the event should be cancelled, false otherwise
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * @param cancel true if the event should be cancelled, false otherwise
     */
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

}
