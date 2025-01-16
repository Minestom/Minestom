package net.minestom.server.extras.query.event;

import net.minestom.server.event.trait.mutation.EventMutator;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import net.minestom.server.extras.query.response.FullQueryResponse;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;
import java.util.Objects;

/**
 * An event called when a full query is received and ready to be responded to.
 */
public record FullQueryEvent(@NotNull SocketAddress sender, int sessionID, @NotNull FullQueryResponse response, boolean cancelled) implements QueryEvent<FullQueryResponse, FullQueryEvent> {

    /**
     * Creates a new full query event.
     *
     * @param sender the sender
     * @param sessionID the sessionID
     */
    public FullQueryEvent(@NotNull SocketAddress sender, int sessionID) {
        this(sender, sessionID, new FullQueryResponse(), false);
    }
    /**
     * Gets the query response that will be sent back to the sender.
     * This can be mutated.
     *
     * @return the response
     */
    @Override
    public FullQueryResponse queryResponse() {
        return this.response;
    }

    /**
     * Gets the socket address of the initiator of the query.
     *
     * @return the initiator
     */
    public @NotNull SocketAddress sender() {
        return this.sender;
    }

    /**
     * Gets the Session ID of the initiator of the query.
     *
     * @return the session ID
     */
    public int sessionId() {
        return this.sessionID;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutatorCancellable<FullQueryEvent> {
        private final SocketAddress sender;
        private final int sessionID;

        private FullQueryResponse response;
        private boolean cancelled;

        public Mutator(FullQueryEvent event) {
            this.sender = event.sender;
            this.sessionID = event.sessionID;
            this.response = event.response;
            this.cancelled = event.cancelled;
        }


        /**
         * Gets the query response that will be sent back to the sender.
         * This can be mutated.
         *
         * @return the response
         */
        public FullQueryResponse getQueryResponse() {
            return this.response;
        }

        /**
         * Sets the query response that will be sent back to the sender.
         *
         * @param response the response
         */
        public void setQueryResponse(@NotNull FullQueryResponse response) {
            this.response = Objects.requireNonNull(response, "response");
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }

        @Override
        public @NotNull FullQueryEvent mutated() {
            return new FullQueryEvent(this.sender, this.sessionID, this.response, this.cancelled);
        }
    }
}
