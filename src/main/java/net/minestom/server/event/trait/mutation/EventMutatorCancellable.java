package net.minestom.server.event.trait.mutation;

import net.minestom.server.event.Event;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.MutableEvent;
import org.jetbrains.annotations.NotNull;

public interface EventMutatorCancellable<T extends MutableEvent<T>> extends EventMutator<T> {

    /**
     * Gets if the {@link Event} should be cancelled or not.
     *
     * @return true if the event should be cancelled
     */
    boolean isCancelled();

    /**
     * Marks the {@link Event} as cancelled or not.
     *
     * @param cancel true if the event should be cancelled, false otherwise
     */
    void setCancelled(boolean cancel);

    /**
     * Simple cancelable implementation of {@link EventMutatorCancellable}.
     * <p>
     * This should only be used when all fields are final except cancellable.
     *
     * @param <T> Event type
     */
    abstract class Simple<T extends CancellableEvent<T>> implements EventMutatorCancellable<T> {
        protected @NotNull T event;
        private boolean cancelled;
        
        public Simple(@NotNull T event) {
            this.event = event;
            this.setCancelled(event.cancelled());
        }
        
        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }
    }
}
